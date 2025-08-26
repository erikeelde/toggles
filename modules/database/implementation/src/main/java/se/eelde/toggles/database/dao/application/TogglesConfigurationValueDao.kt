@file:Suppress("MaxLineLength")

package se.eelde.toggles.database.dao.application

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.tables.ConfigurationValueTable

@Dao
interface TogglesConfigurationValueDao {
    @Query(
        "SELECT * FROM " + ConfigurationValueTable.TABLE_NAME +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId) AND " + ConfigurationValueTable.COL_SCOPE + " = (:scopeId)"
    )
    fun getConfigurationValue(configurationId: Long, scopeId: Long): Flow<TogglesConfigurationValue?>

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
    fun insertSync(togglesConfigurationValue: TogglesConfigurationValue): Long

    @Insert
    suspend fun insert(togglesConfigurationValue: TogglesConfigurationValue): Long

    @Update
    fun update(togglesConfigurationValue: TogglesConfigurationValue): Int

    @Delete
    suspend fun delete(selectedConfigurationValue: TogglesConfigurationValue)
}
