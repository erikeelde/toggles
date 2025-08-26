@file:Suppress("MaxLineLength")

package se.eelde.toggles.database.dao.provider

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.tables.ScopeTable

@Dao
interface ProviderScopeDao {

    @Insert
    fun insert(scope: TogglesScope): Long

    @Delete
    fun delete(scope: TogglesScope)

    @Query(
        "SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) ORDER BY " + ScopeTable.COL_SELECTED_TIMESTAMP + " DESC LIMIT 1"
    )
    fun getSelectedScope(applicationId: Long): TogglesScope?

    @Query(
        "SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) AND " + ScopeTable.COL_NAME + " = '" + TogglesScope.SCOPE_DEFAULT + "'"
    )
    fun getDefaultScope(applicationId: Long): TogglesScope?

    @Update
    suspend fun update(scope: TogglesScope)
}
