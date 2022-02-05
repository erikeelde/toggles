package se.eelde.toggles.database.tables

import se.eelde.toggles.core.ColumnNames

interface PredefinedConfigurationValueTable {
    companion object {

        const val TABLE_NAME = "predefinedConfigurationValue"
        const val COL_ID = ColumnNames.ToggleValue.COL_ID
        const val COL_CONFIG_ID = ColumnNames.ToggleValue.COL_CONFIG_ID
        const val COL_VALUE = ColumnNames.ToggleValue.COL_VALUE
    }
}
