package se.eelde.toggles.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.TogglesDatabase

@Module
@InstallIn(SingletonComponent::class)
object ProviderDaoModule {
    @Provides
    fun provideProviderApplicationDao(togglesDatabase: TogglesDatabase) =
        togglesDatabase.providerApplicationDao()

    @Provides
    fun provideProviderConfigurationDao(togglesDatabase: TogglesDatabase) =
        togglesDatabase.providerConfigurationDao()

    @Provides
    fun provideProviderConfigurationValueDao(togglesDatabase: TogglesDatabase) =
        togglesDatabase.providerConfigurationValueDao()

    @Provides
    fun provideProviderScopeDao(togglesDatabase: TogglesDatabase) = togglesDatabase.providerScopeDao()

    @Provides
    fun provideProviderPredefinedConfigurationValueDao(togglesDatabase: TogglesDatabase) =
        togglesDatabase.providerPredefinedConfigurationValueDao()
}
