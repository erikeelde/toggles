package com.izettle.wrench.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.izettle.wrench.database.tables.ScopeTable
import kotlinx.coroutines.flow.Flow

@Dao
interface WrenchScopeDao {

    @Query("SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) AND " + ScopeTable.COL_NAME + " != '" + WrenchScope.SCOPE_DEFAULT + "'")
    fun getScopes(applicationId: Long): Flow<List<WrenchScope>>

    @Insert
    fun insert(scope: WrenchScope): Long

    @Delete
    fun delete(scope: WrenchScope)

    @Query("SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) ORDER BY " + ScopeTable.COL_SELECTED_TIMESTAMP + " DESC LIMIT 1")
    fun getSelectedScope(applicationId: Long): WrenchScope

    @Query("SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) ORDER BY " + ScopeTable.COL_SELECTED_TIMESTAMP + " DESC LIMIT 1")
    fun getSelectedScopeLiveData(applicationId: Long): LiveData<WrenchScope>

    @Query("SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) AND " + ScopeTable.COL_NAME + " = '" + WrenchScope.SCOPE_DEFAULT + "'")
    fun getDefaultScopeLiveData(applicationId: Long): LiveData<WrenchScope>

    @Query("SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_APP_ID + " = (:applicationId) AND " + ScopeTable.COL_NAME + " = '" + WrenchScope.SCOPE_DEFAULT + "'")
    fun getDefaultScope(applicationId: Long): WrenchScope

    @Update
    suspend fun update(scope: WrenchScope)
}
