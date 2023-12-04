package se.eelde.toggles.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.tables.ApplicationTable

@Dao
interface WrenchApplicationDao {

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME)
    fun getApplicationsPagingSource(): PagingSource<Int, WrenchApplication>

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME)
    fun getApplications(): Flow<List<WrenchApplication>>

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE id = (:id)")
    suspend fun getApplication(id: Long): WrenchApplication?

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE id = (:id)")
    fun getApplicationFlow(id: Long): Flow<WrenchApplication>

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE packageName IN (:packageName)")
    fun loadByPackageName(packageName: String): WrenchApplication?

    @Insert
    fun insert(application: WrenchApplication): Long

    @Delete
    suspend fun delete(application: WrenchApplication)

    @Update
    suspend fun update(copy: WrenchApplication)
}
