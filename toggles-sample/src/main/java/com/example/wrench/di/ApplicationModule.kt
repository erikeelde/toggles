package com.example.wrench.di

import android.app.Application
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
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    fun provideWrenchPreferences(application: Application): ITogglesPreferences = TogglesPreferences(application)
}
