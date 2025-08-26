package se.eelde.toggles.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun providesToggles(@ApplicationContext context: Context): Toggles =
        TogglesImpl(context)
}
