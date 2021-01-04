package com.izettle.wrench.database

import androidx.room.ColumnInfo
import androidx.room.Relation
import com.izettle.wrench.database.tables.ConfigurationTable

data class WrenchConfigurationWithValues constructor(
    var id: Long,

    var applicationId: Long,

    @ColumnInfo(name = ConfigurationTable.COL_KEY)
    var key: String?,

    @ColumnInfo(name = ConfigurationTable.COL_TYPE)
    var type: String?
) {
    @Relation(parentColumn = "id", entityColumn = "configurationId")
    var configurationValues: Set<WrenchConfigurationValue>? = null
}
