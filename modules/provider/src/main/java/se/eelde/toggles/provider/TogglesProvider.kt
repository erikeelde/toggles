package se.eelde.toggles.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.os.Binder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.database.WrenchApplication
import se.eelde.toggles.database.WrenchConfiguration
import se.eelde.toggles.database.WrenchConfigurationValue
import se.eelde.toggles.database.WrenchPredefinedConfigurationValue
import se.eelde.toggles.database.WrenchScope
import se.eelde.toggles.database.dao.provider.ProviderApplicationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderPredefinedConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderScopeDao
import se.eelde.toggles.prefs.TogglesPreferences
import java.util.Date

class TogglesProvider : ContentProvider() {

    private val applicationDao: ProviderApplicationDao by lazy {
        applicationEntryPoint.provideProviderApplicationDao()
    }

    private val scopeDao: ProviderScopeDao by lazy {
        applicationEntryPoint.provideProviderScopeDao()
    }

    val configurationDao: ProviderConfigurationDao by lazy {
        applicationEntryPoint.provideProviderConfigurationDao()
    }

    val configurationValueDao: ProviderConfigurationValueDao by lazy {
        applicationEntryPoint.provideProviderConfigurationValueDao()
    }

    private val predefinedConfigurationDao: ProviderPredefinedConfigurationValueDao by lazy {
        applicationEntryPoint.providePredefinedConfigurationValueDao()
    }

    private val packageManagerWrapper: IPackageManagerWrapper by lazy {
        applicationEntryPoint.providePackageManagerWrapper()
    }

    private val togglesPreferences: TogglesPreferences by lazy {
        applicationEntryPoint.provideTogglesPreferences()
    }

    private val togglesUriMatcher: TogglesUriMatcher by lazy {
        applicationEntryPoint.provideTogglesUriMatcher()
    }

    private val applicationEntryPoint: TogglesProviderEntryPoint by lazy {
        entryPointBuilder.build(context!!)
    }

    internal interface EntryPointBuilder {
        fun build(context: Context): TogglesProviderEntryPoint
    }

