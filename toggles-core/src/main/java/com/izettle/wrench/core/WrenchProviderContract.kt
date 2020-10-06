package com.izettle.wrench.core

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.annotation.StringDef

class ColumnNames {
    object Bolt {
        const val COL_KEY = "configurationKey"
        const val COL_ID = "id"
        const val COL_VALUE = "value"
        const val COL_TYPE = "configurationType"
    }

    object Nut {
        const val COL_ID = "id"
        const val COL_VALUE = "value"
        const val COL_CONFIG_ID = "configurationId"
    }
}

data class Nut(
    val id: Long = 0,
    val configurationId: Long = 0,
    val value: String? = null
) {

    constructor(configurationId: Long, value: String?) : this(0, configurationId, value)

    fun toContentValues(): ContentValues {
        val contentValues = ContentValues()
        if (id > 0) {
            contentValues.put(ColumnNames.Nut.COL_ID, id)
        }
        contentValues.put(ColumnNames.Nut.COL_CONFIG_ID, configurationId)
        contentValues.put(ColumnNames.Nut.COL_VALUE, value)

        return contentValues
    }
}

data class Bolt(
    var id: Long = 0,
    @BoltType val type: String,
    val key: String = "",
    val value: String? = null
) {

    fun toContentValues(): ContentValues {
        val contentValues = ContentValues()

        contentValues.put(ColumnNames.Bolt.COL_ID, id)
        contentValues.put(ColumnNames.Bolt.COL_KEY, key)
        contentValues.put(ColumnNames.Bolt.COL_VALUE, value)
        contentValues.put(ColumnNames.Bolt.COL_TYPE, type)

        return contentValues
    }

    @StringDef(TYPE.BOOLEAN, TYPE.STRING, TYPE.INTEGER, TYPE.ENUM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class BoltType

    object TYPE {
        const val BOOLEAN = "boolean"
        const val STRING = "string"
        const val INTEGER = "integer"
        const val ENUM = "enum"
    }

    companion object {

        @JvmStatic
        fun fromContentValues(values: ContentValues): Bolt {

            return Bolt(
                id = values.getAsLong(ColumnNames.Bolt.COL_ID) ?: 0,
                type = values.getAsString(ColumnNames.Bolt.COL_TYPE),
                key = values.getAsString(ColumnNames.Bolt.COL_KEY),
                value = values.getAsString(ColumnNames.Bolt.COL_VALUE)
            )
        }

        @JvmStatic
        fun fromCursor(cursor: Cursor): Bolt {
            return Bolt(
                id = cursor.getLongOrThrow(ColumnNames.Bolt.COL_ID),
                type = cursor.getStringOrThrow(ColumnNames.Bolt.COL_TYPE),
                key = cursor.getStringOrThrow(ColumnNames.Bolt.COL_KEY),
                value = cursor.getStringOrNull(ColumnNames.Bolt.COL_VALUE)
            )
        }
    }
}

private fun Cursor.getStringOrThrow(columnName: String): String = getStringOrNull(columnName)!!

private fun Cursor.getStringOrNull(columnName: String): String? {
    val index = getColumnIndexOrThrow(columnName)
    return if (isNull(index)) null else getString(index)
}

private fun Cursor.getLongOrThrow(columnName: String): Long = getLongOrNull(columnName)!!

private fun Cursor.getLongOrNull(columnName: String): Long? {
    val index = getColumnIndexOrThrow(columnName)
    return if (isNull(index)) null else getLong(index)
}

object WrenchProviderContract {
    const val WRENCH_AUTHORITY = BuildConfig.WRENCH_AUTHORITY

    const val WRENCH_API_VERSION = "API_VERSION"

    private val boltUri = Uri.parse("content://$WRENCH_AUTHORITY/currentConfiguration")
    private val nutUri = Uri.parse("content://$WRENCH_AUTHORITY/predefinedConfigurationValue")

    @JvmStatic
    fun boltUri(id: Long): Uri {
        return boltUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(WRENCH_API_VERSION, BuildConfig.WRENCH_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    fun boltUri(key: String): Uri {
        return boltUri
            .buildUpon()
            .appendPath(key)
            .appendQueryParameter(WRENCH_API_VERSION, BuildConfig.WRENCH_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    fun boltUri(): Uri {
        return boltUri
            .buildUpon()
            .appendQueryParameter(WRENCH_API_VERSION, BuildConfig.WRENCH_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    fun nutUri(): Uri {
        return nutUri
            .buildUpon()
            .appendQueryParameter(WRENCH_API_VERSION, BuildConfig.WRENCH_API_VERSION.toString())
            .build()
    }
}
