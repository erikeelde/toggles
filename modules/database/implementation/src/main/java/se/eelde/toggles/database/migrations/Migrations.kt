@file:Suppress("MaxLineLength", "LongMethod")

package se.eelde.toggles.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    internal const val LEGACY_SCOPE_NAME = "wrench_default"

    private const val databaseVersion1 = 1
    private const val databaseVersion2 = 2
    private const val databaseVersion3 = 3
    private const val databaseVersion4 = 4
    private const val databaseVersion5 = 5
    private const val databaseVersion6 = 6
    private const val databaseVersion7 = 7
    private const val databaseVersion8 = 8

    val MIGRATION_1_2: Migration = object : Migration(databaseVersion1, databaseVersion2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                val tableName = "application"
                val tableNameTemp = tableName + "_temp"

                // create new table with temp name and temp index
                db.execSQL(
                    "CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT, `applicationLabel` TEXT)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_application_temp_packageName` ON `$tableNameTemp` (`packageName`)"
                )

                // copy data from old table + drop it
                db.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                db.execSQL("DROP TABLE $tableName")

                // recreate index with correct name
                db.execSQL("DROP INDEX `index_application_temp_packageName`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_application_packageName` ON `$tableNameTemp` (`packageName`)"
                )

                // rename database
                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "configuration"
                val tableNameTemp = tableName + "_temp"

                db.execSQL(
                    "CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` INTEGER NOT NULL, `configurationKey` TEXT, `configurationType` TEXT, FOREIGN KEY(`applicationId`) REFERENCES `application`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configuration_temp_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)"
                )

                db.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_configuration_temp_applicationId_configurationKey`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configuration_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "configurationValue"
                val tableNameTemp = tableName + "_temp"

                db.execSQL(
                    "CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configurationId` INTEGER NOT NULL, `value` TEXT, `scope` INTEGER NOT NULL, FOREIGN KEY(`configurationId`) REFERENCES `configuration`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configurationValue_temp_configurationId_value_scope` ON `$tableNameTemp` (`configurationId`, `value`, `scope`)"
                )

                db.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_configurationValue_temp_configurationId_value_scope`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configurationValue_configurationId_value_scope` ON `$tableNameTemp` (`configurationId`, `value`, `scope`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "predefinedConfigurationValue"
                val tableNameTemp = tableName + "_temp"

                db.execSQL(
                    "CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configurationId` INTEGER NOT NULL, `value` TEXT, FOREIGN KEY(`configurationId`) REFERENCES `configuration`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE  INDEX `index_predefinedConfigurationValue_temp_configurationId` ON `$tableNameTemp` (`configurationId`)"
                )

                db.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_predefinedConfigurationValue_temp_configurationId`")
                db.execSQL(
                    "CREATE  INDEX `index_predefinedConfigurationValue_configurationId` ON `$tableNameTemp` (`configurationId`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "scope"
                val tableNameTemp = tableName + "_temp"

                db.execSQL(
                    "CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` INTEGER NOT NULL, `name` TEXT, `selectedTimestamp` INTEGER, FOREIGN KEY(`applicationId`) REFERENCES `application`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_scope_temp_applicationId_name` ON `$tableNameTemp` (`applicationId`, `name`)"
                )

                db.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_scope_temp_applicationId_name`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_scope_applicationId_name` ON `$tableNameTemp` (`applicationId`, `name`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }

    val MIGRATION_2_3: Migration = object : Migration(databaseVersion2, databaseVersion3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                // Reinstate indexes - due to a bug in a previous migration (1 -> 2) these indexes may be missing.
                // This will recreate them in case they were missing so that migration can progress
                db.execSQL("DROP INDEX IF EXISTS `index_configurationValue_configurationId_value_scope`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configurationValue_configurationId_value_scope` ON `configurationValue` (`configurationId`, `value`, `scope`)"
                )

                db.execSQL("DROP INDEX IF EXISTS `index_predefinedConfigurationValue_configurationId`")
                db.execSQL(
                    "CREATE  INDEX `index_predefinedConfigurationValue_configurationId` ON `predefinedConfigurationValue` (`configurationId`)"
                )
            }

            run {
                val tableName = "application"
                val tableNameTemp = tableName + "_temp"

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT NOT NULL, `applicationLabel` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_application_temp_packageName` ON `$tableNameTemp` (`packageName`)"
                )

                db.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                db.execSQL("DROP TABLE $tableName")

                // recreate index with correct name
                db.execSQL("DROP INDEX `index_application_temp_packageName`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_application_packageName` ON `$tableNameTemp` (`packageName`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "configuration"
                val tableNameTemp = tableName + "_temp"

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` INTEGER NOT NULL, `configurationKey` TEXT, `configurationType` TEXT NOT NULL, `lastUse` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`applicationId`) REFERENCES `application`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configuration_temp_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)"
                )

                db.execSQL(
                    "INSERT INTO $tableNameTemp SELECT id, applicationId, configurationKey, configurationType, 0 FROM $tableName"
                )
                db.execSQL(
                    "UPDATE $tableNameTemp SET configurationType='integer' WHERE configurationType='java.lang.Integer'"
                )
                db.execSQL(
                    "UPDATE $tableNameTemp SET configurationType='string' WHERE configurationType='java.lang.String'"
                )
                db.execSQL(
                    "UPDATE $tableNameTemp SET configurationType='boolean' WHERE configurationType='java.lang.Boolean'"
                )
                db.execSQL(
                    "UPDATE $tableNameTemp SET configurationType='enum' WHERE configurationType='java.lang.Enum'"
                )
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_configuration_temp_applicationId_configurationKey`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configuration_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "scope"
                val tableNameTemp = tableName + "_temp"

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` INTEGER NOT NULL, `name` TEXT NOT NULL, `selectedTimestamp` INTEGER NOT NULL, FOREIGN KEY(`applicationId`) REFERENCES `application`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_scope_temp_applicationId_name` ON `$tableNameTemp` (`applicationId`, `name`)"
                )

                db.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_scope_temp_applicationId_name`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_scope_applicationId_name` ON `$tableNameTemp` (`applicationId`, `name`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }

    val MIGRATION_3_4: Migration = object : Migration(databaseVersion3, databaseVersion4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                val tableName = "TogglesNotification"
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableName` (`id` INTEGER NOT NULL, `applicationId` INTEGER NOT NULL, `applicationPackageName` TEXT NOT NULL, `configurationId` INTEGER NOT NULL, `configurationKey` TEXT NOT NULL, `configurationValue` TEXT NOT NULL, `added` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
            run {
                val tableName = "application"
                val tableNameTemp = tableName + "_temp"

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `shortcutId` TEXT NOT NULL, `packageName` TEXT NOT NULL, `applicationLabel` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_application_temp_packageName` ON `$tableNameTemp` (`packageName`)"
                )

                db.execSQL(
                    "INSERT INTO $tableNameTemp (id, shortcutId, packageName, applicationLabel) SELECT id, packageName,  packageName, applicationLabel FROM $tableName"
                )
                db.execSQL("DROP TABLE $tableName")

                // recreate index with correct name
                db.execSQL("DROP INDEX `index_application_temp_packageName`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_application_packageName` ON `$tableNameTemp` (`packageName`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }
    val MIGRATION_4_5: Migration = object : Migration(databaseVersion4, databaseVersion5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                val tableName = "TogglesNotification"
                db.execSQL("DROP TABLE IF  EXISTS `$tableName`")
            }
        }
    }
    val MIGRATION_5_6: Migration = object : Migration(databaseVersion5, databaseVersion6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                val tableName = "predefinedConfigurationValue"
                val tableNameTemp = tableName + "_temp"

                val newIndexName = "index_predefinedConfigurationValue_configurationId_value"

                // create new table with temp name and temp index
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configurationId` INTEGER NOT NULL, `value` TEXT, FOREIGN KEY(`configurationId`) REFERENCES `configuration`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `$newIndexName` ON `$tableNameTemp` (`configurationId`, `value`);"
                )

                // copy data from old table + drop it
                db.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName GROUP BY configurationId, value")
                db.execSQL("DROP TABLE $tableName")

                // rename database
                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }

    val MIGRATION_6_7: Migration = object : Migration(databaseVersion6, databaseVersion7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                val tableName = "scope"

                db.execSQL(
                    "UPDATE $tableName SET name='toggles_default' WHERE name='$LEGACY_SCOPE_NAME'"
                )
            }
        }
    }

    val MIGRATION_7_8: Migration = object : Migration(databaseVersion7, databaseVersion8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                val tableName = "configurationValue"
                val tableNameTemp = tableName + "_temp"

                // Create new table with updated unique constraint (configurationId, scope) instead of (configurationId, value, scope)
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configurationId` INTEGER NOT NULL, `value` TEXT, `scope` INTEGER NOT NULL, FOREIGN KEY(`configurationId`) REFERENCES `configuration`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configurationValue_temp_configurationId_scope` ON `$tableNameTemp` (`configurationId`, `scope`)"
                )

                // Migrate data - for each (configurationId, scope) keep only the row with highest id (most recent)
                // This handles cases where multiple values existed for the same config+scope
                db.execSQL(
                    "INSERT INTO $tableNameTemp (id, configurationId, value, scope) " +
                        "SELECT t1.id, t1.configurationId, t1.value, t1.scope " +
                        "FROM $tableName t1 " +
                        "INNER JOIN (" +
                        "  SELECT MAX(id) as maxId, configurationId, scope " +
                        "  FROM $tableName " +
                        "  GROUP BY configurationId, scope" +
                        ") t2 ON t1.id = t2.maxId"
                )

                // Drop old table
                db.execSQL("DROP TABLE $tableName")

                // Recreate index with correct name
                db.execSQL("DROP INDEX IF EXISTS `index_configurationValue_temp_configurationId_scope`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configurationValue_configurationId_scope` ON `$tableNameTemp` (`configurationId`, `scope`)"
                )

                // Rename temp table to final name
                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }
}
