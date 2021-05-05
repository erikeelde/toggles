package com.izettle.wrench.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Build
import androidx.annotation.RequiresApi
import com.izettle.wrench.core.Bolt
import com.izettle.wrench.core.WrenchProviderContract
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchApplicationDao
import com.izettle.wrench.database.WrenchConfiguration
import com.izettle.wrench.database.WrenchConfigurationDao
import com.izettle.wrench.database.WrenchConfigurationValue
import com.izettle.wrench.database.WrenchConfigurationValueDao
import com.izettle.wrench.database.WrenchPredefinedConfigurationValue
import com.izettle.wrench.database.WrenchPredefinedConfigurationValueDao
import com.izettle.wrench.database.WrenchScope
import com.izettle.wrench.database.WrenchScopeDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.BuildConfig
import se.eelde.toggles.TogglesUriMatcher
import se.eelde.toggles.TogglesUriMatcher.Companion.CURRENT_CONFIGURATIONS
import se.eelde.toggles.TogglesUriMatcher.Companion.CURRENT_CONFIGURATION_ID
import se.eelde.toggles.TogglesUriMatcher.Companion.CURRENT_CONFIGURATION_KEY
import se.eelde.toggles.TogglesUriMatcher.Companion.PREDEFINED_CONFIGURATION_VALUES
import se.eelde.toggles.prefs.TogglesPreferences
import se.eelde.toggles.provider.IPackageManagerWrapper
import se.eelde.toggles.provider.notifyInsert
import se.eelde.toggles.provider.notifyUpdate
import java.util.Date

class WrenchProvider : ContentProvider() {

    val applicationDao: WrenchApplicationDao by lazy {
        applicationEntryPoint.provideWrenchApplicationDao()
    }

    val scopeDao: WrenchScopeDao by lazy {
        applicationEntryPoint.provideWrenchScopeDao()
    }

    val configurationDao: WrenchConfigurationDao by lazy {
        applicationEntryPoint.provideWrenchConfigurationDao()
    }

    val configurationValueDao: WrenchConfigurationValueDao by lazy {
        applicationEntryPoint.provideWrenchConfigurationValueDao()
    }

    private val predefinedConfigurationDao: WrenchPredefinedConfigurationValueDao by lazy {
        applicationEntryPoint.providePredefinedConfigurationValueDao()
    }

    private val packageManagerWrapper: IPackageManagerWrapper by lazy {
        applicationEntryPoint.providePackageManagerWrapper()
    }

    private val togglesPreferences: TogglesPreferences by lazy {
        applicationEntryPoint.providesWrenchPreferences()
    }

