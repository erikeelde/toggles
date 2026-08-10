package se.eelde.toggles.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import se.eelde.toggles.database.tables.ConfigurationTable
import se.eelde.toggles.database.tables.ConfigurationValueTable
import se.eelde.toggles.database.tables.ScopeTable

@Entity(
    tableName = ConfigurationValueTable.TABLE_NAME,
    indices = [
        Index(
            value = arrayOf(
                ConfigurationValueTable.COL_CONFIG_ID,
                ConfigurationValueTable.COL_SCOPE
            ),
            unique = true
        ),
        Index(
            value = arrayOf(ConfigurationValueTable.COL_SCOPE)
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = TogglesConfiguration::class,
            parentColumns = arrayOf(
                ConfigurationTable.COL_ID
            ),
            childColumns = arrayOf(ConfigurationValueTable.COL_CONFIG_ID),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TogglesScope::class,
            parentColumns = arrayOf(
                ScopeTable.COL_ID
            ),
            childColumns = arrayOf(ConfigurationValueTable.COL_SCOPE),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TogglesConfigurationValue(
    @field:PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = ConfigurationValueTable.COL_ID)
    var id: Long,

    @field:ColumnInfo(name = ConfigurationValueTable.COL_CONFIG_ID)
    var configurationId: Long,

    @field:ColumnInfo(name = ConfigurationValueTable.COL_VALUE)
    var value: String,

    @field:ColumnInfo(name = ConfigurationValueTable.COL_SCOPE)
    var scope: Long
)
