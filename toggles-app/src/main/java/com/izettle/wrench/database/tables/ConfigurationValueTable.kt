package com.izettle.wrench.database.tables

import se.eelde.toggles.core.ColumnNames

interface ConfigurationValueTable {
    companion object {
        const val TABLE_NAME = "configurationValue"
        const val COL_ID = ColumnNames.Toggle.COL_ID
        const val COL_CONFIG_ID = "configurationId"
        const val COL_VALUE = ColumnNames.Toggle.COL_VALUE
        const val COL_SCOPE = "scope"
    }
}
