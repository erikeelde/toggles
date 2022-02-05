package se.eelde.toggles.database.tables

import se.eelde.toggles.core.ColumnNames

interface ConfigurationTable {
    companion object {
        const val TABLE_NAME = "configuration"
        const val COL_ID = ColumnNames.Toggle.COL_ID
        const val COL_APP_ID = "applicationId"
        const val COL_KEY = ColumnNames.Toggle.COL_KEY
        const val COL_TYPE = ColumnNames.Toggle.COL_TYPE
    }
}
