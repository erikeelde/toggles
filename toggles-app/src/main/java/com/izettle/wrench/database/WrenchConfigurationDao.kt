package com.izettle.wrench.database

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.izettle.wrench.database.tables.ConfigurationTable
import com.izettle.wrench.database.tables.ConfigurationValueTable
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WrenchConfigurationDao {

    @Query(
        "SELECT configuration.id, " +
            " configuration.configurationKey, " +
            " configuration.configurationType," +
            " configurationValue.value" +
            " FROM " + ConfigurationTable.TABLE_NAME +
            " INNER JOIN " + ConfigurationValueTable.TABLE_NAME + " ON configuration.id = configurationValue.configurationId " +
            " WHERE configuration.id = (:configurationId) AND configurationValue.scope = (:scopeId)"
    )
    fun getToggle(configurationId: Long, scopeId: Long): Cursor

    @Query(
        "SELECT configuration.id, " +
            " configuration.configurationKey, " +
            " configuration.configurationType," +
            " configurationValue.value" +
            " FROM " + ConfigurationTable.TABLE_NAME +
            " INNER JOIN " + ConfigurationValueTable.TABLE_NAME + " ON configuration.id = configurationValue.configurationId " +
            " WHERE configuration.configurationKey = (:configurationKey) AND configurationValue.scope = (:scopeId)"
    )
    fun getToggle(configurationKey: String, scopeId: Long): Cursor

    @Query(
        "SELECT * " +
            " FROM " + ConfigurationTable.TABLE_NAME +
            " WHERE configuration.applicationId = (:applicationId) AND configuration.configurationKey = (:configurationKey)"
    )
    fun getWrenchConfiguration(applicationId: Long, configurationKey: String): WrenchConfiguration

    @Query(
        "SELECT * " +
            " FROM " + ConfigurationTable.TABLE_NAME +
            " WHERE configuration.applicationId = (:applicationId) AND configuration.configurationKey = (:configurationKey)"
    )
    fun getWrenchConfigurationByKey(applicationId: Long, configurationKey: String): WrenchConfiguration?

    @Query(
        "SELECT * " +
            " FROM " + ConfigurationTable.TABLE_NAME +
            " WHERE configuration.applicationId = (:applicationId) AND configuration.id = (:configurationId)"
    )
    fun getWrenchConfigurationById(applicationId: Long, configurationId: Long): WrenchConfiguration?

    @Query("SELECT * FROM configuration WHERE id = :configurationId")
    fun getConfiguration(configurationId: Long): Flow<WrenchConfiguration>

    @Transaction
    @Query("SELECT id, applicationId, configurationKey, configurationType FROM configuration WHERE applicationId = :applicationId ORDER BY lastUse DESC")
    fun getApplicationConfigurations(applicationId: Long): LiveData<List<WrenchConfigurationWithValues>>

    @Transaction
    @Query("SELECT id, applicationId, configurationKey, configurationType FROM configuration WHERE applicationId = :applicationId AND configurationKey LIKE :query ORDER BY lastUse DESC")
    fun getApplicationConfigurations(applicationId: Long, query: String): LiveData<List<WrenchConfigurationWithValues>>

    @Insert
    fun insert(wrenchConfiguration: WrenchConfiguration): Long

    @Query("UPDATE configuration set lastUse=:date WHERE id= :configurationId")
    suspend fun touch(configurationId: Long, date: Date)
}
