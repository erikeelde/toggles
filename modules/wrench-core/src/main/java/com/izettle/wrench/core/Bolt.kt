package com.izettle.wrench.core

import android.content.ContentValues
import android.database.Cursor
import androidx.annotation.StringDef

data class Bolt(
    val id: Long = 0,
    @BoltType
    val type: String,
    val key: String,
    val value: String?,
) {

    fun toContentValues() = ContentValues().apply {
        put(ColumnNames.Bolt.COL_ID, id)
        put(ColumnNames.Bolt.COL_KEY, key)
        put(ColumnNames.Bolt.COL_VALUE, value)
        put(ColumnNames.Bolt.COL_TYPE, type)
    }

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(TYPE.BOOLEAN, TYPE.STRING, TYPE.INTEGER, TYPE.ENUM)
    annotation class BoltType
    interface TYPE {
        companion object {
            const val BOOLEAN = "boolean"
            const val STRING = "string"
            const val INTEGER = "integer"
            const val ENUM = "enum"
        }
    }

    companion object {
        @JvmStatic
        fun fromContentValues(values: ContentValues): Bolt {
            return Bolt(
                id = values.getAsLong(ColumnNames.Bolt.COL_ID) ?: 0,
                type = values.getAsString(ColumnNames.Bolt.COL_TYPE),
                key = values.getAsString(ColumnNames.Bolt.COL_KEY),
                value = values.getAsString(ColumnNames.Bolt.COL_VALUE),
            )
        }

        @JvmStatic
        fun fromCursor(cursor: Cursor): Bolt {
            return Bolt(
                id = cursor.getLongOrThrow(ColumnNames.Bolt.COL_ID),
                type = cursor.getStringOrThrow(ColumnNames.Bolt.COL_TYPE),
                key = cursor.getStringOrThrow(ColumnNames.Bolt.COL_KEY),
                value = cursor.getStringOrNull(ColumnNames.Bolt.COL_VALUE),
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