    internal var entryPointBuilder: EntryPointBuilder = object : EntryPointBuilder {
        override fun build(context: Context): TogglesProviderEntryPoint {
            return EntryPointAccessors.fromApplication(context, TogglesProviderEntryPoint::class.java)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TogglesProviderEntryPoint {
        fun provideProviderApplicationDao(): ProviderApplicationDao
        fun provideProviderConfigurationDao(): ProviderConfigurationDao
        fun provideProviderConfigurationValueDao(): ProviderConfigurationValueDao
        fun provideProviderScopeDao(): ProviderScopeDao
        fun providePredefinedConfigurationValueDao(): ProviderPredefinedConfigurationValueDao
        fun providePackageManagerWrapper(): IPackageManagerWrapper
        fun provideTogglesPreferences(): TogglesPreferences
        fun provideTogglesUriMatcher(): TogglesUriMatcher
    }

    private fun getCallingApplication(applicationDao: ProviderApplicationDao): WrenchApplication =
        synchronized(this) {
            var wrenchApplication: WrenchApplication? =
                applicationDao.loadByPackageName(packageManagerWrapper.callingApplicationPackageName!!)

            if (wrenchApplication == null) {
                wrenchApplication = WrenchApplication(
                    id = 0,
                    packageName = packageManagerWrapper.callingApplicationPackageName!!,
                    applicationLabel = packageManagerWrapper.applicationLabel,
                    shortcutId = packageManagerWrapper.callingApplicationPackageName!!,
                )

                wrenchApplication.id = applicationDao.insert(wrenchApplication)
            }

            return wrenchApplication
        }

    override fun onCreate() = true

    @Suppress("LongMethod", "NestedBlockDepth")
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val callingApplication = getCallingApplication(applicationDao)
        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        var cursor: Cursor?

        when (togglesUriMatcher.match(uri)) {
            UriMatch.CURRENT_CONFIGURATION_ID -> {
                val scope = getSelectedScope(context, scopeDao, callingApplication.id)
                cursor = configurationDao.getToggle(
                    java.lang.Long.valueOf(uri.lastPathSegment!!),
                    scope!!.id
                )

                if (cursor.count == 0) {
                    cursor.close()

                    val defaultScope = getDefaultScope(context, scopeDao, callingApplication.id)
                    cursor = configurationDao.getToggle(
                        java.lang.Long.valueOf(uri.lastPathSegment!!),
                        defaultScope!!.id
                    )
                }
            }

            UriMatch.CURRENT_CONFIGURATION_KEY -> {
                // this change is experimental and might be a way
                // for consumers to
                @Suppress("ConstantConditionIf")
                if (false) {
                    val scope = getSelectedScope(context, scopeDao, callingApplication.id)
                    val defaultScope = getDefaultScope(context, scopeDao, callingApplication.id)
                    cursor = configurationDao.getToggles(
                        uri.lastPathSegment!!,
                        listOf(scope!!.id, defaultScope!!.id)
                    )
                } else {
                    val scope = getSelectedScope(context, scopeDao, callingApplication.id)
                    cursor = configurationDao.getToggle(uri.lastPathSegment!!, scope!!.id)

                    if (cursor.count == 0) {
                        cursor.close()

                        val defaultScope = getDefaultScope(context, scopeDao, callingApplication.id)
                        cursor =
                            configurationDao.getToggle(uri.lastPathSegment!!, defaultScope!!.id)
                    }
                }
            }

            UriMatch.CONFIGURATIONS -> {
                cursor = configurationDao.getConfigurationCursor(callingApplication.id)
            }

            UriMatch.CONFIGURATION_KEY -> {
                cursor = configurationDao.getConfigurationCursor(
                    callingApplication.id,
                    uri.lastPathSegment!!
                )
            }

            UriMatch.CONFIGURATION_ID -> {
                cursor = configurationDao.getConfigurationCursor(
                    callingApplication.id,
                    uri.lastPathSegment!!.toLong()
                )
            }

            UriMatch.CONFIGURATION_VALUE_KEY -> {
                cursor = configurationDao.getConfigurationValueCursor(
                    callingApplication.id,
                    uri.pathSegments.get(uri.pathSegments.size - 2)
                )
            }

            UriMatch.CONFIGURATION_VALUE_ID -> {
                cursor = configurationDao.getConfigurationValueCursor(
                    callingApplication.id,
                    uri.pathSegments.get(uri.pathSegments.size - 2).toLong()
                )
            }

            else -> {
                throw UnsupportedOperationException("Not yet implemented $uri")
            }
        }

        if (context != null) {
            cursor.setNotificationUri(context!!.contentResolver, uri)
        }

        return cursor
    }

    private fun isTogglesApplication(callingApplication: WrenchApplication): Boolean {
        return callingApplication.packageName == context!!.packageName
    }

    @Suppress("LongMethod")
    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        val insertId: Long
        when (togglesUriMatcher.match(uri)) {
            UriMatch.CURRENT_CONFIGURATIONS -> {
                val toggle = Toggle.fromContentValues(values!!)

                var wrenchConfiguration: WrenchConfiguration? =
                    configurationDao.getWrenchConfiguration(callingApplication.id, toggle.key)

                if (wrenchConfiguration == null) {
                    wrenchConfiguration =
                        WrenchConfiguration(0, callingApplication.id, toggle.key, toggle.type)

                    wrenchConfiguration.id = configurationDao.insert(wrenchConfiguration)
                }

                val defaultScope = getDefaultScope(context, scopeDao, callingApplication.id)

                val wrenchConfigurationValue = WrenchConfigurationValue(
                    0,
                    wrenchConfiguration.id,
                    toggle.value,
                    defaultScope!!.id
                )
                wrenchConfigurationValue.configurationId = wrenchConfiguration.id
                wrenchConfigurationValue.value = toggle.value
                wrenchConfigurationValue.scope = defaultScope.id

                @Suppress("SwallowedException")
                try {
                    wrenchConfigurationValue.id =
                        configurationValueDao.insertSync(wrenchConfigurationValue)
                } catch (e: SQLiteConstraintException) {
                    // this happens when the app is initially launched because many of many calls
                    // into assertValidApiVersion()
                }

                insertId = wrenchConfiguration.id
            }

            UriMatch.PREDEFINED_CONFIGURATION_VALUES -> {
                val fullConfig = WrenchPredefinedConfigurationValue.fromContentValues(values!!)
                insertId = try {
                    predefinedConfigurationDao.insert(fullConfig)
                } catch (_: SQLiteConstraintException) {
                    predefinedConfigurationDao.getByConfigurationAndValueId(
                        fullConfig.configurationId,
                        fullConfig.value!!
                    ).id
                }
            }

            UriMatch.CONFIGURATIONS -> {
                val togglesConfiguration = TogglesConfiguration.fromContentValues(values!!)
                val wrenchConfiguration = WrenchConfiguration(
                    id = togglesConfiguration.id,
                    applicationId = callingApplication.id,
                    key = togglesConfiguration.key,
                    type = togglesConfiguration.type
                )
                insertId = configurationDao.insert(wrenchConfiguration)
            }

            else -> {
                throw UnsupportedOperationException("Not yet implemented $uri")
            }
        }

        context!!.contentResolver.notifyInsert(Uri.withAppendedPath(uri, insertId.toString()))

        return ContentUris.withAppendedId(uri, insertId)
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        return super.bulkInsert(uri, values)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        val updatedRows: Int
        when (togglesUriMatcher.match(uri)) {
            UriMatch.CURRENT_CONFIGURATION_ID -> {
                val toggle = Toggle.fromContentValues(values!!)
                val scope = getSelectedScope(context, scopeDao, callingApplication.id)
                updatedRows = configurationValueDao.updateConfigurationValueSync(
                    java.lang.Long.parseLong(uri.lastPathSegment!!),
                    scope!!.id,
                    toggle.value!!
                )
                if (updatedRows == 0) {
                    val wrenchConfigurationValue = WrenchConfigurationValue(
                        0,
                        java.lang.Long.parseLong(uri.lastPathSegment!!),
                        toggle.value,
                        scope.id
                    )
                    configurationValueDao.insertSync(wrenchConfigurationValue)
                }
            }

            UriMatch.CONFIGURATION_ID -> {
                val fromContentValues = TogglesConfiguration.fromContentValues(values!!)
                updatedRows = configurationDao.updateConfiguration(
                    callingApplication = callingApplication.id,
                    id = uri.lastPathSegment!!.toLong(),
                    key = fromContentValues.key,
                    type = fromContentValues.type
                )
            }

            else -> {
                throw UnsupportedOperationException("Not yet implemented $uri")
            }
        }

        if (updatedRows > 0) {
            context!!.contentResolver.notifyUpdate(uri)
        }

        return updatedRows
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        when (togglesUriMatcher.match(uri)) {
            UriMatch.CONFIGURATION_ID -> {
                val deletedRows =
                    configurationDao.deleteConfiguration(
                        callingApplication.id,
                        uri.lastPathSegment!!.toLong()
                    )
                if (deletedRows > 0) {
                    context!!.contentResolver.notifyChange(uri, null)
                }
                return deletedRows
            }

            UriMatch.CONFIGURATION_KEY -> {
                val deletedRows =
                    configurationDao.deleteConfiguration(
                        callingApplication.id,
                        uri.lastPathSegment!!
                    )
                if (deletedRows > 0) {
                    context!!.contentResolver.notifyChange(uri, null)
                }
                return deletedRows
            }

            else -> {
                throw UnsupportedOperationException("Not yet implemented $uri")
            }
        }
    }

