package com.izettle.wrench.di

import android.app.Application
import androidx.room.Room
import com.izettle.wrench.database.WrenchDatabase
import com.izettle.wrench.database.migrations.Migrations
import com.izettle.wrench.preferences.ITogglesPreferences
import com.izettle.wrench.preferences.TogglesPreferences
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
object ApplicationModule {

    @Singleton
    @Provides
    fun provideWrenchDb(application: Application): WrenchDatabase {
        return Room.databaseBuilder(application, WrenchDatabase::class.java, "wrench_database.db")
                .addMigrations(Migrations.MIGRATION_1_2)
                .addMigrations(Migrations.MIGRATION_2_3)
                .build()
    }

    @Singleton
    @Provides
    fun provideWrenchApplicationDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.applicationDao()

    @Singleton
    @Provides
    fun provideWrenchConfigurationDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.configurationDao()

    @Singleton
    @Provides
    fun provideWrenchConfigurationValueDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.configurationValueDao()

    @Singleton
    @Provides
    fun provideWrenchScopeDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.scopeDao()

    @Singleton
    @Provides
    fun providePredefinedConfigurationValueDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.predefinedConfigurationValueDao()

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

//    @Provides
//    fun providePackageManagerWrapper(application: Application): IPackageManagerWrapper = PackageManagerWrapper(application.packageManager)

    @Provides
    fun providesWrenchPreferences(application: Application): ITogglesPreferences = TogglesPreferences(application)
}
