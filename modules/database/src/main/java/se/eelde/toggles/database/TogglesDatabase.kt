package se.eelde.toggles.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import se.eelde.toggles.database.dao.application.TogglesApplicationDao
import se.eelde.toggles.database.dao.application.TogglesConfigurationDao
import se.eelde.toggles.database.dao.application.TogglesConfigurationValueDao
import se.eelde.toggles.database.dao.application.TogglesPredefinedConfigurationValueDao
import se.eelde.toggles.database.dao.application.TogglesScopeDao
import se.eelde.toggles.database.dao.provider.ProviderApplicationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderPredefinedConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderScopeDao

@Database(
    entities = [
        TogglesApplication::class,
        TogglesConfiguration::class,
        TogglesConfigurationValue::class,
        TogglesPredefinedConfigurationValue::class,
        TogglesScope::class,
    ],
    version = 7
)
@TypeConverters(RoomDateConverter::class)
abstract class TogglesDatabase : RoomDatabase() {

    abstract fun togglesApplicationDao(): TogglesApplicationDao
    abstract fun providerApplicationDao(): ProviderApplicationDao

    abstract fun togglesConfigurationDao(): TogglesConfigurationDao
    abstract fun providerConfigurationDao(): ProviderConfigurationDao

    abstract fun togglesConfigurationValueDao(): TogglesConfigurationValueDao
    abstract fun providerConfigurationValueDao(): ProviderConfigurationValueDao

    abstract fun togglesPredefinedConfigurationValueDao(): TogglesPredefinedConfigurationValueDao
    abstract fun providerPredefinedConfigurationValueDao(): ProviderPredefinedConfigurationValueDao

    abstract fun togglesScopeDao(): TogglesScopeDao
    abstract fun providerScopeDao(): ProviderScopeDao
}