    override fun getType(uri: Uri): String {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        return when (togglesUriMatcher.match(uri)) {
            UriMatch.CURRENT_CONFIGURATIONS ->
                "vnd.android.cursor.dir/vnd.${context!!.packageName}.currentConfiguration"

            UriMatch.CURRENT_CONFIGURATION_ID ->
                "vnd.android.cursor.item/vnd.${context!!.packageName}.currentConfiguration"

            UriMatch.CURRENT_CONFIGURATION_KEY ->
                "vnd.android.cursor.item/vnd.${context!!.packageName}.currentConfiguration"

            UriMatch.CONFIGURATIONS ->
                "vnd.android.cursor.dir/vnd.${context!!.packageName}.configuration"

            UriMatch.CONFIGURATION_ID ->
                "vnd.android.cursor.item/vnd.${context!!.packageName}.configuration"

            UriMatch.CONFIGURATION_KEY ->
                "vnd.android.cursor.item/vnd.${context!!.packageName}.configuration"

            UriMatch.PREDEFINED_CONFIGURATION_VALUES ->
                "vnd.android.cursor.dir/vnd.${context!!.packageName}.predefinedConfigurationValue"

            UriMatch.CONFIGURATION_VALUE_ID -> "vnd.android.cursor.dir/vnd.${context!!.packageName}.configurationValue"
            UriMatch.CONFIGURATION_VALUE_KEY -> "vnd.android.cursor.dir/vnd.${context!!.packageName}.configurationValue"
            UriMatch.UNKNOWN -> TODO()
        }
    }

