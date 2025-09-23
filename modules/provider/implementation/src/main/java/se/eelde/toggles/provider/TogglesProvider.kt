package se.eelde.toggles.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesPredefinedConfigurationValue
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.dao.provider.ProviderApplicationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderPredefinedConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderScopeDao
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
            return EntryPointAccessors.fromApplication(
                context,
                TogglesProviderEntryPoint::class.java
            )
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
        fun provideTogglesUriMatcher(): TogglesUriMatcher
    }

    private fun getCallingApplication(applicationDao: ProviderApplicationDao): TogglesApplication =
        synchronized(this) {
            var togglesApplication: TogglesApplication? =
                applicationDao.loadByPackageName(packageManagerWrapper.callingApplicationPackageName!!)

            if (togglesApplication == null) {
                togglesApplication = TogglesApplication(
                    id = 0,
                    packageName = packageManagerWrapper.callingApplicationPackageName!!,
                    applicationLabel = packageManagerWrapper.applicationLabel,
                    shortcutId = packageManagerWrapper.callingApplicationPackageName!!,
                )

                togglesApplication.id = applicationDao.insert(togglesApplication)

                createDefaultScope(scopeDao, togglesApplication.id)
                createDevelopmentScope(scopeDao, togglesApplication.id)
            }

            return togglesApplication
        }

    override fun onCreate() = true

    @Suppress("LongMethod", "NestedBlockDepth", "CyclomaticComplexMethod")
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val callingApplication = getCallingApplication(applicationDao)
        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(uri)
        }

        var cursor: Cursor?

        when (togglesUriMatcher.match(uri)) {
            UriMatch.CURRENT_CONFIGURATION_ID -> {
                val scope = getSelectedScope(scopeDao, callingApplication.id)
                cursor = configurationDao.getToggle(
                    java.lang.Long.valueOf(uri.lastPathSegment!!),
                    scope.id
                )

                if (cursor.count == 0) {
                    cursor.close()

                    val defaultScope = getDefaultScope(scopeDao, callingApplication.id)
                    cursor = configurationDao.getToggle(
                        java.lang.Long.valueOf(uri.lastPathSegment!!),
                        defaultScope.id
                    )
                }
            }

            UriMatch.CURRENT_CONFIGURATION_KEY -> {
                val scope = getSelectedScope(scopeDao, callingApplication.id)
                cursor = configurationDao.getToggle(uri.lastPathSegment!!, scope.id)

                if (cursor.count == 0) {
                    cursor.close()

                    val defaultScope = getDefaultScope(scopeDao, callingApplication.id)
                    cursor =
                        configurationDao.getToggle(uri.lastPathSegment!!, defaultScope.id)
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
                    uri.pathSegments[uri.pathSegments.size - 2]
                )
            }

            UriMatch.CONFIGURATION_VALUE_ID -> {
                cursor = configurationDao.getConfigurationValueCursor(
                    callingApplication.id,
                    uri.pathSegments[uri.pathSegments.size - 2].toLong()
                )
            }

            UriMatch.SCOPES -> {
                // getDefaultScope() ensures the default scope is created.
                getDefaultScope(scopeDao, callingApplication.id)
                // getSelectedScope() ensures there is a selected scope.
                getSelectedScope(scopeDao, callingApplication.id)
                cursor = configurationDao.getScopeCursor(callingApplication.id)
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

    private fun isTogglesApplication(callingApplication: TogglesApplication): Boolean {
        return callingApplication.packageName == context!!.packageName
    }

    @Suppress("LongMethod")
    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(uri)
        }

        val insertId: Long
        when (togglesUriMatcher.match(uri)) {
            UriMatch.CURRENT_CONFIGURATIONS -> {
                val toggle = Toggle.fromContentValues(values!!)

                var togglesConfiguration: se.eelde.toggles.database.TogglesConfiguration? =
                    configurationDao.getTogglesConfiguration(callingApplication.id, toggle.key)

                if (togglesConfiguration == null) {
                    togglesConfiguration =
                        se.eelde.toggles.database.TogglesConfiguration(
                            id = 0,
                            applicationId = callingApplication.id,
                            key = toggle.key,
                            type = toggle.type
                        )

                    togglesConfiguration.id = configurationDao.insert(togglesConfiguration)
                }

                val defaultScope = getDefaultScope(scopeDao, callingApplication.id)

                val togglesConfigurationValue = TogglesConfigurationValue(
                    0,
                    togglesConfiguration.id,
                    toggle.value,
                    defaultScope.id
                )
                togglesConfigurationValue.configurationId = togglesConfiguration.id
                togglesConfigurationValue.value = toggle.value
                togglesConfigurationValue.scope = defaultScope.id

                @Suppress("SwallowedException")
                try {
                    togglesConfigurationValue.id =
                        configurationValueDao.insertSync(togglesConfigurationValue)
                } catch (e: SQLiteConstraintException) {
                    // this happens when the app is initially launched because many of many calls
                    // into assertValidApiVersion()
                }

                insertId = togglesConfiguration.id
            }

            UriMatch.PREDEFINED_CONFIGURATION_VALUES -> {
                val fullConfig = TogglesPredefinedConfigurationValue.fromContentValues(values!!)
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
                val databaseConfiguration = se.eelde.toggles.database.TogglesConfiguration(
                    id = togglesConfiguration.id,
                    applicationId = callingApplication.id,
                    key = togglesConfiguration.key,
                    type = togglesConfiguration.type
                )
                insertId = configurationDao.insert(databaseConfiguration)
            }

            UriMatch.CONFIGURATION_VALUE_ID -> {
                val togglesConfigurationValue =
                    se.eelde.toggles.core.TogglesConfigurationValue.fromContentValues(values!!)
                val databaseConfigurationValue = TogglesConfigurationValue(
                    id = togglesConfigurationValue.id,
                    configurationId = togglesConfigurationValue.configurationId,
                    value = togglesConfigurationValue.value,
                    scope = togglesConfigurationValue.scope
                )
                insertId = configurationValueDao.insertSync(databaseConfigurationValue)
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
            assertValidApiVersion(uri)
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
            assertValidApiVersion(uri)
        }

        val updatedRows: Int
        when (togglesUriMatcher.match(uri)) {
            UriMatch.CURRENT_CONFIGURATION_ID -> {
                val toggle = Toggle.fromContentValues(values!!)
                val scope = getSelectedScope(scopeDao, callingApplication.id)
                val updatedCount = configurationValueDao.updateConfigurationValueSync(
                    java.lang.Long.parseLong(uri.lastPathSegment!!),
                    scope.id,
                    toggle.value!!
                )

                updatedRows = if (updatedCount > 0) {
                    updatedCount
                } else {
                    val togglesConfigurationValue = TogglesConfigurationValue(
                        0,
                        java.lang.Long.parseLong(uri.lastPathSegment!!),
                        toggle.value,
                        scope.id
                    )
                    val insertSync = configurationValueDao.insertSync(togglesConfigurationValue)
                    if (insertSync > 0) 1 else 0
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

            UriMatch.CONFIGURATION_VALUE_ID -> {
                val togglesConfigurationValue =
                    se.eelde.toggles.core.TogglesConfigurationValue.fromContentValues(values!!)

                updatedRows = togglesConfigurationValue.value?.let {
                    configurationValueDao.updateConfigurationValueSync(
                        configurationId = togglesConfigurationValue.configurationId,
                        scopeId = togglesConfigurationValue.scope,
                        value = it
                    )
                } ?: throw NullPointerException("Configuration value cannot be null")
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
            assertValidApiVersion(uri)
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

    private val packageName = "se.eelde.toggles"

    override fun getType(uri: Uri): String {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(uri)
        }

        return when (togglesUriMatcher.match(uri)) {
            UriMatch.CURRENT_CONFIGURATIONS ->
                "vnd.android.cursor.dir/vnd.$packageName.currentConfiguration"

            UriMatch.CURRENT_CONFIGURATION_ID ->
                "vnd.android.cursor.item/vnd.$packageName.currentConfiguration"

            UriMatch.CURRENT_CONFIGURATION_KEY ->
                "vnd.android.cursor.item/vnd.$packageName.currentConfiguration"

            UriMatch.CONFIGURATIONS ->
                "vnd.android.cursor.dir/vnd.$packageName.configuration"

            UriMatch.CONFIGURATION_ID ->
                "vnd.android.cursor.item/vnd.$packageName.configuration"

            UriMatch.CONFIGURATION_KEY ->
                "vnd.android.cursor.item/vnd.$packageName.configuration"

            UriMatch.PREDEFINED_CONFIGURATION_VALUES ->
                "vnd.android.cursor.dir/vnd.$packageName.predefinedConfigurationValue"

            UriMatch.CONFIGURATION_VALUE_ID -> "vnd.android.cursor.dir/vnd.$packageName.configurationValue"
            UriMatch.CONFIGURATION_VALUE_KEY -> "vnd.android.cursor.dir/vnd.$packageName.configurationValue"
            UriMatch.SCOPES ->
                "vnd.android.cursor.dir/vnd.$packageName.scope"

            UriMatch.UNKNOWN -> TODO()
        }
    }

    companion object {

        private const val oneSecond = 1000

        @Synchronized
        private fun getDefaultScope(
            scopeDao: ProviderScopeDao,
            applicationId: Long
        ): TogglesScope = scopeDao.getDefaultScope(applicationId)
            ?: error("No default scope for application $applicationId")

        @Synchronized
        private fun getSelectedScope(
            scopeDao: ProviderScopeDao,
            applicationId: Long
        ): TogglesScope = scopeDao.getSelectedScope(applicationId)
            ?: error("No selected scope for application $applicationId")

        private fun createDefaultScope(
            scopeDao: ProviderScopeDao,
            applicationId: Long
        ): TogglesScope {
            val scope = TogglesScope.newScope()
            scope.applicationId = applicationId
            val id = scopeDao.insert(scope)
            scope.timeStamp = Date(Date().time - oneSecond)
            scope.id = id
            return scope
        }

        private fun createDevelopmentScope(
            scopeDao: ProviderScopeDao,
            applicationId: Long
        ): TogglesScope {
            val developmentScope = TogglesScope.newScope()
            developmentScope.applicationId = applicationId
            developmentScope.timeStamp = Date()
            developmentScope.name = TogglesScope.SCOPE_USER
            developmentScope.id = scopeDao.insert(developmentScope)
            return developmentScope
        }

        @Synchronized
        private fun assertValidApiVersion(uri: Uri) {
            when (getApiVersion(uri)) {
                API_1 -> {
                    return
                }

                API_INVALID -> {
                    throw IllegalArgumentException(
                        "This content provider requires you to provide a " +
                            "valid api-version in a queryParameter"
                    )
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
