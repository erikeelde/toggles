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
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesPredefinedConfigurationValue
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.dao.provider.ProviderApplicationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderPredefinedConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderScopeDao
import se.eelde.toggles.provider.resolution.ScopeChain
import kotlin.time.Clock

@Suppress("TooManyFunctions")
class TogglesProvider : ContentProvider() {

    private val requireContext: Context
        get() = requireNotNull(context) { "ContentProvider context not yet initialized" }

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

    private val clock: Clock by lazy {
        applicationEntryPoint.provideClock()
    }

    private val applicationEntryPoint: TogglesProviderEntryPoint by lazy {
        entryPointBuilder.build(requireContext)
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
        fun provideClock(): Clock
    }

    // Guards only the rare first-touch initialisation (create application row + its scopes) so that
    // it happens exactly once per calling app. Steady-state reads acquire no lock — see
    // getCallingApplication().
    private val initLock = Any()

    private fun getCallingApplication(applicationDao: ProviderApplicationDao): TogglesApplication {
        val callingPackageName = packageManagerWrapper.callingApplicationPackageName

        // Fast path: the calling app is already registered. No lock needed for a read.
        applicationDao.loadByPackageName(callingPackageName)?.let { return it }

        // Slow path: first time we've seen this calling app. Serialise creation so concurrent
        // first-touch callers don't insert duplicate applications/scopes.
        return synchronized(initLock) {
            // Re-check: another thread may have created it while we waited for the lock.
            applicationDao.loadByPackageName(callingPackageName)?.let { return@synchronized it }

            val togglesApplication = TogglesApplication(
                id = 0,
                packageName = callingPackageName,
                applicationLabel = packageManagerWrapper.applicationLabel,
                shortcutId = callingPackageName,
            )

            togglesApplication.id = applicationDao.insert(togglesApplication)

            createDefaultScope(scopeDao, togglesApplication.id, clock)
            createDevelopmentScope(scopeDao, togglesApplication.id, clock)

            requireContext.contentResolver.notifyInsert(
                TogglesProviderContract.scopeUri()
            )

            togglesApplication
        }
    }

    private fun scopeChainFor(applicationId: Long): ScopeChain = ScopeChain(
        selectedScopeId = getSelectedScope(scopeDao, applicationId).id,
        defaultScopeId = getDefaultScope(scopeDao, applicationId).id
    )

    private fun firstNonEmptyCursor(chain: ScopeChain, query: (Long) -> Cursor): Cursor {
        var lastEmpty: Cursor? = null
        chain.orderedScopeIds.forEach { scopeId ->
            lastEmpty?.close()
            val candidate = query(scopeId)
            if (candidate.count > 0) return candidate
            lastEmpty = candidate
        }
        return requireNotNull(lastEmpty) { "scope chain is never empty" }
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
                val configId = requireNotNull(uri.lastPathSegment).toLong()
                cursor = firstNonEmptyCursor(scopeChainFor(callingApplication.id)) { scopeId ->
                    configurationDao.getToggle(configId, scopeId)
                }
            }

            UriMatch.CURRENT_CONFIGURATION_KEY -> {
                val key = requireNotNull(uri.lastPathSegment)
                cursor = firstNonEmptyCursor(scopeChainFor(callingApplication.id)) { scopeId ->
                    configurationDao.getToggle(key, scopeId)
                }
            }

            UriMatch.CONFIGURATIONS -> {
                cursor = configurationDao.getConfigurationCursor(callingApplication.id)
            }

            UriMatch.CONFIGURATION_KEY -> {
                val key = requireNotNull(uri.lastPathSegment)
                cursor = configurationDao.getConfigurationCursor(callingApplication.id, key)
            }

