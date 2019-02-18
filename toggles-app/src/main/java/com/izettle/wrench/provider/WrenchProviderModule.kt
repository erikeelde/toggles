package com.izettle.wrench.provider

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class WrenchProviderModule {
    @ContributesAndroidInjector
    internal abstract fun contributesProvider(): WrenchProvider
}