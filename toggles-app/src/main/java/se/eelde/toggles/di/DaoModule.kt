package se.eelde.toggles.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.WrenchDatabase

@Module
@InstallIn(SingletonComponent::class)
object ApplicationDaoModule {
    @Provides
    fun provideTogglesApplicationDao(wrenchDatabase: WrenchDatabase) =
        wrenchDatabase.togglesApplicationDao()

    @Provides
    fun provideTogglesConfigurationDao(wrenchDatabase: WrenchDatabase) =
        wrenchDatabase.togglesConfigurationDao()

    @Provides
    fun provideTogglesConfigurationValueDao(wrenchDatabase: WrenchDatabase) =
        wrenchDatabase.togglesConfigurationValueDao()

    @Provides
    fun provideTogglesScopeDao(wrenchDatabase: WrenchDatabase) = wrenchDatabase.togglesScopeDao()

    @Provides
    fun providePredefinedConfigurationValueDao(wrenchDatabase: WrenchDatabase) =
        wrenchDatabase.togglesPredefinedConfigurationValueDao()
}