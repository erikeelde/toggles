package com.example.wrench.di

import android.app.Application
import se.eelde.toggles.TogglesPreferences
import se.eelde.toggles.TogglesPreferencesImpl
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
    fun provideWrenchPreferences(application: Application): TogglesPreferences = TogglesPreferencesImpl(application)
}
