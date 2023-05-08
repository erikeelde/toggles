package com.example.toggles.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import se.eelde.toggles.flow.Toggles
import se.eelde.toggles.flow.TogglesImpl
import se.eelde.toggles.prefs.TogglesPreferences
import se.eelde.toggles.prefs.TogglesPreferencesImpl

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    fun provideTogglesPreferences(application: Application): TogglesPreferences = TogglesPreferencesImpl(application)

    @Provides
    fun provideTogglesFlow(application: Application): Toggles = TogglesImpl(application)
}
