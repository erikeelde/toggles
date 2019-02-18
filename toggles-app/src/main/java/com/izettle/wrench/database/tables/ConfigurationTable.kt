package com.izettle.wrench.database.tables

import com.izettle.wrench.core.ColumnNames

interface ConfigurationTable {
    companion object {
        const val TABLE_NAME = "configuration"
        const val COL_ID = ColumnNames.Bolt.COL_ID
        const val COL_APP_ID = "applicationId"
        const val COL_KEY = ColumnNames.Bolt.COL_KEY
        const val COL_TYPE = ColumnNames.Bolt.COL_TYPE
    }
}

