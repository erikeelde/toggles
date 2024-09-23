package se.eelde.toggles.example.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import se.eelde.toggles.flow.Toggles
import se.eelde.toggles.flow.TogglesImpl

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    fun provideTogglesFlow(application: Application): Toggles = TogglesImpl(application)
}
