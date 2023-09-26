package se.eelde.toggles.database

import android.content.ContentValues
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import se.eelde.toggles.database.tables.ConfigurationTable
import se.eelde.toggles.database.tables.PredefinedConfigurationValueTable

@Entity(
    tableName = PredefinedConfigurationValueTable.TABLE_NAME,
    indices = [Index(
        value = arrayOf(
            PredefinedConfigurationValueTable.COL_CONFIG_ID,
            PredefinedConfigurationValueTable.COL_VALUE
        ),
        unique = true
    )],
    foreignKeys = [
        ForeignKey(
            entity = WrenchConfiguration::class,
            parentColumns = arrayOf(
                ConfigurationTable.COL_ID
            ),
            childColumns = arrayOf(PredefinedConfigurationValueTable.COL_CONFIG_ID),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WrenchPredefinedConfigurationValue constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = PredefinedConfigurationValueTable.COL_ID)
    var id: Long,

    @ColumnInfo(name = PredefinedConfigurationValueTable.COL_CONFIG_ID)
    var configurationId: Long,

    @ColumnInfo(name = PredefinedConfigurationValueTable.COL_VALUE)
    var value: String?
) {
    companion object {

        @JvmStatic
        fun fromContentValues(values: ContentValues): WrenchPredefinedConfigurationValue {
            val wrenchConfigurationValue =
                WrenchPredefinedConfigurationValue(id = 0, configurationId = 0, value = null)
            if (values.containsKey(PredefinedConfigurationValueTable.COL_ID)) {
                wrenchConfigurationValue.id =
                    values.getAsLong(PredefinedConfigurationValueTable.COL_ID)!!
            }
            wrenchConfigurationValue.configurationId =
                values.getAsLong(PredefinedConfigurationValueTable.COL_CONFIG_ID)!!
            wrenchConfigurationValue.value =
                values.getAsString(PredefinedConfigurationValueTable.COL_VALUE)

            return wrenchConfigurationValue
        }
    }
}
