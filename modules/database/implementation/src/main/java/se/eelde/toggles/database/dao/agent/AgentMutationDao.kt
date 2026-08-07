@file:Suppress("MaxLineLength")

package se.eelde.toggles.database.dao.agent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfiguration
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesScope
import kotlin.time.Instant

/**
 * Write queries backing the adb agent API. Kept separate from [AgentDao] so the read and write
 * surfaces can be reasoned about independently.
 */
@Suppress("TooManyFunctions")
@Dao
interface AgentMutationDao {

    @Insert
    fun insertApplication(application: TogglesApplication): Long

    @Insert
    fun insertConfiguration(configuration: TogglesConfiguration): Long

    @Query("DELETE FROM configuration WHERE id = (:configurationId)")
    fun deleteConfiguration(configurationId: Long): Int

    @Insert
    fun insertConfigurationValue(configurationValue: TogglesConfigurationValue): Long

    @Query(
        "UPDATE configurationValue SET value = (:value) WHERE configurationId = (:configurationId) AND scope = (:scopeId)"
    )
    fun updateConfigurationValue(configurationId: Long, scopeId: Long, value: String): Int

    @Query("SELECT id FROM configurationValue WHERE configurationId = (:configurationId) AND scope = (:scopeId)")
    fun findConfigurationValueId(configurationId: Long, scopeId: Long): Long?

    @Insert
    fun insertScope(scope: TogglesScope): Long

    @Query("UPDATE scope SET selectedTimestamp = (:timeStamp) WHERE id = (:scopeId)")
    fun touchScope(scopeId: Long, timeStamp: Instant): Int

    @Query("SELECT * FROM scope WHERE id = (:scopeId)")
    fun getScope(scopeId: Long): TogglesScope?

    @Query("SELECT * FROM configuration WHERE id = (:configurationId)")
    fun getConfiguration(configurationId: Long): TogglesConfiguration?

    @Query("UPDATE application SET agentControlEnabled = (:enabled) WHERE packageName = (:packageName)")
    fun setAgentControlEnabled(packageName: String, enabled: Boolean): Int
}
