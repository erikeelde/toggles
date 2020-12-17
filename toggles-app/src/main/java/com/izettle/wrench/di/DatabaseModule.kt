package com.izettle.wrench.di

import android.content.Context
import androidx.room.Room
import com.izettle.wrench.database.WrenchDatabase
import com.izettle.wrench.database.migrations.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun provideWrenchApplicationDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.applicationDao()

    @Provides
    fun provideWrenchConfigurationDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.configurationDao()

    @Provides
    fun provideWrenchConfigurationValueDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.configurationValueDao()

    @Provides
    fun provideWrenchScopeDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.scopeDao()

    @Provides
    fun providePredefinedConfigurationValueDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.predefinedConfigurationValueDao()

    @Provides
    fun provideTogglesNotificationDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.togglesNotificationsDao()
}
