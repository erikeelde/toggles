package se.eelde.toggles.example.toggles2

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.collections.immutable.ImmutableList
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
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import se.eelde.toggles.core.TogglesProviderContract.configurationUri
import se.eelde.toggles.core.TogglesProviderContract.configurationValueUri
import se.eelde.toggles.core.TogglesProviderContract.scopeUri
import se.eelde.toggles.core.TogglesProviderContract.toggleUri

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

//    fun toggleHasOverride(): Flow<Boolean> = TODO()
//
//    fun setDefaultValue(key: String, defaultValue: String): Unit = TODO()
//
//    fun setDefaultValue(key: String, defaultValue: Boolean): Unit = TODO()
//
//    fun setDefaultValue(key: String, defaultValue: Int): Unit = TODO()
//
//    fun <T : Enum<T>> setDefaultValue(
//        key: String,
//        type: Class<T>,
//        defaultValue: T
//    ): Unit = TODO()

    @Suppress("unused")
    fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T> = TODO()

    @Suppress("unused", "UnsafeCallOnNullableType")
    fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> =
        providerToggleFlow(context, Toggle.TYPE.BOOLEAN, key)
            .map { toggle: WrappedObject ->
                toggle.configurationValues.last().value!!.toBoolean()
            }

    @Suppress("unused", "UnsafeCallOnNullableType")
    fun toggle(key: String, defaultValue: Int): Flow<Int> =
        providerToggleFlow(context, Toggle.TYPE.INTEGER, key)
            .map { toggle: WrappedObject ->
                toggle.configurationValues.last().value!!.toInt()
            }

    @Suppress("UnsafeCallOnNullableType")
    fun toggle(key: String, defaultValue: String): Flow<String> {
        return providerToggleFlow(context, Toggle.TYPE.STRING, key)
            .map { wrappedObject: WrappedObject ->
                val defaultScope = getDefaultScope(wrappedObject.scopes)

                if (wrappedObject.configuration == null) {
                    Log.e("Toggles2", "No configuration for '$key' created")

                    val configurationUri =
                        insertConfiguration(contentResolver, key, Toggle.TYPE.STRING)
                    insertConfigurationValue(
                        contentResolver,
                        configurationUri!!.lastPathSegment!!.toLong(),
                        defaultValue,
                        defaultScope.id
                    )
                    return@map defaultValue
                } else {
                    val selectedScope = getSelectedScope(wrappedObject.scopes)

                    val currentConfigurationValue =
                        getConfigurationValueForScope(
                            selectedScope,
                            wrappedObject.configurationValues
                        )

                    val defaultConfigurationValue =
                        getConfigurationValueForScope(
                            defaultScope,
                            wrappedObject.configurationValues
                        )!!

                    if (defaultConfigurationValue.value != defaultValue) {
                        updateConfigurationValue(
                            contentResolver,
                            defaultConfigurationValue,
                            defaultValue
                        )
                    }

                    if (currentConfigurationValue != null) {
                        return@map currentConfigurationValue.value!!
                    } else {
                        Log.e("Toggles2", "No scope override for '$key' found")
                        return@map defaultConfigurationValue.value!!
                    }
                }
            }
    }

    private fun insertConfiguration(
        contentResolver: ContentResolver,
        key: String,
        @Toggle.ToggleType type: String
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
        val configuration = TogglesConfigurationValue {
            setId(0)
            setConfigurationId(configurationId)
            setValue(value)
            setScope(scope)
        }
        contentResolver.insert(
            configurationUri(configurationId),
            configuration.toContentValues()
        )
    }

    private fun getDefaultScope(scopes: ImmutableList<ToggleScope>): ToggleScope =
        scopes.first { it.name == ColumnNames.ToggleScope.DEFAULT_SCOPE }

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
        defaultConfigurationValue: TogglesConfigurationValue,
        defaultValue: String
    ) {
        contentResolver.update(
            configurationValueUri(defaultConfigurationValue.id),
            defaultConfigurationValue.copy(value = defaultValue).toContentValues(),
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

        trySend(getToggle(contentResolver, type, key))

        awaitClose {
            contentResolver.unregisterContentObserver(toggleContentObserver)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private suspend fun getToggle(
        contentResolver: ContentResolver,
        @Suppress("unused")
        @ToggleType type: String,
        key: String
    ): WrappedObject =
        withContext(ioDispatcher) {
            val scopes = mutableListOf<ToggleScope>()

            contentResolver.query(
                scopeUri(),
                null,
                null,
                null,
                null
            ).use { scopesCursor ->
                scopesCursor!!.moveToFirst()
                do {
                    scopes.add(ToggleScope.fromCursor(scopesCursor))
                } while (scopesCursor.moveToNext())
            }

            contentResolver.query(
                configurationUri(key),
                null,
                null,
                null,
                null
            ).use { configurationCursor ->
                configurationCursor!!.moveToFirst()
                val configuration = TogglesConfiguration.fromCursor(configurationCursor)
                val configurationValues = mutableListOf<TogglesConfigurationValue>()

                contentResolver.query(
                    configurationValueUri(configuration.id),
                    null,
                    null,
                    null,
                    null
                ).use { configurationValuesCursor ->
                    if (configurationValuesCursor!!.moveToFirst()) {
                        do {
                            configurationValues.add(
                                TogglesConfigurationValue.fromCursor(
                                    configurationValuesCursor
                                )
                            )
                        } while (configurationValuesCursor.moveToNext())

                        return@withContext WrappedObject(
                            configuration,
                            configurationValues.toImmutableList(),
                            scopes.toImmutableList()
                        )
                    }
                }

                return@withContext WrappedObject(
                    configuration,
                    configurationValues.toImmutableList(),
                    scopes.toImmutableList()
                )
            }
        }
}
