package se.eelde.toggles.provider

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class TogglesProviderModule {
    @ContributesAndroidInjector
    internal abstract fun contributesTogglesProvider(): TogglesProvider

    companion object {
        @Provides
        fun providePackageManagerWrapper(application: Application): IPackageManagerWrapper = PackageManagerWrapper(application.packageManager)
    }
}