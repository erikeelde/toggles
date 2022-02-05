package se.eelde.toggles.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.tables.ConfigurationValueTable

@Dao
interface WrenchConfigurationValueDao {

    @Query(
        "SELECT * FROM " + ConfigurationValueTable.TABLE_NAME +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId)"
    )
    fun getConfigurationValue(configurationId: Long): LiveData<List<WrenchConfigurationValue>>

    @Query(
        "SELECT * FROM " + ConfigurationValueTable.TABLE_NAME +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId) AND " + ConfigurationValueTable.COL_SCOPE + " = (:scopeId)"
    )
    fun getConfigurationValue(configurationId: Long, scopeId: Long): Flow<WrenchConfigurationValue?>

    @Query(
        "UPDATE " + ConfigurationValueTable.TABLE_NAME +
            " SET " + ConfigurationValueTable.COL_VALUE + " = (:value)" +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId) AND " + ConfigurationValueTable.COL_SCOPE + " = (:scopeId) "
    )
    fun updateConfigurationValueSync(configurationId: Long, scopeId: Long, value: String): Int

    @Query(
        "UPDATE " + ConfigurationValueTable.TABLE_NAME +
            " SET " + ConfigurationValueTable.COL_VALUE + " = (:value)" +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId) AND " + ConfigurationValueTable.COL_SCOPE + " = (:scopeId) "
    )
    suspend fun updateConfigurationValue(configurationId: Long, scopeId: Long, value: String): Int

    @Insert
    fun insertSync(wrenchConfigurationValue: WrenchConfigurationValue): Long

    @Insert
    suspend fun insert(wrenchConfigurationValue: WrenchConfigurationValue): Long

    @Update
    fun update(wrenchConfigurationValue: WrenchConfigurationValue): Int

    @Delete
    suspend fun delete(selectedConfigurationValue: WrenchConfigurationValue)
}