            UriMatch.CONFIGURATION_ID -> {
                val configId = requireNotNull(uri.lastPathSegment).toLong()
                cursor = configurationDao.getConfigurationCursor(callingApplication.id, configId)
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

            UriMatch.CONFIGURATION_VALUES -> {
                cursor = configurationDao.getConfigurationValueCursor(callingApplication.id)
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

        cursor.setNotificationUri(requireContext.contentResolver, uri)

        return cursor
    }

    private fun isTogglesApplication(callingApplication: TogglesApplication): Boolean {
        return callingApplication.packageName == requireContext.packageName
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(uri)
        }

        val contentValues = requireNotNull(values) { "ContentValues required for insert" }
        val insertId: Long
        var crossNotifyUri: Uri? = null
        var notify = true
        when (togglesUriMatcher.match(uri)) {
            UriMatch.CURRENT_CONFIGURATIONS -> {
                val toggle = Toggle.fromContentValues(contentValues)

                var togglesConfiguration: se.eelde.toggles.database.TogglesConfiguration? =
                    configurationDao.getTogglesConfiguration(callingApplication.id, toggle.key)

                if (togglesConfiguration == null) {
                    togglesConfiguration =
                        se.eelde.toggles.database.TogglesConfiguration(
                            id = 0,
                            applicationId = callingApplication.id,
                            key = toggle.key,
                            type = toggle.type,
                            lastUse = clock.now(),
                        )

                    togglesConfiguration.id = configurationDao.insert(togglesConfiguration)
                }

                // configurationValue.value is NOT NULL at the database level (see
                // MIGRATION_11_12). A null toggle.value has nothing to persist: an absent row
                // resolves to the caller's compiled-in default exactly like a null-valued one
                // used to, so skipping the insert changes nothing a consumer observes.
                val toggleValue = toggle.value
                if (toggleValue != null) {
                    val defaultScope = getDefaultScope(scopeDao, callingApplication.id)

                    val togglesConfigurationValue = TogglesConfigurationValue(
                        0,
                        togglesConfiguration.id,
                        toggleValue,
                        defaultScope.id
                    )

                    @Suppress("SwallowedException")
                    try {
                        togglesConfigurationValue.id =
                            configurationValueDao.insertSync(togglesConfigurationValue)
                    } catch (e: SQLiteConstraintException) {
                        // this happens when the app is initially launched because many of many
                        // calls into assertValidApiVersion()
                    }
                }

                insertId = togglesConfiguration.id
                crossNotifyUri = TogglesProviderContract.configurationUri(insertId)
            }

            UriMatch.PREDEFINED_CONFIGURATION_VALUES -> {
                val fullConfig = TogglesPredefinedConfigurationValue.fromContentValues(contentValues)
                insertId = try {
                    predefinedConfigurationDao.insert(fullConfig)
                } catch (_: SQLiteConstraintException) {
                    predefinedConfigurationDao.getByConfigurationAndValueId(
                        fullConfig.configurationId,
                        requireNotNull(fullConfig.value) { "Predefined configuration value cannot be null" }
                    ).id
                }
            }

            UriMatch.CONFIGURATIONS -> {
                val togglesConfiguration = TogglesConfiguration.fromContentValues(contentValues)
                val databaseConfiguration = se.eelde.toggles.database.TogglesConfiguration(
                    id = togglesConfiguration.id,
                    applicationId = callingApplication.id,
                    key = togglesConfiguration.key,
                    type = togglesConfiguration.type,
                    lastUse = clock.now(),
                )
                insertId = try {
                    configurationDao.insert(databaseConfiguration)
                } catch (e: SQLiteConstraintException) {
                    configurationDao.getTogglesConfiguration(callingApplication.id, togglesConfiguration.key)?.id
                        ?: throw e
                }
                crossNotifyUri = TogglesProviderContract.toggleUri(insertId)
            }

            UriMatch.CONFIGURATION_VALUE_ID -> {
                val togglesConfigurationValue =
                    se.eelde.toggles.core.TogglesConfigurationValue.fromContentValues(contentValues)
                // A constraint violation here means one of two things, and only a lookup can tell
                // them apart: (a) the (configurationId, scope) pair already has a row — an upsert,
                // nothing was written, so no id was freshly created and observers should not be
                // woken; or (b) the row references a scope that does not exist (its FK to
                // scope(id) — see MIGRATION_9_10) — a genuine failure, nothing was written and
                // there is no id to report. Only case (a) has an id to fall back to.
                //
                // A null value falls into the same "attempt nothing, fall back to lookup" shape:
                // configurationValue.value is NOT NULL at the database level (see
                // MIGRATION_11_12), so rather than let insertSync fail on that constraint - which
                // a caught SQLiteConstraintException could not tell apart from cases (a) or (b) -
                // the insert is skipped outright and freshId stays null.
                val value = togglesConfigurationValue.value

                @Suppress("SwallowedException")
                val freshId = if (value == null) {
                    null
                } else {
                    val databaseConfigurationValue = TogglesConfigurationValue(
                        id = togglesConfigurationValue.id,
                        configurationId = togglesConfigurationValue.configurationId,
                        value = value,
                        scope = togglesConfigurationValue.scope
                    )
                    try {
                        configurationValueDao.insertSync(databaseConfigurationValue)
                    } catch (e: SQLiteConstraintException) {
                        null
                    }
                }
                insertId = freshId ?: configurationValueDao.getIdByConfigurationIdAndScope(
                    togglesConfigurationValue.configurationId,
                    togglesConfigurationValue.scope
                ) ?: return null
                notify = freshId != null
                val configId = uri.pathSegments[uri.pathSegments.size - 2].toLong()
                crossNotifyUri = TogglesProviderContract.toggleUri(configId)
            }

            else -> {
                throw UnsupportedOperationException("Not yet implemented $uri")
            }
        }

        if (notify) {
            requireContext.contentResolver.notifyInsert(Uri.withAppendedPath(uri, insertId.toString()))
            crossNotifyUri?.let { requireContext.contentResolver.notifyInsert(it) }
        }

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

        val contentValues = requireNotNull(values) { "ContentValues required for update" }
        val (updatedRows, crossNotifyUri) = performUpdate(uri, contentValues, callingApplication)

        if (updatedRows > 0) {
            requireContext.contentResolver.notifyUpdate(uri)
            requireContext.contentResolver.notifyUpdate(crossNotifyUri)
        }

        return updatedRows
    }

