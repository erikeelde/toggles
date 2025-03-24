package se.eelde.toggles.core

import android.database.Cursor

internal fun Cursor.getStringOrThrow(columnName: String): String {
    val index = getColumnIndexOrThrow(columnName)
    return getString(index)
}
internal fun Cursor.getStringOrNull(columnName: String): String? {
    val index = getColumnIndexOrThrow(columnName)
    return if (isNull(index)) null else getString(index)
}

internal fun Cursor.getLongOrThrow(columnName: String): Long {
    val index = getColumnIndexOrThrow(columnName)
    return getLong(index)
}
