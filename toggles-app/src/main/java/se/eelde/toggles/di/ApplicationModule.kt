package se.eelde.toggles.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import se.eelde.toggles.prefs.TogglesPreferences
import se.eelde.toggles.prefs.TogglesPreferencesImpl
import se.eelde.toggles.provider.IPackageManagerWrapper
import se.eelde.toggles.provider.PackageManagerWrapper

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    fun providePackageManagerWrapper(@ApplicationContext context: Context): IPackageManagerWrapper =
        PackageManagerWrapper(context.packageManager)

    @Provides
    fun providesWrenchPreferences(@ApplicationContext context: Context): TogglesPreferences =
        TogglesPreferencesImpl(context)
}
