package se.eelde.toggles.coroutines

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
import se.eelde.toggles.core.Bolt
import se.eelde.toggles.core.Bolt.BoltType
import se.eelde.toggles.core.Bolt.Companion.fromCursor
import se.eelde.toggles.core.Nut
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.core.TogglesProviderContract.boltUri
import se.eelde.toggles.core.showDownloadNotification

@ExperimentalCoroutinesApi
fun booleanBoltFlow(context: Context, key: String, defaultValue: Boolean = true): Flow<Boolean> =
    boltFlow(context, Bolt.TYPE.BOOLEAN, key)
        .map { bolt ->
            when {
                bolt == null -> defaultValue
                bolt.id == 0L -> {
                    context.contentResolver.insert(boltUri(), bolt.copy(bolt.id, bolt.type, key, defaultValue.toString()).toContentValues())
                    defaultValue
                }
                bolt.value == null -> defaultValue
                else -> bolt.value.toBoolean()
            }
        }

@ExperimentalCoroutinesApi
fun stringBoltFlow(context: Context, key: String, defaultValue: String = ""): Flow<String> =
    boltFlow(context, Bolt.TYPE.STRING, key)
        .map { bolt ->
            when {
                bolt == null -> defaultValue
                bolt.id == 0L -> {
                    context.contentResolver.insert(boltUri(), bolt.copy(bolt.id, bolt.type, key, defaultValue).toContentValues())
                    defaultValue
                }
                bolt.value == null -> defaultValue
                else -> bolt.value!!
            }
        }

@ExperimentalCoroutinesApi
fun integerBoltFlow(context: Context, key: String, defaultValue: Int = 0): Flow<Int> =
    boltFlow(context, Bolt.TYPE.INTEGER, key)
        .map { bolt ->
            when {
                bolt == null -> defaultValue
                bolt.id == 0L -> {
                    context.contentResolver.insert(boltUri(), bolt.copy(bolt.id, bolt.type, key, defaultValue.toString()).toContentValues())
                    defaultValue
                }
                bolt.value == null -> defaultValue
                else -> bolt.value!!.toInt()
            }
        }

@ExperimentalCoroutinesApi
fun <T : Enum<T>> enumBoltFlow(context: Context, key: String, type: Class<T>, defaultValue: T): Flow<T> =
    boltFlow(context, Bolt.TYPE.ENUM, key)
        .map { bolt ->
            when {
                bolt == null -> defaultValue
                bolt.id == 0L -> {
                    val uri = context.contentResolver.insert(boltUri(), bolt.copy(bolt.id, bolt.type, key, defaultValue.toString()).toContentValues())
                    val configurationId = uri!!.lastPathSegment!!.toLong()

                    for (enumConstant in type.enumConstants!!) {
                        val nut = Nut(configurationId = configurationId, value = enumConstant.toString())
                        context.contentResolver.insert(TogglesProviderContract.nutUri(), nut.toContentValues())
                    }
                    defaultValue
                }
                bolt.value == null -> defaultValue
                else -> java.lang.Enum.valueOf(type, bolt.value!!)
            }
        }

@ExperimentalCoroutinesApi
fun boltFlow(context: Context, @BoltType type: String, key: String): Flow<Bolt?> = callbackFlow {

    val boltContentObserver = BoltContentObserver(null) {
        launch {
            offer(getBolt(context.contentResolver, type, key))
        }
    }

    val providerInfo = context.packageManager.resolveContentProvider(TogglesProviderContract.TOGGLES_AUTHORITY, 0)
    if (providerInfo != null) {
        context.contentResolver
            .registerContentObserver(boltUri(), true, boltContentObserver)
    } else {
        showDownloadNotification(context = context)
    }

    offer(getBolt(context.contentResolver, type, key))

    awaitClose {
        context.contentResolver.unregisterContentObserver(boltContentObserver)
    }
}

private suspend fun getBolt(contentResolver: ContentResolver, @BoltType type: String, key: String): Bolt? = withContext(Dispatchers.IO) {
    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(
            boltUri(key),
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
    return@withContext Bolt(0L, type, key, "")
}

class BoltContentObserver(handler: Handler?, private val changeCallback: (uri: Uri?) -> Unit) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        this.onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        changeCallback.invoke(uri)
    }
}
