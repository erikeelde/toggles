@file:Suppress("MaxLineLength")

package se.eelde.toggles.database.dao.provider

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import se.eelde.toggles.database.TogglesConfiguration
import se.eelde.toggles.database.tables.ConfigurationTable
import se.eelde.toggles.database.tables.ConfigurationValueTable
import se.eelde.toggles.database.tables.ScopeTable
import java.util.Date

@Suppress("TooManyFunctions")
@Dao
interface ProviderConfigurationDao {

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
        "SELECT configuration.id, " +
            " configuration.configurationKey, " +
            " configuration.configurationType," +
            " configurationValue.value," +
            " scope.name as scope" +
            " FROM " + ConfigurationTable.TABLE_NAME +
            " INNER JOIN " + ConfigurationValueTable.TABLE_NAME + " ON configuration.id = configurationValue.configurationId " +
            " INNER JOIN " + ScopeTable.TABLE_NAME + " ON configurationValue.scope = scope.id " +
            " WHERE configuration.configurationKey = (:configurationKey) AND configurationValue.scope IN (:scopeId)"
    )
    fun getToggles(configurationKey: String, scopeId: List<Long>): Cursor

    @Query(
        "SELECT configuration.id, " +
            " configuration.configurationKey, " +
            " configuration.configurationType," +
            " configurationValue.value," +
            " scope.name as scope" +
            " FROM " + ConfigurationTable.TABLE_NAME +
            " INNER JOIN " + ConfigurationValueTable.TABLE_NAME + " ON configuration.id = configurationValue.configurationId " +
            " INNER JOIN " + ScopeTable.TABLE_NAME + " ON configurationValue.scope = scope.id " +
            " WHERE configuration.configurationKey = (:configurationKey)"
    )
    fun getToggles(configurationKey: String): Cursor

    @Query(
        "SELECT * " +
            " FROM " + ConfigurationTable.TABLE_NAME +
            " WHERE configuration.applicationId = (:applicationId) AND configuration.configurationKey = (:configurationKey)"
    )
    fun getTogglesConfiguration(applicationId: Long, configurationKey: String): TogglesConfiguration?

    @Query("DELETE FROM configuration WHERE applicationId = :callingApplication AND id = :id")
    fun deleteConfiguration(callingApplication: Long, id: Long): Int

    @Query(
        "DELETE FROM configuration WHERE applicationId = :callingApplication AND configurationKey = :configurationKey"
    )
    fun deleteConfiguration(callingApplication: Long, configurationKey: String): Int

    @Insert
    fun insert(togglesConfiguration: TogglesConfiguration): Long

    @Query("UPDATE configuration set lastUse=:date WHERE id= :configurationId")
    suspend fun touch(configurationId: Long, date: Date)

    @Query(
        "UPDATE configuration SET configurationKey = :key, configurationType = :type WHERE applicationId = :callingApplication AND id= :id"
    )
    fun updateConfiguration(callingApplication: Long, id: Long, key: String, type: String): Int

    @Query(
        "SELECT * FROM configuration WHERE applicationId = :callingApplication"
    )
    fun getConfigurationCursor(callingApplication: Long): Cursor

    @Query("SELECT * FROM configuration WHERE id = :configurationId and applicationId = :callingApplication")
    fun getConfigurationCursor(callingApplication: Long, configurationId: Long): Cursor

    @Query(
        "SELECT * FROM configuration WHERE configurationKey = :configurationKey and applicationId = :callingApplication"
    )
    fun getConfigurationCursor(callingApplication: Long, configurationKey: String): Cursor

    @Query(
        """SELECT configurationValue.* FROM configuration 
INNER JOIN configurationValue ON configuration.id = configurationValue.configurationId  
WHERE configuration.applicationId = :callingApplication AND configurationId = :configurationId
"""
    )
    fun getConfigurationValueCursor(callingApplication: Long, configurationId: Long): Cursor

    @Query(
        """SELECT configurationValue.* FROM configuration 
INNER JOIN configurationValue ON configuration.id = configurationValue.configurationId  
WHERE configuration.applicationId = :callingApplication AND configurationKey = :configurationKey
"""
    )
    fun getConfigurationValueCursor(callingApplication: Long, configurationKey: String): Cursor
}
