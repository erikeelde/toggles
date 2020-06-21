package com.izettle.wrench.database.tables

import com.izettle.wrench.core.ColumnNames

interface ConfigurationValueTable {
    companion object {
        const val TABLE_NAME = "configurationValue"
        const val COL_ID = ColumnNames.Bolt.COL_ID
        const val COL_CONFIG_ID = "configurationId"
        const val COL_VALUE = ColumnNames.Bolt.COL_VALUE
        const val COL_SCOPE = "scope"
    }
}
