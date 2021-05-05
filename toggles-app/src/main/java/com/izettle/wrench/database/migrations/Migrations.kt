package com.izettle.wrench.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    const val databaseVersion1 = 1
    const val databaseVersion2 = 2
    const val databaseVersion3 = 3
    const val databaseVersion4 = 4
    const val databaseVersion5 = 5

    val MIGRATION_1_2: Migration = object : Migration(databaseVersion1, databaseVersion2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            run {
                val tableName = "application"
                val tableNameTemp = tableName + "_temp"

                // create new table with temp name and temp index
                database.execSQL("CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT, `applicationLabel` TEXT)")
                database.execSQL("CREATE UNIQUE INDEX `index_application_temp_packageName` ON `$tableNameTemp` (`packageName`)")

                // copy data from old table + drop it
                database.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                database.execSQL("DROP TABLE $tableName")

                // recreate index with correct name
                database.execSQL("DROP INDEX `index_application_temp_packageName`")
                database.execSQL("CREATE UNIQUE INDEX `index_application_packageName` ON `$tableNameTemp` (`packageName`)")

                // rename database
                database.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "configuration"
                val tableNameTemp = tableName + "_temp"

                database.execSQL("CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` INTEGER NOT NULL, `configurationKey` TEXT, `configurationType` TEXT, FOREIGN KEY(`applicationId`) REFERENCES `application`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE UNIQUE INDEX `index_configuration_temp_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)")

                database.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                database.execSQL("DROP TABLE $tableName")

                database.execSQL("DROP INDEX `index_configuration_temp_applicationId_configurationKey`")
                database.execSQL("CREATE UNIQUE INDEX `index_configuration_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)")

                database.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "configurationValue"
                val tableNameTemp = tableName + "_temp"

                database.execSQL("CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configurationId` INTEGER NOT NULL, `value` TEXT, `scope` INTEGER NOT NULL, FOREIGN KEY(`configurationId`) REFERENCES `configuration`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE UNIQUE INDEX `index_configurationValue_temp_configurationId_value_scope` ON `$tableNameTemp` (`configurationId`, `value`, `scope`)")

                database.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                database.execSQL("DROP TABLE $tableName")

                database.execSQL("DROP INDEX `index_configurationValue_temp_configurationId_value_scope`")
                database.execSQL("CREATE UNIQUE INDEX `index_configurationValue_configurationId_value_scope` ON `$tableNameTemp` (`configurationId`, `value`, `scope`)")

                database.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "predefinedConfigurationValue"
                val tableNameTemp = tableName + "_temp"

                database.execSQL("CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configurationId` INTEGER NOT NULL, `value` TEXT, FOREIGN KEY(`configurationId`) REFERENCES `configuration`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE  INDEX `index_predefinedConfigurationValue_temp_configurationId` ON `$tableNameTemp` (`configurationId`)")

                database.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                database.execSQL("DROP TABLE $tableName")

                database.execSQL("DROP INDEX `index_predefinedConfigurationValue_temp_configurationId`")
                database.execSQL("CREATE  INDEX `index_predefinedConfigurationValue_configurationId` ON `$tableNameTemp` (`configurationId`)")

                database.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "scope"
                val tableNameTemp = tableName + "_temp"

                database.execSQL("CREATE TABLE `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` INTEGER NOT NULL, `name` TEXT, `selectedTimestamp` INTEGER, FOREIGN KEY(`applicationId`) REFERENCES `application`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE UNIQUE INDEX `index_scope_temp_applicationId_name` ON `$tableNameTemp` (`applicationId`, `name`)")

                database.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                database.execSQL("DROP TABLE $tableName")

                database.execSQL("DROP INDEX `index_scope_temp_applicationId_name`")
                database.execSQL("CREATE UNIQUE INDEX `index_scope_applicationId_name` ON `$tableNameTemp` (`applicationId`, `name`)")

                database.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }

    val MIGRATION_2_3: Migration = object : Migration(databaseVersion2, databaseVersion3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            run {
                // Reinstate indexes - due to a bug in a previous migration (1 -> 2) these indexes may be missing.
                // This will recreate them in case they were missing so that migration can progress
                database.execSQL("DROP INDEX IF EXISTS `index_configurationValue_configurationId_value_scope`")
                database.execSQL("CREATE UNIQUE INDEX `index_configurationValue_configurationId_value_scope` ON `configurationValue` (`configurationId`, `value`, `scope`)")

                database.execSQL("DROP INDEX IF EXISTS `index_predefinedConfigurationValue_configurationId`")
                database.execSQL("CREATE  INDEX `index_predefinedConfigurationValue_configurationId` ON `predefinedConfigurationValue` (`configurationId`)")
            }

            run {
                val tableName = "application"
                val tableNameTemp = tableName + "_temp"

                database.execSQL("CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT NOT NULL, `applicationLabel` TEXT NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX `index_application_temp_packageName` ON `$tableNameTemp` (`packageName`)")

                database.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                database.execSQL("DROP TABLE $tableName")

                // recreate index with correct name
                database.execSQL("DROP INDEX `index_application_temp_packageName`")
                database.execSQL("CREATE UNIQUE INDEX `index_application_packageName` ON `$tableNameTemp` (`packageName`)")

                database.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "configuration"
                val tableNameTemp = tableName + "_temp"

                database.execSQL("CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` INTEGER NOT NULL, `configurationKey` TEXT, `configurationType` TEXT NOT NULL, `lastUse` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`applicationId`) REFERENCES `application`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE UNIQUE INDEX `index_configuration_temp_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)")

                database.execSQL("INSERT INTO $tableNameTemp SELECT id, applicationId, configurationKey, configurationType, 0 FROM $tableName")
                database.execSQL("UPDATE $tableNameTemp SET configurationType='integer' WHERE configurationType='java.lang.Integer'")
                database.execSQL("UPDATE $tableNameTemp SET configurationType='string' WHERE configurationType='java.lang.String'")
                database.execSQL("UPDATE $tableNameTemp SET configurationType='boolean' WHERE configurationType='java.lang.Boolean'")
                database.execSQL("UPDATE $tableNameTemp SET configurationType='enum' WHERE configurationType='java.lang.Enum'")
                database.execSQL("DROP TABLE $tableName")

                database.execSQL("DROP INDEX `index_configuration_temp_applicationId_configurationKey`")
                database.execSQL("CREATE UNIQUE INDEX `index_configuration_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)")

                database.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
            run {
                val tableName = "scope"
                val tableNameTemp = tableName + "_temp"

                database.execSQL("CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` INTEGER NOT NULL, `name` TEXT NOT NULL, `selectedTimestamp` INTEGER NOT NULL, FOREIGN KEY(`applicationId`) REFERENCES `application`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE UNIQUE INDEX `index_scope_temp_applicationId_name` ON `$tableNameTemp` (`applicationId`, `name`)")

                database.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName")
                database.execSQL("DROP TABLE $tableName")

                database.execSQL("DROP INDEX `index_scope_temp_applicationId_name`")
                database.execSQL("CREATE UNIQUE INDEX `index_scope_applicationId_name` ON `$tableNameTemp` (`applicationId`, `name`)")

                database.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }
    val MIGRATION_3_4: Migration = object : Migration(databaseVersion3, databaseVersion4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            run {
                val tableName = "TogglesNotification"
                database.execSQL("CREATE TABLE IF NOT EXISTS `$tableName` (`id` INTEGER NOT NULL, `applicationId` INTEGER NOT NULL, `applicationPackageName` TEXT NOT NULL, `configurationId` INTEGER NOT NULL, `configurationKey` TEXT NOT NULL, `configurationValue` TEXT NOT NULL, `added` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
            run {
                val tableName = "application"
                val tableNameTemp = tableName + "_temp"

                database.execSQL("CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `shortcutId` TEXT NOT NULL, `packageName` TEXT NOT NULL, `applicationLabel` TEXT NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX `index_application_temp_packageName` ON `$tableNameTemp` (`packageName`)")

                database.execSQL("INSERT INTO $tableNameTemp (id, shortcutId, packageName, applicationLabel) SELECT id, packageName,  packageName, applicationLabel FROM $tableName")
                database.execSQL("DROP TABLE $tableName")

                // recreate index with correct name
                database.execSQL("DROP INDEX `index_application_temp_packageName`")
                database.execSQL("CREATE UNIQUE INDEX `index_application_packageName` ON `$tableNameTemp` (`packageName`)")

                database.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }
    val MIGRATION_4_5: Migration = object : Migration(databaseVersion4, databaseVersion5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            run {
                val tableName = "TogglesNotification"
                database.execSQL("DROP TABLE IF  EXISTS `$tableName`")
            }
        }
    }
}
