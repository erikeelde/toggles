package se.eelde.toggles.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.flow.Toggles
import se.eelde.toggles.flow.TogglesImpl

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun providesToggles(@ApplicationContext context: Context): Toggles =
        TogglesImpl(context)
}
