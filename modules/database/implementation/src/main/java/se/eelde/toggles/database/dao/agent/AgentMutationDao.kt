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

    @Query("DELETE FROM configurationValue WHERE configurationId = (:configurationId) AND scope = (:scopeId)")
    fun deleteConfigurationValue(configurationId: Long, scopeId: Long): Int

    // configurationValue.scope has no foreign key to scope (see the exported schema), so deleting a
    // scope never cascades to its value rows on its own — this is the explicit cleanup callers (see
    // AgentScopeDeleter) must run themselves before deleteScope, or those rows are orphaned forever:
    // still returned by AgentDao.getConfigurationValues (it joins through configuration, not scope),
    // surfaced with a null scope name, and never participating in resolution again.
    @Query("DELETE FROM configurationValue WHERE scope = (:scopeId)")
    fun deleteScopeValues(scopeId: Long): Int

    @Query("DELETE FROM scope WHERE id = (:scopeId)")
    fun deleteScope(scopeId: Long): Int

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

    // Synchronous, matching every other query in this DAO: AgentCallHandler.handle runs
    // synchronously on the binder thread, same as TogglesAgentProvider.call. The UI-facing
    // equivalent, ProviderConfigurationDao.touch, is a suspend function and would need a
    // runBlocking bridge here for no benefit.
    @Query("UPDATE configuration SET lastUse = (:date) WHERE id = (:configurationId)")
    fun touch(configurationId: Long, date: Instant): Int
}
