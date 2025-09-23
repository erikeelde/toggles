package se.eelde.toggles

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import se.eelde.toggles.database.DatabaseModule
import se.eelde.toggles.database.TogglesDatabase
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {
    @Singleton
    @Provides
    fun provideTogglesDb(@ApplicationContext context: Context): TogglesDatabase {
        return Room.inMemoryDatabaseBuilder(context, TogglesDatabase::class.java)
            .allowMainThreadQueries().build()
    }
}