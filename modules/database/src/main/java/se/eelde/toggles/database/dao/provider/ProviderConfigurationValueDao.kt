@file:Suppress("MaxLineLength")

package se.eelde.toggles.database.dao.provider

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.WrenchConfigurationValue
import se.eelde.toggles.database.tables.ConfigurationValueTable

@Dao
interface ProviderConfigurationValueDao {
    @Query(
        "UPDATE " + ConfigurationValueTable.TABLE_NAME +
            " SET " + ConfigurationValueTable.COL_VALUE + " = (:value)" +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId) AND " + ConfigurationValueTable.COL_SCOPE + " = (:scopeId) "
    )
    fun updateConfigurationValueSync(configurationId: Long, scopeId: Long, value: String): Int

    @Insert
    fun insertSync(wrenchConfigurationValue: WrenchConfigurationValue): Long

    @Insert
    suspend fun insert(wrenchConfigurationValue: WrenchConfigurationValue): Long

    @Update
    fun update(wrenchConfigurationValue: WrenchConfigurationValue): Int

    @Delete
    suspend fun delete(selectedConfigurationValue: WrenchConfigurationValue)
}
