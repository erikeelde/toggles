package se.eelde.toggles.flow

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.ToggleScope
import se.eelde.toggles.core.ToggleState
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import se.eelde.toggles.core.TogglesProviderContract.configurationUri
import se.eelde.toggles.core.TogglesProviderContract.configurationValueUri
import se.eelde.toggles.core.TogglesProviderContract.scopeUri
import se.eelde.toggles.core.TogglesProviderContract.toggleUri

@Suppress("TooManyFunctions")
internal class TogglesProvider(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val contentResolver: ContentResolver = context.applicationContext.contentResolver
    private val providerAvailable: Boolean = configurationUri().authority?.let { authority ->
        context.applicationContext.packageManager.resolveContentProvider(authority, 0)
    } != null

    // region Query

    suspend fun getToggleState(key: String): ToggleState = withContext(dispatcher) {
        val scopes = queryScopes()
        if (scopes.isEmpty()) {
            return@withContext ToggleState(null, emptyList(), emptyList())
        }

        val configuration = queryConfiguration(key)
            ?: return@withContext ToggleState(null, emptyList(), scopes)

        val configurationValues = queryConfigurationValues(configuration.id)

        ToggleState(configuration, configurationValues, scopes)
    }

    fun getDefaultScope(scopes: List<ToggleScope>): ToggleScope? =
        scopes.firstOrNull { it.name == ColumnNames.ToggleScope.DEFAULT_SCOPE }

    // Returns the actively selected scope (highest timestamp). This may be the default scope
    // when no other scope has been explicitly selected by the user.
    fun getSelectedScope(scopes: List<ToggleScope>): ToggleScope? =
        scopes.maxByOrNull { it.timeStamp }

    fun getConfigurationValueForScope(
        scopeId: Long,
        configurationValues: List<TogglesConfigurationValue>,
    ): TogglesConfigurationValue? = configurationValues.find { it.scope == scopeId }

    // endregion

    // region Mutations

    fun insertConfiguration(key: String, type: String): Long? {
        val configuration = TogglesConfiguration.Builder()
            .setId(0)
            .setType(type)
            .setKey(key)
            .build()
        val uri = contentResolver.insert(configurationUri(), configuration.toContentValues())
        return uri?.lastPathSegment?.toLongOrNull()
    }

    fun insertConfigurationValue(configId: Long, value: String, scopeId: Long) {
        val configurationValue = TogglesConfigurationValue.Builder()
            .setId(0)
            .setConfigurationId(configId)
            .setValue(value)
            .setScope(scopeId)
            .build()
        contentResolver.insert(
            configurationValueUri(configId),
            configurationValue.toContentValues()
        )
    }

    fun updateConfigurationValue(configId: Long, configurationValue: TogglesConfigurationValue, newValue: String) {
        contentResolver.update(
            configurationValueUri(configId),
            configurationValue.copy(value = newValue).toContentValues(),
            null,
            null
        )
    }

    fun insertPredefinedValues(configurationId: Long, values: List<String>) {
        for (value in values) {
            contentResolver.insert(
                se.eelde.toggles.core.TogglesProviderContract.toggleValueUri(),
                se.eelde.toggles.core.ToggleValue {
                    this.configurationId = configurationId
                    this.value = value
                }.toContentValues()
            )
        }
    }

    // endregion

    // region Observation

    fun observeToggleState(key: String): Flow<ToggleState> = callbackFlow {
        val observer = object : ContentObserver(Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                onChange(selfChange, null)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                launch { trySend(getToggleState(key)) }
            }
        }

        if (providerAvailable) {
            contentResolver.registerContentObserver(
                configurationUri(),
                true,
                observer
            )
            // Also observe the legacy toggleUri — the Toggles app notifies on
            // currentConfiguration/{id} when values change through its UI.
            contentResolver.registerContentObserver(
                toggleUri(),
                true,
                observer
            )
            contentResolver.registerContentObserver(
                scopeUri(),
                true,
                observer
            )
        }

        trySend(getToggleState(key))

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }

    // endregion

    // region Private query helpers

    private fun queryScopes(): List<ToggleScope> {
        val scopes = mutableListOf<ToggleScope>()
        val cursor = contentResolver.query(scopeUri(), null, null, null, null)
        if (cursor == null) {
            return emptyList()
        }
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    scopes.add(ToggleScope.fromCursor(it))
                } while (it.moveToNext())
            }
        }
        return scopes
    }

    @Suppress("ReturnCount")
    private fun queryConfiguration(key: String): TogglesConfiguration? {
        val cursor = contentResolver.query(configurationUri(key), null, null, null, null)
            ?: return null
        cursor.use {
            if (it.count == 0) return null
            if (it.count > 1) error("Multiple configurations found for key: $key")
            if (!it.moveToFirst()) error("Could not move to first in configuration cursor for key: $key")
            return TogglesConfiguration.fromCursor(it)
        }
    }

    private fun queryConfigurationValues(configId: Long): List<TogglesConfigurationValue> {
        val values = mutableListOf<TogglesConfigurationValue>()
        val cursor = contentResolver.query(
            configurationValueUri(configId), null, null, null, null
        ) ?: return emptyList()
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    values.add(TogglesConfigurationValue.fromCursor(it))
                } while (it.moveToNext())
            }
        }
        return values
    }

    // endregion
}
