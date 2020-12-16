package com.izettle.wrench.database.tables

interface ApplicationTable {
    companion object {
        const val TABLE_NAME = "application"
        const val COL_ID = "id"
        const val COL_SHORTCUT_ID = "shortcutId"
        const val COL_PACK_NAME = "packageName"
        const val COL_APP_LABEL = "applicationLabel"
    }
}
