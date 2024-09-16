package se.eelde.toggles.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.WrenchDatabase

@Module
@InstallIn(SingletonComponent::class)
object ProviderDaoModule {
    @Provides
    fun provideProviderApplicationDao(wrenchDatabase: WrenchDatabase) =
        wrenchDatabase.providerApplicationDao()

    @Provides
    fun provideProviderConfigurationDao(wrenchDatabase: WrenchDatabase) =
        wrenchDatabase.providerConfigurationDao()

    @Provides
    fun provideProviderConfigurationValueDao(wrenchDatabase: WrenchDatabase) =
        wrenchDatabase.providerConfigurationValueDao()

    @Provides
    fun provideProviderScopeDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.providerScopeDao()

    @Provides
    fun provideProviderPredefinedConfigurationValueDao(wrenchDatabase: WrenchDatabase) =
        wrenchDatabase.providerPredefinedConfigurationValueDao()
}
