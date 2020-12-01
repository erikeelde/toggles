package com.izettle.wrench.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.izettle.wrench.database.tables.ApplicationTable
import com.izettle.wrench.database.tables.ConfigurationTable

@Dao
interface TogglesNotificationDao {

    @Query("SELECT * FROM TogglesNotification INNER JOIN ${ApplicationTable.TABLE_NAME} on application.id = TogglesNotification.applicationId GROUP BY applicationId")
    fun getApplicationsWithPendingNotifications(): List<WrenchApplication>

    @Query("SELECT * FROM TogglesNotification WHERE applicationPackageName = :applicationPackageName")
    fun getPendingNotificationsForApplication(applicationPackageName: String): List<TogglesNotification>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(togglesNotification: TogglesNotification): Long

    @Delete
    suspend fun delete(application: WrenchApplication)
}
