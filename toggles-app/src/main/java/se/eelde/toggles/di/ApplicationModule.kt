package se.eelde.toggles.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import se.eelde.toggles.BuildConfig
import se.eelde.toggles.flow.Toggles
import se.eelde.toggles.flow.TogglesImpl
import se.eelde.toggles.provider.IPackageManagerWrapper
import se.eelde.toggles.provider.PackageManagerWrapper
import se.eelde.toggles.provider.TogglesUriMatcher

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun provideTogglesUriMatcher() = TogglesUriMatcher(BuildConfig.CONFIG_AUTHORITY)

    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    fun providePackageManagerWrapper(@ApplicationContext context: Context): IPackageManagerWrapper =
        PackageManagerWrapper(context.packageManager)

    @Provides
    fun providesToggles(@ApplicationContext context: Context): Toggles =
        TogglesImpl(context)
}
