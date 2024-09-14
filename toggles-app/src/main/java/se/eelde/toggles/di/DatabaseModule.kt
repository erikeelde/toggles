package se.eelde.toggles.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.WrenchDatabase
import se.eelde.toggles.database.migrations.Migrations
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideWrenchDb(@ApplicationContext context: Context): WrenchDatabase {
        return Room.databaseBuilder(context, WrenchDatabase::class.java, "wrench_database.db")
            .addMigrations(Migrations.MIGRATION_1_2)
            .addMigrations(Migrations.MIGRATION_2_3)
            .addMigrations(Migrations.MIGRATION_3_4)
            .addMigrations(Migrations.MIGRATION_4_5)
            .addMigrations(Migrations.MIGRATION_5_6)
            .build()
    }
}
