package se.eelde.toggles.provider

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ProviderModule {
    @Provides
    fun provideTogglesUriMatcher() = TogglesUriMatcher("se.eelde.toggles.configprovider")

    @Provides
    fun providePackageManagerWrapper(@ApplicationContext context: Context): IPackageManagerWrapper =
        PackageManagerWrapper(context.packageManager)
}
