package se.eelde.toggles.flow

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
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

class TogglesImpl(context: Context) : Toggles {
    private val context = context.applicationContext
    private val contentResolver = this.context.contentResolver

    @ExperimentalCoroutinesApi
    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> =
        providerToggleFlow(context, Toggle.TYPE.BOOLEAN, key)
            .map { toggle ->
                when {
                    toggle == null -> defaultValue
                    toggle.id == 0L -> {
                        contentResolver.insert(
                            toggleUri(),
                            toggle.copy(value = defaultValue.toString()).toContentValues()
                        )
                        defaultValue
                    }
                    toggle.value == null -> defaultValue
                    else -> toggle.value!!.toBoolean()
                }
            }

    @ExperimentalCoroutinesApi
    override fun toggle(key: String, defaultValue: Int): Flow<Int> =
        providerToggleFlow(context, Toggle.TYPE.INTEGER, key)
            .map { toggle ->
                when {
                    toggle == null -> defaultValue
                    toggle.id == 0L -> {
                        contentResolver.insert(
                            toggleUri(),
                            toggle.copy(value = defaultValue.toString()).toContentValues()
                        )
                        defaultValue
                    }
                    toggle.value == null -> defaultValue
                    else -> toggle.value!!.toInt()
                }
            }

    @ExperimentalCoroutinesApi
    override fun toggle(key: String, defaultValue: String): Flow<String> =
        providerToggleFlow(context, Toggle.TYPE.STRING, key)
            .map { toggle ->
                when {
                    toggle == null -> defaultValue
                    toggle.id == 0L -> {
                        contentResolver.insert(
                            toggleUri(),
                            toggle.copy(value = defaultValue).toContentValues()
                        )
                        defaultValue
                    }
                    toggle.value == null -> defaultValue
                    else -> toggle.value!!
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
                when {
                    toggle == null -> defaultValue
                    toggle.id == 0L -> {
                        val uri = contentResolver.insert(
                            toggleUri(),
                            toggle.copy(value = defaultValue.toString()).toContentValues()
                        )
                        val configurationId = uri!!.lastPathSegment!!.toLong()

                        for (enumConstant in type.enumConstants!!) {
                            contentResolver.insert(
                                toggleValueUri(),
                                ToggleValue(
                                    configurationId = configurationId,
                                    value = enumConstant.toString()
                                ).toContentValues()
                            )
                        }
                        defaultValue
                    }
                    toggle.value == null -> defaultValue
                    else -> java.lang.Enum.valueOf(type, toggle.value!!)
                }
            }

    @ExperimentalCoroutinesApi
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

        val providerInfo =
            context.packageManager.resolveContentProvider(
                toggleUri().authority!!,
                0
            )
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
        withContext(Dispatchers.IO) {
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
                    return@withContext fromCursor(cursor)
                }
            } finally {
                if (cursor != null && !cursor.isClosed) {
                    cursor.close()
                }
            }
            return@withContext Toggle(0L, type, key, "")
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
