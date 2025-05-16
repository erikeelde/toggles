package se.eelde.toggles.flow

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.Toggle.Companion.fromCursor
import se.eelde.toggles.core.Toggle.ToggleType
import se.eelde.toggles.core.ToggleValue
import se.eelde.toggles.core.TogglesProviderContract.toggleUri
import se.eelde.toggles.core.TogglesProviderContract.toggleValueUri

@Suppress("LibraryEntitiesShouldNotBePublic")
public class TogglesImpl(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : Toggles {
    private val context = context.applicationContext
    private val contentResolver = this.context.contentResolver

    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> =
        providerToggleFlow(context, Toggle.TYPE.BOOLEAN, key)
            .map { toggle ->
                if (toggle == null) {
                    defaultValue
                } else if (toggle.id == 0L) {
                    contentResolver.insert(
                        toggleUri(),
                        toggle.copy(value = defaultValue.toString()).toContentValues()
                    )
                    defaultValue
                } else {
                    when (val value = toggle.value) {
                        null -> defaultValue
                        else -> value.toBoolean()
                    }
                }
            }

    override fun toggle(key: String, defaultValue: Int): Flow<Int> =
        providerToggleFlow(context, Toggle.TYPE.INTEGER, key)
            .map { toggle ->
                if (toggle == null) {
                    defaultValue
                } else if (toggle.id == 0L) {
                    contentResolver.insert(
                        toggleUri(),
                        toggle.copy(value = defaultValue.toString()).toContentValues()
                    )
                    defaultValue
                } else {
                    when (val value = toggle.value) {
                        null -> defaultValue
                        else -> value.toInt()
                    }
                }
            }

    override fun toggle(key: String, defaultValue: String): Flow<String> =
        providerToggleFlow(context, Toggle.TYPE.STRING, key)
            .map { toggle ->
                if (toggle == null) {
                    defaultValue
                } else if (toggle.id == 0L) {
                    contentResolver.insert(
                        toggleUri(),
                        toggle.copy(value = defaultValue).toContentValues()
                    )
                    defaultValue
                } else {
                    when (val value = toggle.value) {
                        null -> defaultValue
                        else -> value
                    }
                }
            }

    @ExperimentalCoroutinesApi
    override fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T> =
        providerToggleFlow(context, Toggle.TYPE.ENUM, key)
            .map { toggle ->
                if (toggle == null) {
                    defaultValue
                } else if (toggle.id == 0L) {
                    val uri = contentResolver.insert(
                        toggleUri(),
                        toggle.copy(value = defaultValue.toString()).toContentValues()
                    )
                    val configurationId = uri?.lastPathSegment?.toLong() ?: return@map defaultValue

                    for (enumConstant in type.enumConstants!!) {
                        contentResolver.insert(
                            toggleValueUri(),
                            ToggleValue {
                                this.configurationId = configurationId
                                value = enumConstant.toString()
                            }.toContentValues()
                        )
                    }
                    defaultValue
                } else {
                    when (val value = toggle.value) {
                        null -> defaultValue
                        else -> java.lang.Enum.valueOf(type, value)
                    }
                }
            }

    private fun providerToggleFlow(
        context: Context,
        @ToggleType type: String,
        key: String
    ): Flow<Toggle?> = callbackFlow {
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
    ): Toggle? =
        withContext(ioDispatcher) {
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(
                    toggleUri(key),
                    null,
                    null,
                    null,
                    null
                )
                if (cursor == null) {
                    return@withContext null
                }
                if (cursor.moveToFirst()) {
                    val mutableList = mutableListOf<Toggle>()
                    while (cursor.moveToNext()) {
                        mutableList.add(fromCursor(cursor))
                    }
                    Log.w("TogglesImpl", mutableList.toString())

                    cursor.moveToFirst()
                    return@withContext fromCursor(cursor)
                }
            } finally {
                if (cursor != null && !cursor.isClosed) {
                    cursor.close()
                }
            }

            return@withContext Toggle {
                id = 0L
                this.type = type
                this.key = key
                value = ""
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
