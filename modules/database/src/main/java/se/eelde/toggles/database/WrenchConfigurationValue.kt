package se.eelde.toggles.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import se.eelde.toggles.database.tables.ConfigurationTable
import se.eelde.toggles.database.tables.ConfigurationValueTable

@Entity(
    tableName = ConfigurationValueTable.TABLE_NAME,
    indices = [
        Index(
            value = arrayOf(
                ConfigurationValueTable.COL_CONFIG_ID,
                ConfigurationValueTable.COL_VALUE,
                ConfigurationValueTable.COL_SCOPE
            ),
            unique = true
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = WrenchConfiguration::class,
            parentColumns = arrayOf(
                ConfigurationTable.COL_ID
            ),
            childColumns = arrayOf(ConfigurationValueTable.COL_CONFIG_ID),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WrenchConfigurationValue(
    @field:PrimaryKey(autoGenerate = true) @field:ColumnInfo(name = ConfigurationValueTable.COL_ID)
    var id: Long,

    @field:ColumnInfo(name = ConfigurationValueTable.COL_CONFIG_ID)
    var configurationId: Long,

    @field:ColumnInfo(name = ConfigurationValueTable.COL_VALUE)
    var value: String?,

    @field:ColumnInfo(name = ConfigurationValueTable.COL_SCOPE)
    var scope: Long
)
