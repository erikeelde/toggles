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
    indices = [
        Index(
            value = arrayOf(
                PredefinedConfigurationValueTable.COL_CONFIG_ID,
                PredefinedConfigurationValueTable.COL_VALUE
            ),
            unique = true
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = TogglesConfiguration::class,
            parentColumns = arrayOf(
                ConfigurationTable.COL_ID
            ),
            childColumns = arrayOf(PredefinedConfigurationValueTable.COL_CONFIG_ID),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TogglesPredefinedConfigurationValue constructor(
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
        fun fromContentValues(values: ContentValues): TogglesPredefinedConfigurationValue {
            val togglesPredefinedConfigurationValue =
                TogglesPredefinedConfigurationValue(id = 0, configurationId = 0, value = null)
            if (values.containsKey(PredefinedConfigurationValueTable.COL_ID)) {
                togglesPredefinedConfigurationValue.id =
                    values.getAsLong(PredefinedConfigurationValueTable.COL_ID)!!
            }
            togglesPredefinedConfigurationValue.configurationId =
                values.getAsLong(PredefinedConfigurationValueTable.COL_CONFIG_ID)!!
            togglesPredefinedConfigurationValue.value =
                values.getAsString(PredefinedConfigurationValueTable.COL_VALUE)

            return togglesPredefinedConfigurationValue
        }
    }
}
