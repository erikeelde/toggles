package com.izettle.wrench.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.izettle.wrench.database.tables.PredefinedConfigurationValueTable

@Dao
interface WrenchPredefinedConfigurationValueDao {

    @Query("SELECT * FROM " + PredefinedConfigurationValueTable.TABLE_NAME + " WHERE " + PredefinedConfigurationValueTable.COL_CONFIG_ID + " = (:configurationId)")
    fun getByConfigurationId(configurationId: Long): LiveData<List<WrenchPredefinedConfigurationValue>>

    @Insert
    fun insert(fullConfig: WrenchPredefinedConfigurationValue): Long
}
