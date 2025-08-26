package se.eelde.toggles.database

import android.content.Context
import androidx.room.Room

object FakeTogglesDatabase {
    fun create(context: Context): TogglesDatabase {
        return Room
            .inMemoryDatabaseBuilder(context, TogglesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}