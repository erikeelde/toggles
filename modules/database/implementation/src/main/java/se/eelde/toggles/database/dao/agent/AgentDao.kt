@file:Suppress("MaxLineLength")

package se.eelde.toggles.database.dao.agent

import androidx.room.Dao
import androidx.room.Query
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfiguration
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesPredefinedConfigurationValue
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.tables.ApplicationTable

/**
 * Queries backing the adb agent API. Kept separate from the provider DAOs because the agent reads
 * across applications, which the per-application provider API must never do.
 */
@Dao
interface AgentDao {

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " ORDER BY " + ApplicationTable.COL_PACK_NAME + " ASC")
    fun getApplications(): List<TogglesApplication>

    @Query(
        "SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE " + ApplicationTable.COL_PACK_NAME + " = (:packageName)"
    )
    fun getApplicationByPackageName(packageName: String): TogglesApplication?

    @Query(
        "SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE " + ApplicationTable.COL_ID + " = (:applicationId)"
    )
    fun getApplicationById(applicationId: Long): TogglesApplication?

    @Query("SELECT * FROM configuration WHERE applicationId = (:applicationId) ORDER BY configurationKey ASC")
    fun getConfigurations(applicationId: Long): List<TogglesConfiguration>

    @Query("SELECT * FROM scope WHERE applicationId = (:applicationId) ORDER BY id ASC")
    fun getScopes(applicationId: Long): List<TogglesScope>

    @Query(
        """SELECT configurationValue.* FROM configurationValue
INNER JOIN configuration ON configuration.id = configurationValue.configurationId
WHERE configuration.applicationId = (:applicationId)"""
    )
    fun getConfigurationValues(applicationId: Long): List<TogglesConfigurationValue>

    @Query(
        """SELECT predefinedConfigurationValue.* FROM predefinedConfigurationValue
INNER JOIN configuration ON configuration.id = predefinedConfigurationValue.configurationId
WHERE configuration.applicationId = (:applicationId)"""
    )
    fun getPredefinedConfigurationValues(applicationId: Long): List<TogglesPredefinedConfigurationValue>
}
