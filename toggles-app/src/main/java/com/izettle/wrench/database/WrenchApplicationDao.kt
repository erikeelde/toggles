package com.izettle.wrench.database

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.izettle.wrench.database.tables.ApplicationTable

@Dao
interface WrenchApplicationDao {

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME)
    fun getApplications(): PagingSource<Int, WrenchApplication>

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE id = (:id)")
    fun getApplication(id: Long): LiveData<WrenchApplication>

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE id = (:id)")
    fun getApplicationSync(id: Long): WrenchApplication

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE packageName IN (:packageName)")
    fun loadByPackageName(packageName: String): WrenchApplication

    @Insert
    fun insert(application: WrenchApplication): Long

    @Delete
    suspend fun delete(application: WrenchApplication)
}
