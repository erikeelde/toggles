package com.izettle.wrench.database.tables

import com.izettle.wrench.core.ColumnNames

interface PredefinedConfigurationValueTable {
    companion object {

        const val TABLE_NAME = "predefinedConfigurationValue"
        const val COL_ID = ColumnNames.Nut.COL_ID
        const val COL_CONFIG_ID = ColumnNames.Nut.COL_CONFIG_ID
        const val COL_VALUE = ColumnNames.Nut.COL_VALUE
    }
}
