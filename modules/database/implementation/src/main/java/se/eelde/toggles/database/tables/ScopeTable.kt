package se.eelde.toggles.database.tables

import se.eelde.toggles.core.ColumnNames

interface ScopeTable {
    companion object {
        const val TABLE_NAME = "scope"
        const val COL_ID = ColumnNames.ToggleScope.COL_ID
        const val COL_APP_ID = ColumnNames.ToggleScope.COL_APP_ID
        const val COL_NAME = ColumnNames.ToggleScope.COL_NAME
        const val COL_SELECTED_TIMESTAMP = ColumnNames.ToggleScope.COL_SELECTED_TIMESTAMP
    }
}
