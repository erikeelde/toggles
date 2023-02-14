@file:Suppress("MaxLineLength")

package se.eelde.toggles.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.tables.PredefinedConfigurationValueTable

@Dao
interface WrenchPredefinedConfigurationValueDao {

    @Query(
        "SELECT * FROM " + PredefinedConfigurationValueTable.TABLE_NAME + " WHERE " + PredefinedConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId)"
    )
    fun getLiveDataByConfigurationId(configurationId: Long): LiveData<List<WrenchPredefinedConfigurationValue>>

    @Query(
        "SELECT * FROM " + PredefinedConfigurationValueTable.TABLE_NAME + " WHERE " + PredefinedConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId)"
    )
    fun getByConfigurationId(configurationId: Long): Flow<List<WrenchPredefinedConfigurationValue>>

    @Insert
    fun insert(fullConfig: WrenchPredefinedConfigurationValue): Long
}