    companion object {

        private const val oneSecond = 1000

        @Synchronized
        private fun getDefaultScope(
            context: Context?,
            scopeDao: ProviderScopeDao?,
            applicationId: Long
        ): WrenchScope? {
            if (context == null) {
                return null
            }

            var scope: WrenchScope? = scopeDao!!.getDefaultScope(applicationId)

            if (scope == null) {
                scope = WrenchScope.newWrenchScope()
                scope.applicationId = applicationId
                val id = scopeDao.insert(scope)
                scope.id = id
            }
            return scope
        }

        @Synchronized
        private fun getSelectedScope(
            context: Context?,
            scopeDao: ProviderScopeDao?,
            applicationId: Long
        ): WrenchScope? {
            if (context == null) {
                return null
            }

            var scope: WrenchScope? = scopeDao!!.getSelectedScope(applicationId)

            if (scope == null) {
                val defaultScope = WrenchScope.newWrenchScope()
                defaultScope.applicationId = applicationId
                defaultScope.id = scopeDao.insert(defaultScope)

                val customScope = WrenchScope.newWrenchScope()
                customScope.applicationId = applicationId
                customScope.timeStamp = Date(defaultScope.timeStamp.time + oneSecond)
                customScope.name = WrenchScope.SCOPE_USER
                customScope.id = scopeDao.insert(customScope)

                scope = customScope
            }
            return scope
        }

        @Synchronized
        private fun assertValidApiVersion(togglesPreferences: TogglesPreferences, uri: Uri) {
            var l: Long = 0
            val strictApiVersion = try {
                l = Binder.clearCallingIdentity()
                togglesPreferences.getBoolean(
                    "Require valid wrench api version",
                    false
                )
            } finally {
                Binder.restoreCallingIdentity(l)
            }

            when (getApiVersion(uri)) {
                API_1 -> {
                    return
                }

                API_INVALID -> {
                    if (strictApiVersion) {
                        throw IllegalArgumentException(
                            "This content provider requires you to provide a " +
                                "valid api-version in a queryParameter"
                        )
                    }
                }
            }
        }

        @TogglesApiVersion
        private fun getApiVersion(uri: Uri): Int {
            val queryParameter = uri.getQueryParameter("API_VERSION")
            return if (queryParameter != null) {
                Integer.valueOf(queryParameter)
            } else {
                API_INVALID
            }
        }
    }
}
