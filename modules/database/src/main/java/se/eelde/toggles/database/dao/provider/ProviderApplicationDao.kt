package se.eelde.toggles.database.dao.provider

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.tables.ApplicationTable

@Dao
interface ProviderApplicationDao {

    @Query("SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE packageName IN (:packageName)")
    fun loadByPackageName(packageName: String): TogglesApplication?

    @Insert
    fun insert(application: TogglesApplication): Long
}
