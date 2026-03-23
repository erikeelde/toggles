package se.eelde.toggles.provider.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.provider.IPackageManagerWrapper
import se.eelde.toggles.provider.PackageManagerWrapper
import se.eelde.toggles.provider.TogglesUriMatcher
import javax.inject.Singleton
import kotlin.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object TestProviderModule {
    @Singleton
    @Provides
    fun provideTogglesUriMatcher() = TogglesUriMatcher("se.eelde.toggles.configprovider")

    @Provides
    fun providePackageManagerWrapper(@ApplicationContext context: Context): IPackageManagerWrapper =
        PackageManagerWrapper(context.packageManager)

    @Provides
    fun provideClock(): Clock = Clock.System
}
