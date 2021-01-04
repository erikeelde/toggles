package com.izettle.wrench.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class TogglesNotification constructor(
    @PrimaryKey
    val id: Long = 0,
    val applicationId: Long,
    val applicationPackageName: String,
    val configurationId: Long,
    val configurationKey: String,
    val configurationValue: String,
    val added: Date,
)
