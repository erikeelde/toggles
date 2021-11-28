package com.izettle.wrench.core

import android.content.ContentValues

data class Nut(
    val id: Long,
    val configurationId: Long,
    val value: String,
) {
    constructor(configurationId: Long, value: String) : this(0, configurationId, value)

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
