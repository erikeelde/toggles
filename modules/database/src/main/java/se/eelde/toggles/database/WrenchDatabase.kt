package se.eelde.toggles.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        WrenchApplication::class,
        WrenchConfiguration::class,
        WrenchConfigurationValue::class,
        WrenchPredefinedConfigurationValue::class,
        WrenchScope::class,
    ],
    version = 6
)
@TypeConverters(RoomDateConverter::class)
abstract class WrenchDatabase : RoomDatabase() {

    abstract fun applicationDao(): WrenchApplicationDao

    abstract fun configurationDao(): WrenchConfigurationDao

    abstract fun configurationValueDao(): WrenchConfigurationValueDao

    abstract fun predefinedConfigurationValueDao(): WrenchPredefinedConfigurationValueDao

    abstract fun scopeDao(): WrenchScopeDao
}
