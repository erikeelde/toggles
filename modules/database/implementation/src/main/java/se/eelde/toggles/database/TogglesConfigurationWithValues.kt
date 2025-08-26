package se.eelde.toggles.database

import androidx.room.ColumnInfo
import androidx.room.Relation
import se.eelde.toggles.database.tables.ConfigurationTable

data class TogglesConfigurationWithValues constructor(
    var id: Long,

    var applicationId: Long,

    @ColumnInfo(name = ConfigurationTable.COL_KEY)
    var key: String?,

    @ColumnInfo(name = ConfigurationTable.COL_TYPE)
    var type: String?,

    @Relation(parentColumn = "id", entityColumn = "configurationId")
    var configurationValues: Set<TogglesConfigurationValue>? = null
)
