package se.eelde.toggles.database.tables

import se.eelde.toggles.core.ColumnNames

interface ConfigurationValueTable {
    companion object {
        const val TABLE_NAME = "configurationValue"
        const val COL_ID = ColumnNames.ConfigurationValue.COL_ID
        const val COL_CONFIG_ID = ColumnNames.ConfigurationValue.COL_CONFIG_ID
        const val COL_VALUE = ColumnNames.ConfigurationValue.COL_VALUE
        const val COL_SCOPE = ColumnNames.ConfigurationValue.COL_SCOPE
    }
}
