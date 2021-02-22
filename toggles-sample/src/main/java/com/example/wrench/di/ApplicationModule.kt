package com.example.wrench.di

import android.app.Application
import com.izettle.wrench.preferences.ITogglesPreferences
import com.izettle.wrench.preferences.TogglesPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    fun provideWrenchPreferences(application: Application): ITogglesPreferences = TogglesPreferences(application)
}
