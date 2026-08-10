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
    private const val databaseVersion9 = 9
    private const val databaseVersion10 = 10
    private const val databaseVersion11 = 11
    private const val databaseVersion12 = 12

    /**
     * A configuration key is the sole identifier of a toggle, so a row whose key is null - or
     * blank, which the unique `(applicationId, configurationKey)` index treats as a single shared
     * identity - is unreachable by any client. The trim set mirrors Kotlin's `isNotBlank()`;
     * SQLite's bare `TRIM()` strips only spaces.
     */
    private const val UNREACHABLE_KEY =
        "configurationKey IS NULL OR TRIM(configurationKey, ' ' || char(9) || char(10) || char(13)) = ''"

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

                // Rebuild table with tightened unique constraint: (configurationId, scope) instead
                // of (configurationId, value, scope). One value per toggle per scope.
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configurationId` INTEGER NOT NULL, `value` TEXT, `scope` INTEGER NOT NULL, FOREIGN KEY(`configurationId`) REFERENCES `configuration`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_configurationValue_temp_configurationId_scope` ON `$tableNameTemp` (`configurationId`, `scope`)"
                )

                // Keep only the most-recent row per (configurationId, scope) pair.
                db.execSQL(
                    "INSERT INTO $tableNameTemp SELECT id, configurationId, value, scope FROM $tableName WHERE id IN (SELECT MAX(id) FROM $tableName GROUP BY configurationId, scope)"
                )
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_configurationValue_temp_configurationId_scope`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configurationValue_configurationId_scope` ON `$tableNameTemp` (`configurationId`, `scope`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }

    val MIGRATION_8_9: Migration = object : Migration(databaseVersion8, databaseVersion9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE application ADD COLUMN agentControlEnabled INTEGER NOT NULL DEFAULT 1"
            )
        }
    }

    val MIGRATION_9_10: Migration = object : Migration(databaseVersion9, databaseVersion10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                val tableName = "configurationValue"
                val tableNameTemp = tableName + "_temp"

                // Rebuild table to add a foreign key from scope -> scope(id) ON DELETE CASCADE, so
                // deleting a scope now also deletes its configurationValue rows instead of leaving
                // them orphaned. Pre-existing orphans (rows whose scope no longer exists — possible
                // at v9 since no FK enforced this) are filtered out of the copy: they are unreachable
                // by resolution (TogglesProvider / the agent API only ever consult an application's
                // selected and default scopes, both of which are live rows), so dropping them cannot
                // change any observed value.
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configurationId` INTEGER NOT NULL, `value` TEXT, `scope` INTEGER NOT NULL, FOREIGN KEY(`configurationId`) REFERENCES `configuration`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`scope`) REFERENCES `scope`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_configurationValue_temp_configurationId_scope` ON `$tableNameTemp` (`configurationId`, `scope`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_configurationValue_temp_scope` ON `$tableNameTemp` (`scope`)"
                )

                db.execSQL(
                    "INSERT INTO $tableNameTemp SELECT id, configurationId, value, scope FROM $tableName WHERE scope IN (SELECT id FROM scope)"
                )
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_configurationValue_temp_configurationId_scope`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configurationValue_configurationId_scope` ON `$tableNameTemp` (`configurationId`, `scope`)"
                )

                db.execSQL("DROP INDEX `index_configurationValue_temp_scope`")
                db.execSQL(
                    "CREATE INDEX `index_configurationValue_scope` ON `$tableNameTemp` (`scope`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }

    val MIGRATION_10_11: Migration = object : Migration(databaseVersion10, databaseVersion11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                // Drop children of unreachable configurations explicitly. Both child tables
                // declare ON DELETE CASCADE, but Room issues `PRAGMA foreign_keys = ON` in
                // onOpen - after onUpgrade - so foreign keys are not enforced during a migration.
                db.execSQL(
                    "DELETE FROM configurationValue WHERE configurationId IN (SELECT id FROM configuration WHERE $UNREACHABLE_KEY)"
                )
                db.execSQL(
                    "DELETE FROM predefinedConfigurationValue WHERE configurationId IN (SELECT id FROM configuration WHERE $UNREACHABLE_KEY)"
                )
            }
            run {
                val tableName = "configuration"
                val tableNameTemp = tableName + "_temp"

                // Rebuild the table with configurationKey tightened to NOT NULL.
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` INTEGER NOT NULL, `configurationKey` TEXT NOT NULL, `configurationType` TEXT NOT NULL, `lastUse` INTEGER NOT NULL, FOREIGN KEY(`applicationId`) REFERENCES `application`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_configuration_temp_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)"
                )

                db.execSQL("INSERT INTO $tableNameTemp SELECT * FROM $tableName WHERE NOT ($UNREACHABLE_KEY)")
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_configuration_temp_applicationId_configurationKey`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configuration_applicationId_configurationKey` ON `$tableNameTemp` (`applicationId`, `configurationKey`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }

    val MIGRATION_11_12: Migration = object : Migration(databaseVersion11, databaseVersion12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            run {
                val tableName = "configurationValue"
                val tableNameTemp = tableName + "_temp"

                // Rebuild the table with `value` tightened to NOT NULL. Nullable was never a
                // deliberate choice - it dates back to schema v1, and the write paths already
                // contradicted each other: updates rejected a null value (TogglesProvider used
                // requireNotNull(toggle.value) and threw), while inserts let one through
                // unchecked.
                //
                // A null-valued row is observationally identical to an absent row -
                // TogglesResolver.kt falls back to the caller's compiled-in default either way
                // (`selectedConfigValue.value ?: defaultValue`, and one line later
                // `defaultConfigValue?.value ?: defaultValue`) - so dropping null rows instead of
                // backfilling them changes nothing a consumer can observe. It is also strictly
                // better: a null row sitting in the DEFAULT scope was tripping a spurious
                // default-mismatch (`defaultConfigValue.value != defaultValue`), either silently
                // overwriting the row or calling onDefaultMismatch with an empty string. Deleting
                // it lets addDefaultAutomatically recreate the row properly instead. Backfilling
                // was considered and rejected: any invented value would differ from the app's
                // real default and trip that very same mismatch path.
                //
                // configurationValue is a child table only - its foreign keys point out to
                // `configuration` and `scope`, nothing references configurationValue as a parent
                // - so dropping it here cannot cascade-delete any other table's rows.
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$tableNameTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configurationId` INTEGER NOT NULL, `value` TEXT NOT NULL, `scope` INTEGER NOT NULL, FOREIGN KEY(`configurationId`) REFERENCES `configuration`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`scope`) REFERENCES `scope`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_configurationValue_temp_configurationId_scope` ON `$tableNameTemp` (`configurationId`, `scope`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_configurationValue_temp_scope` ON `$tableNameTemp` (`scope`)"
                )

                db.execSQL(
                    "INSERT INTO $tableNameTemp SELECT id, configurationId, value, scope FROM $tableName WHERE value IS NOT NULL"
                )
                db.execSQL("DROP TABLE $tableName")

                db.execSQL("DROP INDEX `index_configurationValue_temp_configurationId_scope`")
                db.execSQL(
                    "CREATE UNIQUE INDEX `index_configurationValue_configurationId_scope` ON `$tableNameTemp` (`configurationId`, `scope`)"
                )

                db.execSQL("DROP INDEX `index_configurationValue_temp_scope`")
                db.execSQL(
                    "CREATE INDEX `index_configurationValue_scope` ON `$tableNameTemp` (`scope`)"
                )

                db.execSQL("ALTER TABLE $tableNameTemp RENAME TO $tableName")
            }
        }
    }
}
