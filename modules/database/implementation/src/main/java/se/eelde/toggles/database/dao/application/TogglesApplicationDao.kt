package se.eelde.toggles.database.dao.application

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.tables.ApplicationTable

@Dao
interface TogglesApplicationDao {

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME)
    fun getApplications(): Flow<List<TogglesApplication>>

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE id = (:id)")
    suspend fun getApplication(id: Long): TogglesApplication?

    // Observed rather than a one-shot suspend fetch: the Configurations screen's overflow menu
    // reads agentControlEnabled off this row to label its toggle item ("Disable"/"Enable agent
    // control"), and that label must update live when the same screen's own action flips the
    // flag — a one-shot fetch would leave the label stale until the screen is left and reopened.
    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE id = (:id)")
    fun getApplicationFlow(id: Long): Flow<TogglesApplication?>

    @Query(
        "UPDATE " + ApplicationTable.TABLE_NAME +
            " SET " + ApplicationTable.COL_AGENT_CONTROL_ENABLED + " = (:enabled)" +
            " WHERE " + ApplicationTable.COL_ID + " = (:id)"
    )
    suspend fun setAgentControlEnabled(id: Long, enabled: Boolean): Int

    @Delete
    suspend fun delete(application: TogglesApplication)
}
