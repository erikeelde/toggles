package se.eelde.toggles.database

import android.content.Context
import androidx.room.Room

object FakeWrenchDatabase {
    fun create(context: Context): WrenchDatabase {
        return Room
            .inMemoryDatabaseBuilder(context, WrenchDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}