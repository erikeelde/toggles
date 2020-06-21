package com.izettle.wrench.database.tables

interface ScopeTable {
    companion object {
        const val TABLE_NAME = "scope"
        const val COL_ID = "id"
        const val COL_APP_ID = "applicationId"
        const val COL_NAME = "name"
        const val COL_SELECTED_TIMESTAMP = "selectedTimestamp"
    }
}

