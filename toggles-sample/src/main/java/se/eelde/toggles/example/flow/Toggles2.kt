package se.eelde.toggles.example.flow

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.Toggle.ToggleType
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import se.eelde.toggles.core.TogglesProviderContract.configurationUri
import se.eelde.toggles.core.TogglesProviderContract.configurationValueUri
import se.eelde.toggles.core.TogglesProviderContract.toggleUri

class Toggles2(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val context = context.applicationContext
    private val contentResolver = this.context.contentResolver
//
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
//
//    fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> = TODO()

    fun toggle(key: String): Flow<List<String>> =
        providerToggleFlow(context, Toggle.TYPE.STRING, key)
            .map { toggle: List<Toggle> ->
                toggle.map { it.value!! }
            }
//
//    fun toggle(key: String, defaultValue: Int): Flow<Int> = TODO()
//
//    fun <T : Enum<T>> toggle(
//        key: String,
//        type: Class<T>,
//        defaultValue: T
//    ): Flow<T> = TODO()


    private fun providerToggleFlow(
        context: Context,
        @ToggleType type: String,
        key: String
    ): Flow<List<Toggle>> = callbackFlow {
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

    private suspend fun getToggle(
        contentResolver: ContentResolver,
        @ToggleType type: String,
        key: String
    ): List<Toggle> =
        withContext(ioDispatcher) {
            contentResolver.query(
                configurationUri(key),
                null,
                null,
                null,
                null
            ).use { configurationCursor ->
                configurationCursor!!.moveToFirst()
                val configuration = TogglesConfiguration.fromCursor(configurationCursor)

                contentResolver.query(
                    configurationValueUri(configuration.id),
                    null,
                    null,
                    null,
                    null
                ).use { configurationValuesCursor ->
                    if (configurationValuesCursor!!.moveToFirst()) {
                        val configurationValues = mutableListOf<TogglesConfigurationValue>()
                        do {
                            configurationValues.add(
                                TogglesConfigurationValue.fromCursor(
                                    configurationValuesCursor
                                )
                            )
                        } while (configurationValuesCursor.moveToNext())
                        configurationValues.forEach {
                            val mutableList = mutableListOf<Toggle>()
                            do {
                                mutableList.add(Toggle {
                                    setId(0)
                                    setKey(configuration.key)
                                    setType(configuration.type)
                                    setValue(it.value)
                                })
                            } while (configurationCursor.moveToNext())
                            return@withContext mutableList
                        }
                    }
                }

                return@withContext listOf(Toggle {
                    id = 0L
                    this.type = type
                    this.key = key
                    value = ""
                })
            }
        }

    private class ToggleContentObserver(
        handler: Handler?,
        private val changeCallback: (uri: Uri?) -> Unit
    ) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            this.onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            changeCallback.invoke(uri)
        }
    }
}

