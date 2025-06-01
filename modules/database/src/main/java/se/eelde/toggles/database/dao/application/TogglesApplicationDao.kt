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

    @Delete
    suspend fun delete(application: TogglesApplication)
}
