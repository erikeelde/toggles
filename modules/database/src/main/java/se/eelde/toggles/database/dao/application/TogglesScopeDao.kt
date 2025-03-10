@file:Suppress("MaxLineLength")

package se.eelde.toggles.database.dao.application

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.WrenchScope
import se.eelde.toggles.database.tables.ScopeTable

@Dao
interface TogglesScopeDao {

    @Query(
        "SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) AND " + ScopeTable.COL_NAME + " != '" + WrenchScope.SCOPE_DEFAULT + "'"
    )
    fun getScopes(applicationId: Long): Flow<List<WrenchScope>>

    @Insert
    fun insert(scope: WrenchScope): Long

    @Delete
    fun delete(scope: WrenchScope)

    @Query(
        "SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) ORDER BY " + ScopeTable.COL_SELECTED_TIMESTAMP + " DESC LIMIT 1"
    )
    fun getSelectedScope(applicationId: Long): WrenchScope?

    @Query(
        "SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) ORDER BY " + ScopeTable.COL_SELECTED_TIMESTAMP + " DESC LIMIT 1"
    )
    fun getSelectedScopeFlow(applicationId: Long): Flow<WrenchScope>

    @Query(
        "SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) AND " + ScopeTable.COL_NAME + " = '" + WrenchScope.SCOPE_DEFAULT + "'"
    )
    fun getDefaultScopeFlow(applicationId: Long): Flow<WrenchScope>

    @Query(
        "SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) AND " + ScopeTable.COL_NAME + " = '" + WrenchScope.SCOPE_DEFAULT + "'"
    )
    fun getDefaultScope(applicationId: Long): WrenchScope?

    @Update
    suspend fun update(scope: WrenchScope)
}
