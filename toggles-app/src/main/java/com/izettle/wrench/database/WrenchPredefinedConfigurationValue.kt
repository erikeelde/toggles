package com.izettle.wrench.database

import android.content.ContentValues
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.izettle.wrench.database.tables.ConfigurationTable
import com.izettle.wrench.database.tables.PredefinedConfigurationValueTable

@Entity(
    tableName = PredefinedConfigurationValueTable.TABLE_NAME,
    indices = [Index(value = arrayOf(PredefinedConfigurationValueTable.COL_CONFIG_ID))],
    foreignKeys = [ForeignKey(entity = WrenchConfiguration::class, parentColumns = arrayOf(ConfigurationTable.COL_ID), childColumns = arrayOf(PredefinedConfigurationValueTable.COL_CONFIG_ID), onDelete = CASCADE)]
)
data class WrenchPredefinedConfigurationValue constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = PredefinedConfigurationValueTable.COL_ID)
    var id: Long = 0,

    @ColumnInfo(name = PredefinedConfigurationValueTable.COL_CONFIG_ID)
    var configurationId: Long = 0,

    @ColumnInfo(name = PredefinedConfigurationValueTable.COL_VALUE)
    var value: String? = null
) {
    companion object {

        @JvmStatic
        fun fromContentValues(values: ContentValues): WrenchPredefinedConfigurationValue {
            val wrenchConfigurationValue = WrenchPredefinedConfigurationValue()
            if (values.containsKey(PredefinedConfigurationValueTable.COL_ID)) {
                wrenchConfigurationValue.id = values.getAsLong(PredefinedConfigurationValueTable.COL_ID)!!
            }
            wrenchConfigurationValue.configurationId = values.getAsLong(PredefinedConfigurationValueTable.COL_CONFIG_ID)!!
            wrenchConfigurationValue.value = values.getAsString(PredefinedConfigurationValueTable.COL_VALUE)

            return wrenchConfigurationValue
        }
    }
}
