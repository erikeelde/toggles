package com.izettle.wrench.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.izettle.wrench.database.tables.ConfigurationValueTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WrenchConfigurationValueDao {

    @Query("SELECT * FROM " + ConfigurationValueTable.TABLE_NAME +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId)")
    abstract fun getConfigurationValue(configurationId: Long): LiveData<List<WrenchConfigurationValue>>

    @Query("SELECT * FROM " + ConfigurationValueTable.TABLE_NAME +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId) AND " + ConfigurationValueTable.COL_SCOPE + " = (:scopeId)")
    abstract fun getConfigurationValue(configurationId: Long, scopeId: Long): Flow<WrenchConfigurationValue?>

    @Query("UPDATE " + ConfigurationValueTable.TABLE_NAME +
            " SET " + ConfigurationValueTable.COL_VALUE + " = (:value)" +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId) AND " + ConfigurationValueTable.COL_SCOPE + " = (:scopeId) ")
    abstract fun updateConfigurationValueSync(configurationId: Long, scopeId: Long, value: String): Int

    @Query("UPDATE " + ConfigurationValueTable.TABLE_NAME +
            " SET " + ConfigurationValueTable.COL_VALUE + " = (:value)" +
            " WHERE " + ConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId) AND " + ConfigurationValueTable.COL_SCOPE + " = (:scopeId) ")
    abstract suspend fun updateConfigurationValue(configurationId: Long, scopeId: Long, value: String): Int

    @Insert
    abstract fun insertSync(wrenchConfigurationValue: WrenchConfigurationValue): Long

    @Insert
    abstract suspend fun insert(wrenchConfigurationValue: WrenchConfigurationValue): Long

    @Update
    abstract fun update(wrenchConfigurationValue: WrenchConfigurationValue): Int

    @Delete
    abstract suspend fun delete(selectedConfigurationValue: WrenchConfigurationValue)
}
