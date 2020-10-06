package com.izettle.wrench.di

import android.content.Context
import com.izettle.wrench.preferences.ITogglesPreferences
import com.izettle.wrench.preferences.TogglesPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import se.eelde.toggles.provider.IPackageManagerWrapper
import se.eelde.toggles.provider.PackageManagerWrapper

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    fun providePackageManagerWrapper(@ApplicationContext context: Context): IPackageManagerWrapper = PackageManagerWrapper(context.packageManager)

    @Provides
    fun provideWrenchPackageManagerWrapper(@ApplicationContext context: Context): com.izettle.wrench.provider.IPackageManagerWrapper =
        com.izettle.wrench.provider.PackageManagerWrapper(context.packageManager)

    @Provides
    fun providesWrenchPreferences(@ApplicationContext context: Context): ITogglesPreferences = TogglesPreferences(context)
}
