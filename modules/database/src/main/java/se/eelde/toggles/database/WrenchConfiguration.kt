package se.eelde.toggles.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import se.eelde.toggles.database.tables.ApplicationTable
import se.eelde.toggles.database.tables.ConfigurationTable
import java.util.Date

@Entity(
    tableName = ConfigurationTable.TABLE_NAME,
    indices = [Index(value = arrayOf(ConfigurationTable.COL_APP_ID, ConfigurationTable.COL_KEY), unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = WrenchApplication::class,
            parentColumns = arrayOf(
                ApplicationTable.COL_ID
            ),
            childColumns = arrayOf(ConfigurationTable.COL_APP_ID), onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WrenchConfiguration(
    @field:PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = ConfigurationTable.COL_ID)
    var id: Long,

    @field:ColumnInfo(name = ConfigurationTable.COL_APP_ID)
    var applicationId: Long,

    @field:ColumnInfo(name = ConfigurationTable.COL_KEY)
    var key: String?,

    @field:ColumnInfo(name = ConfigurationTable.COL_TYPE)
    var type: String
) {

    var lastUse: Date = Date()

    init {
        this.lastUse = Date()
    }
}