    private val applicationEntryPoint: WrenchProviderEntryPoint by lazy {
        EntryPointAccessors.fromApplication(context!!, WrenchProviderEntryPoint::class.java)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrenchProviderEntryPoint {
        fun provideWrenchApplicationDao(): WrenchApplicationDao
        fun provideWrenchConfigurationDao(): WrenchConfigurationDao
        fun provideWrenchConfigurationValueDao(): WrenchConfigurationValueDao
        fun provideWrenchScopeDao(): WrenchScopeDao
        fun providePredefinedConfigurationValueDao(): WrenchPredefinedConfigurationValueDao
        fun providePackageManagerWrapper(): IPackageManagerWrapper
        fun providesWrenchPreferences(): TogglesPreferences
    }

    @Synchronized
    private fun getCallingApplication(applicationDao: WrenchApplicationDao): WrenchApplication {
        var wrenchApplication: WrenchApplication? =
            applicationDao.loadByPackageName(packageManagerWrapper.callingApplicationPackageName!!)

        if (wrenchApplication == null) {
            try {
                wrenchApplication = WrenchApplication(
                    id = 0,
                    packageName = packageManagerWrapper.callingApplicationPackageName!!,
                    applicationLabel = packageManagerWrapper.applicationLabel,
                    shortcutId = packageManagerWrapper.callingApplicationPackageName!!,
                )

                wrenchApplication.id = applicationDao.insert(wrenchApplication)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                throw e
            }
        }

        return wrenchApplication
    }

    override fun onCreate() = true

    @Suppress("LongMethod")
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {

        val callingApplication = getCallingApplication(applicationDao)

        if (!isWrenchApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        var cursor: Cursor?

        when (uriMatcher.match(uri)) {
            CURRENT_CONFIGURATION_ID -> {
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
            CURRENT_CONFIGURATION_KEY -> {
                val scope = getSelectedScope(context, scopeDao, callingApplication.id)
                cursor = configurationDao.getToggle(uri.lastPathSegment!!, scope!!.id)

                if (cursor.count == 0) {
                    cursor.close()

                    val defaultScope = getDefaultScope(context, scopeDao, callingApplication.id)
                    cursor = configurationDao.getToggle(uri.lastPathSegment!!, defaultScope!!.id)
                }
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

    private fun isWrenchApplication(callingApplication: WrenchApplication): Boolean {
        return callingApplication.packageName == BuildConfig.APPLICATION_ID
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {

        val callingApplication = getCallingApplication(applicationDao)

        if (!isWrenchApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        val insertId: Long
        when (uriMatcher.match(uri)) {
            CURRENT_CONFIGURATIONS -> {
                var bolt = Bolt.fromContentValues(values!!)

                bolt = fixRCTypes(bolt)

                var wrenchConfiguration: WrenchConfiguration? =
                    configurationDao.getWrenchConfiguration(callingApplication.id, bolt.key)

                if (wrenchConfiguration == null) {
                    wrenchConfiguration =
                        WrenchConfiguration(0, callingApplication.id, bolt.key, bolt.type)

                    wrenchConfiguration.id = configurationDao.insert(wrenchConfiguration)
                }

                val defaultScope = getDefaultScope(context, scopeDao, callingApplication.id)

                val wrenchConfigurationValue = WrenchConfigurationValue(
                    0,
                    wrenchConfiguration.id,
                    bolt.value,
                    defaultScope!!.id
                )
                wrenchConfigurationValue.configurationId = wrenchConfiguration.id
                wrenchConfigurationValue.value = bolt.value
                wrenchConfigurationValue.scope = defaultScope.id

                wrenchConfigurationValue.id =
                    configurationValueDao.insertSync(wrenchConfigurationValue)

                insertId = wrenchConfiguration.id
            }
            PREDEFINED_CONFIGURATION_VALUES -> {
                val fullConfig = WrenchPredefinedConfigurationValue.fromContentValues(values!!)
                insertId = predefinedConfigurationDao.insert(fullConfig)
            }
            else -> {
                throw UnsupportedOperationException("Not yet implemented $uri")
            }
        }

        context!!.contentResolver.notifyInsert(Uri.withAppendedPath(uri, insertId.toString()))

        return ContentUris.withAppendedId(uri, insertId)
    }

    private fun fixRCTypes(bolt: Bolt): Bolt {
        return when (bolt.type) {
            "java.lang.String" -> bolt.copy(bolt.id, Bolt.TYPE.STRING, bolt.key, bolt.value)
            "java.lang.Integer" -> bolt.copy(bolt.id, Bolt.TYPE.INTEGER, bolt.key, bolt.value)
            "java.lang.Enum" -> bolt.copy(bolt.id, Bolt.TYPE.ENUM, bolt.key, bolt.value)
            "java.lang.Boolean" -> bolt.copy(bolt.id, Bolt.TYPE.BOOLEAN, bolt.key, bolt.value)
            else -> {
                bolt
            }
        }
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isWrenchApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        return super.bulkInsert(uri, values)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {

        val callingApplication = getCallingApplication(applicationDao)

        if (!isWrenchApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        val updatedRows: Int
        when (uriMatcher.match(uri)) {
            CURRENT_CONFIGURATION_ID -> {
                val bolt = Bolt.fromContentValues(values!!)
                val scope = getSelectedScope(context, scopeDao, callingApplication.id)
                updatedRows = configurationValueDao.updateConfigurationValueSync(
                    java.lang.Long.parseLong(uri.lastPathSegment!!),
                    scope!!.id,
                    bolt.value!!
                )
                if (updatedRows == 0) {
                    val wrenchConfigurationValue = WrenchConfigurationValue(
                        0,
                        java.lang.Long.parseLong(uri.lastPathSegment!!),
                        bolt.value,
                        scope.id
                    )
                    configurationValueDao.insertSync(wrenchConfigurationValue)
                }
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

        if (!isWrenchApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun getType(uri: Uri): String {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isWrenchApplication(callingApplication)) {
            assertValidApiVersion(togglesPreferences, uri)
        }

        return when (uriMatcher.match(uri)) {
            CURRENT_CONFIGURATIONS -> {
                "vnd.android.cursor.dir/vnd.${BuildConfig.APPLICATION_ID}.currentConfiguration"
            }
            CURRENT_CONFIGURATION_ID -> {
                "vnd.android.cursor.item/vnd.${BuildConfig.APPLICATION_ID}.currentConfiguration"
            }
            CURRENT_CONFIGURATION_KEY -> {
                "vnd.android.cursor.dir/vnd.${BuildConfig.APPLICATION_ID}.currentConfiguration"
            }
            PREDEFINED_CONFIGURATION_VALUES -> {
                "vnd.android.cursor.dir/vnd.${BuildConfig.APPLICATION_ID}.predefinedConfigurationValue"
            }
            else -> {
                throw UnsupportedOperationException("Not yet implemented")
            }
        }
    }

    companion object {

        private val uriMatcher = TogglesUriMatcher.getTogglesUriMatcher()

        private const val oneSecond = 1000

        @Synchronized
        private fun getDefaultScope(
            context: Context?,
            scopeDao: WrenchScopeDao?,
            applicationId: Long
        ): WrenchScope? {
            if (context == null) {
                return null
            }

            var scope: WrenchScope? = scopeDao!!.getDefaultScope(applicationId)

            if (scope == null) {
                scope = WrenchScope()
                scope.applicationId = applicationId
                val id = scopeDao.insert(scope)
                scope.id = id
            }
            return scope
        }

        @Synchronized
        private fun getSelectedScope(
            context: Context?,
            scopeDao: WrenchScopeDao?,
            applicationId: Long
        ): WrenchScope? {
            if (context == null) {
                return null
            }

            var scope: WrenchScope? = scopeDao!!.getSelectedScope(applicationId)

            if (scope == null) {
                val defaultScope = WrenchScope()
                defaultScope.applicationId = applicationId
                defaultScope.id = scopeDao.insert(defaultScope)

                val customScope = WrenchScope()
                customScope.applicationId = applicationId
                customScope.timeStamp = Date(defaultScope.timeStamp.time + oneSecond)
                customScope.name = WrenchScope.SCOPE_USER
                customScope.id = scopeDao.insert(customScope)

                scope = customScope
            }
            return scope
        }

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
                se.eelde.toggles.provider.API_1 -> {
                    return
                }
                se.eelde.toggles.provider.API_INVALID -> {
                    if (strictApiVersion) {
                        throw IllegalArgumentException("This content provider requires you to provide a valid api-version in a queryParameter")
                    }
                }
            }
        }

        @WrenchApiVersion
        private fun getApiVersion(uri: Uri): Int {
            val queryParameter = uri.getQueryParameter(WrenchProviderContract.WRENCH_API_VERSION)
            return if (queryParameter != null) {
                Integer.valueOf(queryParameter)
            } else {
                API_INVALID
            }
        }
    }
}
