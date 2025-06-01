package se.eelde.toggles.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.TogglesDatabase

@Module
@InstallIn(SingletonComponent::class)
object ApplicationDaoModule {
    @Provides
    fun provideTogglesApplicationDao(togglesDatabase: TogglesDatabase) =
        togglesDatabase.togglesApplicationDao()

    @Provides
    fun provideTogglesConfigurationDao(togglesDatabase: TogglesDatabase) =
        togglesDatabase.togglesConfigurationDao()

    @Provides
    fun provideTogglesConfigurationValueDao(togglesDatabase: TogglesDatabase) =
        togglesDatabase.togglesConfigurationValueDao()

    @Provides
    fun provideTogglesScopeDao(togglesDatabase: TogglesDatabase) = togglesDatabase.togglesScopeDao()

    @Provides
    fun providePredefinedConfigurationValueDao(togglesDatabase: TogglesDatabase) =
        togglesDatabase.togglesPredefinedConfigurationValueDao()
}
