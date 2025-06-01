package se.eelde.toggles.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import se.eelde.toggles.database.tables.ApplicationTable

@Entity(
    tableName = ApplicationTable.TABLE_NAME,
    indices = [Index(value = arrayOf(ApplicationTable.COL_PACK_NAME), unique = true)]
)
data class TogglesApplication constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ApplicationTable.COL_ID)
    var id: Long,

    @ColumnInfo(name = ApplicationTable.COL_SHORTCUT_ID)
    var shortcutId: String,

    @ColumnInfo(name = ApplicationTable.COL_PACK_NAME)
    var packageName: String,

    @ColumnInfo(name = ApplicationTable.COL_APP_LABEL)
    var applicationLabel: String
)
