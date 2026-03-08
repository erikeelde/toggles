package se.eelde.toggles.example.toggles2

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.Toggle.ToggleType
import se.eelde.toggles.core.ToggleScope
import se.eelde.toggles.core.ToggleValue
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import se.eelde.toggles.core.TogglesProviderContract.configurationUri
import se.eelde.toggles.core.TogglesProviderContract.configurationValueUri
import se.eelde.toggles.core.TogglesProviderContract.scopeUri
import se.eelde.toggles.core.TogglesProviderContract.toggleUri
import se.eelde.toggles.core.TogglesProviderContract.toggleValueUri

// Open question: should WrappedObject be an internal detail or part of the public API?
// Currently it's a data class used to bundle provider query results. If Toggles2 moves to a
// published library, consider whether consumers need access to the raw configuration/scope data
// or if this should be encapsulated behind the toggle() methods.
data class WrappedObject(
    val configuration: TogglesConfiguration?,
    val configurationValues: ImmutableList<TogglesConfigurationValue>,
    val scopes: ImmutableList<ToggleScope>,
)

@Suppress("TooManyFunctions")
class Toggles2(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val context = context.applicationContext
    private val contentResolver = this.context.contentResolver

    fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> =
        resolveToggleValue(key, Toggle.TYPE.BOOLEAN, defaultValue.toString())
            .map { it.toBoolean() }

    fun toggle(key: String, defaultValue: Int): Flow<Int> =
        resolveToggleValue(key, Toggle.TYPE.INTEGER, defaultValue.toString())
            .map { it.toInt() }

    fun toggle(key: String, defaultValue: String): Flow<String> =
        resolveToggleValue(key, Toggle.TYPE.STRING, defaultValue)

    fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T> =
        resolveToggleValue(key, Toggle.TYPE.ENUM, defaultValue.toString()) { configurationId ->
            for (enumConstant in type.enumConstants!!) {
                contentResolver.insert(
                    toggleValueUri(),
                    ToggleValue {
                        this.configurationId = configurationId
                        value = enumConstant.toString()
                    }.toContentValues()
                )
            }
        }.map { java.lang.Enum.valueOf(type, it) }

    /**
     * Core resolution logic shared by all toggle types.
     *
     * Observes the provider for changes and for each emission:
     * 1. If no configuration exists, creates it with the default value
     * 2. If configuration exists but has no default-scope value, inserts it
     * 3. If default-scope value differs from [defaultValue], updates it
     * 4. Returns the selected scope's value if present, otherwise the default scope's value
     *
     * @param onFirstCreate optional callback invoked after creating a new configuration,
     *   useful for inserting predefined values (e.g. enum constants)
     */
    private fun resolveToggleValue(
        key: String,
        @ToggleType type: String,
        defaultValue: String,
        onFirstCreate: ((configurationId: Long) -> Unit)? = null,
    ): Flow<String> =
        providerToggleFlow(context, type, key)
            .map { wrappedObject ->
                if (wrappedObject.scopes.isEmpty()) {
                    Log.w(TAG, "No scopes available, returning default for '$key'")
                    return@map defaultValue
                }

                val defaultScope = getDefaultScope(wrappedObject.scopes)

                if (wrappedObject.configuration == null) {
                    Log.d(TAG, "No configuration for '$key', creating")
                    val configurationUri =
                        insertConfiguration(contentResolver, key, type)
                            ?: return@map defaultValue
                    val configurationId = configurationUri.lastPathSegment!!.toLong()
                    insertConfigurationValue(
                        contentResolver,
                        configurationId,
                        defaultValue,
                        defaultScope.id
                    )
                    onFirstCreate?.invoke(configurationId)
                    return@map defaultValue
                }

                val selectedScope = getSelectedScope(wrappedObject.scopes)

                val defaultConfigurationValue =
                    getConfigurationValueForScope(
                        defaultScope,
                        wrappedObject.configurationValues
                    )

                if (defaultConfigurationValue == null) {
                    insertConfigurationValue(
                        contentResolver,
                        wrappedObject.configuration.id,
                        defaultValue,
                        defaultScope.id
                    )
                } else if (defaultConfigurationValue.value != defaultValue) {
                    updateConfigurationValue(
                        contentResolver,
                        wrappedObject.configuration.id,
                        defaultConfigurationValue,
                        defaultValue
                    )
                }

                val selectedConfigurationValue =
                    getConfigurationValueForScope(
                        selectedScope,
                        wrappedObject.configurationValues
                    )

                if (selectedConfigurationValue != null) {
                    selectedConfigurationValue.value ?: defaultValue
                } else {
                    Log.d(TAG, "No scope override for '$key', using default")
                    defaultConfigurationValue?.value ?: defaultValue
                }
            }

    private fun insertConfiguration(
        contentResolver: ContentResolver,
        key: String,
        @ToggleType type: String
    ): Uri? {
        val configuration = TogglesConfiguration {
            setId(0)
            setType(type)
            setKey(key)
        }
        return contentResolver.insert(configurationUri(), configuration.toContentValues())
    }

    private fun insertConfigurationValue(
        contentResolver: ContentResolver,
        configurationId: Long,
        value: String,
        scope: Long,
    ) {
        val configurationValue = TogglesConfigurationValue {
            setId(0)
            setConfigurationId(configurationId)
            setValue(value)
            setScope(scope)
        }
        contentResolver.insert(
            configurationValueUri(configurationId),
            configurationValue.toContentValues()
        )
    }

    private fun getDefaultScope(scopes: ImmutableList<ToggleScope>): ToggleScope =
        scopes.firstOrNull { it.name == ColumnNames.ToggleScope.DEFAULT_SCOPE }
            ?: error("Default scope not found in the provided scopes list.")

    private fun getSelectedScope(scopes: ImmutableList<ToggleScope>): ToggleScope =
        scopes.maxBy { it.timeStamp }

    private fun getConfigurationValueForScope(
        scope: ToggleScope,
        configurationValues: ImmutableList<TogglesConfigurationValue>
    ): TogglesConfigurationValue? = configurationValues.find {
        it.scope == scope.id
    }

    private fun updateConfigurationValue(
        contentResolver: ContentResolver,
        configurationId: Long,
        configurationValue: TogglesConfigurationValue,
        newValue: String
    ) {
        contentResolver.update(
            configurationValueUri(configurationId),
            configurationValue.copy(value = newValue).toContentValues(),
            null,
            null
        )
    }

    private fun providerToggleFlow(
        context: Context,
        @ToggleType type: String,
        key: String
    ): Flow<WrappedObject> = callbackFlow {
        val toggleContentObserver = ToggleContentObserver(null) {
            launch {
                trySend(getToggle(contentResolver, type, key))
            }
        }

        val providerInfo = toggleUri().authority?.let { authority ->
            context.packageManager.resolveContentProvider(authority, 0)
        }

        if (providerInfo != null) {
            contentResolver
                .registerContentObserver(toggleUri(), true, toggleContentObserver)
        }

        val toggle = getToggle(contentResolver, type, key)
        trySend(toggle)

        awaitClose {
            contentResolver.unregisterContentObserver(toggleContentObserver)
        }
    }

    @Suppress("LongMethod")
    private suspend fun getToggle(
        contentResolver: ContentResolver,
        @Suppress("unused")
        @ToggleType type: String,
        key: String
    ): WrappedObject =
        withContext(ioDispatcher) {
            val scopes = mutableListOf<ToggleScope>()

            val scopesCursor = contentResolver.query(
                scopeUri(),
                null,
                null,
                null,
                null
            )
            if (scopesCursor == null) {
                Log.w(TAG, "Toggles provider not available")
                return@withContext WrappedObject(null, persistentListOf(), persistentListOf())
            }
            scopesCursor.use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        scopes.add(ToggleScope.fromCursor(cursor))
                    } while (cursor.moveToNext())
                } else {
                    Log.w(TAG, "No scopes found")
                    return@withContext WrappedObject(null, persistentListOf(), persistentListOf())
                }
            }

            val configurationCursor = contentResolver.query(
                configurationUri(key),
                null,
                null,
                null,
                null
            )
            if (configurationCursor == null || configurationCursor.count == 0) {
                configurationCursor?.close()
                Log.d(TAG, "No configuration found for key: $key")
                return@withContext WrappedObject(
                    null,
                    persistentListOf(),
                    scopes.toImmutableList()
                )
            }

            configurationCursor.use { cursor ->
                if (cursor.count > 1) {
                    error("Multiple configurations found for key: $key")
                }
                if (!cursor.moveToFirst()) {
                    error("Could not move to first in configuration cursor for key: $key")
                }

                val configuration = TogglesConfiguration.fromCursor(cursor)
                val configurationValues = mutableListOf<TogglesConfigurationValue>()

                val valuesCursor = contentResolver.query(
                    configurationValueUri(configuration.id),
                    null,
                    null,
                    null,
                    null
                )
                valuesCursor?.use { vc ->
                    if (vc.moveToFirst()) {
                        do {
                            configurationValues.add(
                                TogglesConfigurationValue.fromCursor(vc)
                            )
                        } while (vc.moveToNext())
                    }
                }

                return@withContext WrappedObject(
                    configuration,
                    configurationValues.toImmutableList(),
                    scopes.toImmutableList()
                )
            }
        }

    companion object {
        private const val TAG = "Toggles2"
    }
}