    private fun performUpdate(
        uri: Uri,
        contentValues: ContentValues,
        callingApplication: TogglesApplication
    ): Pair<Int, Uri> = when (togglesUriMatcher.match(uri)) {
        UriMatch.CURRENT_CONFIGURATION_ID -> {
            val toggle = Toggle.fromContentValues(contentValues)
            val scope = getSelectedScope(scopeDao, callingApplication.id)
            val configId = requireNotNull(uri.lastPathSegment).toLong()
            val toggleValue = requireNotNull(toggle.value) { "Toggle value required for update" }
            val updatedCount = configurationValueDao.updateConfigurationValueSync(
                configId,
                scope.id,
                toggleValue
            )

            val updatedRows = if (updatedCount > 0) {
                updatedCount
            } else {
                val togglesConfigurationValue = TogglesConfigurationValue(
                    0,
                    configId,
                    toggleValue,
                    scope.id
                )
                val insertSync = configurationValueDao.insertSync(togglesConfigurationValue)
                if (insertSync > 0) 1 else 0
            }
            updatedRows to TogglesProviderContract.configurationUri(configId)
        }

        UriMatch.CONFIGURATION_ID -> {
            val fromContentValues = TogglesConfiguration.fromContentValues(contentValues)
            val id = requireNotNull(uri.lastPathSegment).toLong()
            val updatedRows = configurationDao.updateConfiguration(
                callingApplication = callingApplication.id,
                id = id,
                key = fromContentValues.key,
                type = fromContentValues.type
            )
            updatedRows to TogglesProviderContract.toggleUri(id)
        }

        UriMatch.CONFIGURATION_VALUE_ID -> {
            val togglesConfigurationValue =
                se.eelde.toggles.core.TogglesConfigurationValue.fromContentValues(contentValues)

            val updatedRows = togglesConfigurationValue.value?.let {
                configurationValueDao.updateConfigurationValueSync(
                    configurationId = togglesConfigurationValue.configurationId,
                    scopeId = togglesConfigurationValue.scope,
                    value = it
                )
            } ?: throw NullPointerException("Configuration value cannot be null")
            val configId = uri.pathSegments[uri.pathSegments.size - 2].toLong()
            updatedRows to TogglesProviderContract.toggleUri(configId)
        }

        else -> throw UnsupportedOperationException("Not yet implemented $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val callingApplication = getCallingApplication(applicationDao)

        if (!isTogglesApplication(callingApplication)) {
            assertValidApiVersion(uri)
        }

        when (togglesUriMatcher.match(uri)) {
            UriMatch.CONFIGURATION_ID -> {
                val configId = requireNotNull(uri.lastPathSegment).toLong()
                val deletedRows =
                    configurationDao.deleteConfiguration(callingApplication.id, configId)
                if (deletedRows > 0) {
                    requireContext.contentResolver.notifyChange(uri, null)
                    requireContext.contentResolver.notifyChange(
                        TogglesProviderContract.toggleUri(configId),
                        null
                    )
                }
                return deletedRows
            }

            UriMatch.CONFIGURATION_KEY -> {
                val key = requireNotNull(uri.lastPathSegment)
                val deletedRows =
                    configurationDao.deleteConfiguration(callingApplication.id, key)
                if (deletedRows > 0) {
                    requireContext.contentResolver.notifyChange(uri, null)
                    requireContext.contentResolver.notifyChange(
                        TogglesProviderContract.toggleUri(key),
                        null
                    )
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
            UriMatch.CONFIGURATION_VALUES -> "vnd.android.cursor.dir/vnd.$packageName.configurationValue"
            UriMatch.SCOPES ->
                "vnd.android.cursor.dir/vnd.$packageName.scope"

            UriMatch.UNKNOWN -> TODO()
        }
    }

    companion object {

        private fun getDefaultScope(
            scopeDao: ProviderScopeDao,
            applicationId: Long
        ): TogglesScope = scopeDao.getDefaultScope(applicationId)
            ?: error("No default scope for application $applicationId")

        private fun getSelectedScope(
            scopeDao: ProviderScopeDao,
            applicationId: Long
        ): TogglesScope = scopeDao.getSelectedScope(applicationId)
            ?: error("No selected scope for application $applicationId")

        // Scope construction (names, timestamps) lives on TogglesScope's companion object in
        // modules/database/implementation so the agent API's on-demand application creation
        // (AgentCallHandler.resolveOrCreateApplication) produces identical scopes without
        // duplicating this logic.
        private fun createDefaultScope(
            scopeDao: ProviderScopeDao,
            applicationId: Long,
            clock: Clock,
        ): TogglesScope {
            val scope = TogglesScope.defaultScope(applicationId, clock)
            scope.id = scopeDao.insert(scope)
            return scope
        }

        private fun createDevelopmentScope(
            scopeDao: ProviderScopeDao,
            applicationId: Long,
            clock: Clock,
        ): TogglesScope {
            val developmentScope = TogglesScope.developmentScope(applicationId, clock)
            developmentScope.id = scopeDao.insert(developmentScope)
            return developmentScope
        }

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
