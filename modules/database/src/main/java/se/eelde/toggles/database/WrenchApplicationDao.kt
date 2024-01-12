package se.eelde.toggles.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.tables.ApplicationTable

@Dao
interface WrenchApplicationDao {

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME)
    fun getApplications(): Flow<List<WrenchApplication>>

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE id = (:id)")
    suspend fun getApplication(id: Long): WrenchApplication?

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE packageName IN (:packageName)")
    fun loadByPackageName(packageName: String): WrenchApplication?

    @Insert
    fun insert(application: WrenchApplication): Long

    @Delete
    suspend fun delete(application: WrenchApplication)
}
