package se.eelde.toggles.database

import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.database.migrations.Migrations.LEGACY_SCOPE_NAME
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_10_11
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_11_12
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_1_2
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_2_3
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_3_4
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_4_5
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_5_6
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_6_7
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_7_8
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_8_9
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_9_10
import se.eelde.toggles.database.tables.ConfigurationTable
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36]) // Robolectric 4.16.x supports up to SDK 36; pin to avoid SDK-mismatch errors
class MigrationTests {
    // Unable to migrate to unitTest due to https://github.com/robolectric/robolectric/issues/2065

    @get:Rule
    var testHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TogglesDatabase::class.java,
        listOf<AutoMigrationSpec>(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun test1to2() {
        // Create the database with version 2
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 1)

        // insert data

        originalDb.close()

        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)
    }

    @Test
    @Throws(IOException::class)
    @Suppress("LongMethod")
    fun test2to3() {
        // Create the database with version 2
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 2)

        val testApplicationId = DatabaseHelper.insertApplicationPre4(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application"
        )

        // insert data
        DatabaseHelper.insertConfigurationPre3(
            originalDb,
            testApplicationId,
            "Integerkey",
            Toggle.TYPE.INTEGER
        )
        DatabaseHelper.insertConfigurationPre3(
            originalDb,
            testApplicationId,
            "Stringkey",
            Toggle.TYPE.STRING
        )
        DatabaseHelper.insertConfigurationPre3(
            originalDb,
            testApplicationId,
            "Booleankey",
            Toggle.TYPE.BOOLEAN
        )
        DatabaseHelper.insertConfigurationPre3(
            originalDb,
            testApplicationId,
            "Enumkey",
            Toggle.TYPE.ENUM
        )

        originalDb.close()

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 3, true, MIGRATION_2_3)

        var cursor = DatabaseHelper.getConfigurationByKey(migratedDb, "Integerkey")
        assertTrue(cursor.moveToFirst())
        assertEquals(
            Toggle.TYPE.INTEGER,
            cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE))
        )
        cursor.close()

        cursor = DatabaseHelper.getConfigurationByKey(migratedDb, "Stringkey")
        assertTrue(cursor.moveToFirst())
        assertEquals(
            Toggle.TYPE.STRING,
            cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE))
        )
        cursor.close()

        cursor = DatabaseHelper.getConfigurationByKey(migratedDb, "Booleankey")
        assertTrue(cursor.moveToFirst())
        assertEquals(
            Toggle.TYPE.BOOLEAN,
            cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE))
        )
        cursor.close()

        cursor = DatabaseHelper.getConfigurationByKey(migratedDb, "Enumkey")
        assertTrue(cursor.moveToFirst())
        assertEquals(
            Toggle.TYPE.ENUM,
            cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE))
        )
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun test3to4() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 3)

        DatabaseHelper.insertApplicationPre4(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.testapplication"
        )

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 4, true, MIGRATION_3_4)

        val application = DatabaseHelper.getApplication(migratedDb, 1)

        assertEquals("TestApplication", application.applicationLabel)
        assertEquals("se.eelde.toggles.testapplication", application.shortcutId)
        assertEquals("se.eelde.toggles.testapplication", application.packageName)
    }

    @Test
    @Throws(IOException::class)
    fun test4to5() {
        testHelper.createDatabase(TEST_DB_NAME, 4)
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 5, true, MIGRATION_4_5)
    }

    @Test
    @Throws(IOException::class)
    fun test5to6() {
        testHelper.createDatabase(TEST_DB_NAME, 5)
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 6, true, MIGRATION_5_6)
    }

    @Test
    @Throws(IOException::class)
    fun test5to6WithDuplicates() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 5)
        assertEquals(
            1,
            DatabaseHelper.insertApplication(
                originalDb,
                "TestApplication",
                "se.eelde.toggles.application",
                "se.eelde.toggles.application",
            )
        )

        // insert data
        assertEquals(
            1,
            DatabaseHelper.insertConfiguration(
                originalDb,
                1,
                "MyEnum",
                Toggle.TYPE.ENUM,
                0,
            )
        )

        assertEquals(1, DatabaseHelper.insertPredefinedConfigurationValue(originalDb, 1, "a"))
        assertEquals(2, DatabaseHelper.insertPredefinedConfigurationValue(originalDb, 1, "a"))
        assertEquals(3, DatabaseHelper.insertPredefinedConfigurationValue(originalDb, 1, "b"))
        assertEquals(4, DatabaseHelper.insertPredefinedConfigurationValue(originalDb, 1, "b"))
        assertEquals(5, DatabaseHelper.insertPredefinedConfigurationValue(originalDb, 1, "b"))
        assertEquals(6, DatabaseHelper.insertPredefinedConfigurationValue(originalDb, 1, "c"))

        val valuesBefore =
            DatabaseHelper.getPredefinedConfigurationValueByConfigurationId(
                db = originalDb,
                configId = 1
            )
        assertEquals(6, valuesBefore.size)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 6, true, MIGRATION_5_6)

        val values =
            DatabaseHelper.getPredefinedConfigurationValueByConfigurationId(
                db = migratedDb,
                configId = 1
            )

        assertEquals(3, values.size)
    }

    @Test
    @Throws(IOException::class)
    fun test6to7() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 6)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )

        val scopeId = DatabaseHelper.insertScope(
            originalDb,
            applicationId,
            LEGACY_SCOPE_NAME
        )

        val upgradedDatabase =
            testHelper.runMigrationsAndValidate(TEST_DB_NAME, 7, true, MIGRATION_6_7)

        val scope = DatabaseHelper.getScope(upgradedDatabase, scopeId)
        assertEquals(TogglesScope.SCOPE_DEFAULT, scope.name)
    }

    @Test
    @Throws(IOException::class)
    fun test7to8() {
        testHelper.createDatabase(TEST_DB_NAME, 7)
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 8, true, MIGRATION_7_8)
    }

    @Test
    @Throws(IOException::class)
    fun test7to8WithDuplicates() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 7)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        val scopeId = DatabaseHelper.insertScope(originalDb, applicationId, TogglesScope.SCOPE_DEFAULT)

        // Insert two rows for same (configurationId, scope) with different values — allowed by v7
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "false", scopeId)
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "true", scopeId)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 8, true, MIGRATION_7_8)

        // After migration only one row should exist per (configurationId, scope)
        val values = DatabaseHelper.getConfigurationValuesByConfigurationIdAndScope(
            migratedDb,
            configurationId,
            scopeId
        )
        assertEquals(1, values.size)
        // The most-recent (highest id) row is kept
        assertEquals("true", values[0].value)
    }

    @Test
    @Throws(IOException::class)
    fun test8to9() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 8)
        originalDb.close()

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 9, true, MIGRATION_8_9)

        migratedDb.query("SELECT agentControlEnabled FROM application").use { cursor ->
            assertEquals(0, cursor.count)
        }
    }

    @Test
    @Throws(IOException::class)
    fun test8to9DefaultsExistingApplicationsToEnabled() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 8)
        originalDb.execSQL(
            "INSERT INTO application (shortcutId, packageName, applicationLabel) " +
                "VALUES ('com.example.app', 'com.example.app', 'Example')"
        )
        originalDb.close()

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 9, true, MIGRATION_8_9)

        migratedDb.query("SELECT agentControlEnabled FROM application").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }

    @Test
    @Throws(IOException::class)
    fun test9to10() {
        testHelper.createDatabase(TEST_DB_NAME, 9)
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 10, true, MIGRATION_9_10)
    }

    @Test
    @Throws(IOException::class)
    fun test9to10ValueRowWithExistingScopeSurvives() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 9)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        val scopeId = DatabaseHelper.insertScope(originalDb, applicationId, TogglesScope.SCOPE_DEFAULT)
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "true", scopeId)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 10, true, MIGRATION_9_10)

        val values = DatabaseHelper.getConfigurationValuesByConfigurationIdAndScope(
            migratedDb,
            configurationId,
            scopeId
        )
        assertEquals(1, values.size)
        assertEquals("true", values[0].value)
    }

    @Test
    @Throws(IOException::class)
    fun test9to10RemovesPreExistingOrphanedValueRows() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 9)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        // No FK exists at v9, so a value row can reference a scope id that never existed —
        // this is the pre-existing orphan the migration must drop.
        val orphanScopeId = 999_999L
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "true", orphanScopeId)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 10, true, MIGRATION_9_10)

        val values = DatabaseHelper.getConfigurationValuesByConfigurationIdAndScope(
            migratedDb,
            configurationId,
            orphanScopeId
        )
        assertEquals(0, values.size)
    }

    @Test
    @Throws(IOException::class)
    fun test9to10DeletingScopeCascadesToItsValueRows() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 9)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        val scopeId = DatabaseHelper.insertScope(originalDb, applicationId, TogglesScope.SCOPE_DEFAULT)
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "true", scopeId)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 10, true, MIGRATION_9_10)

        // The raw framework database opened by MigrationTestHelper does not go through Room's
        // onConfigure, which is what normally enables this pragma — enable it explicitly to
        // exercise the FK's ON DELETE CASCADE clause added by MIGRATION_9_10.
        migratedDb.execSQL("PRAGMA foreign_keys=ON")
        migratedDb.execSQL("DELETE FROM scope WHERE id = ?", arrayOf<Any>(scopeId))

        val values = DatabaseHelper.getConfigurationValuesByConfigurationIdAndScope(
            migratedDb,
            configurationId,
            scopeId
        )
        assertEquals(0, values.size)
    }

    @Test
    @Throws(IOException::class)
    fun test9to10UniqueIndexOnConfigurationIdAndScopeStillExists() {
        testHelper.createDatabase(TEST_DB_NAME, 9)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 10, true, MIGRATION_9_10)

        migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' " +
                "AND name = 'index_configurationValue_configurationId_scope'"
        ).use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    @Test
    @Throws(IOException::class)
    fun test10to11() {
        testHelper.createDatabase(TEST_DB_NAME, 10)
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 11, true, MIGRATION_10_11)
    }

    @Test
    @Throws(IOException::class)
    @Suppress("LongMethod")
    fun test10to11DropsUnreachableConfigurations() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 10)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val scopeId =
            DatabaseHelper.insertScope(originalDb, applicationId, TogglesScope.SCOPE_DEFAULT)

        val reachableId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        // A key that is null, empty or whitespace-only cannot be looked up by any client.
        val unreachableIds = listOf(null, "", "   ").map { key ->
            DatabaseHelper.insertConfigurationWithNullableKey(
                originalDb,
                applicationId,
                key,
                Toggle.TYPE.BOOLEAN,
                0,
            )
        }

        (unreachableIds + reachableId).forEach { configurationId ->
            DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "true", scopeId)
            DatabaseHelper.insertPredefinedConfigurationValue(originalDb, configurationId, "true")
        }

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 11, true, MIGRATION_10_11)

        assertEquals(listOf(reachableId), DatabaseHelper.getConfigurationIds(migratedDb))

        // The reachable configuration keeps both of its children.
        assertEquals(
            1,
            DatabaseHelper.getConfigurationValueIdsByConfigurationId(migratedDb, reachableId).size
        )
        assertEquals(
            1,
            DatabaseHelper.getPredefinedConfigurationValueByConfigurationId(
                migratedDb,
                reachableId
            ).size
        )

        // The unreachable ones leave no orphaned children behind.
        unreachableIds.forEach { configurationId ->
            assertEquals(
                emptyList<Long>(),
                DatabaseHelper.getConfigurationValueIdsByConfigurationId(migratedDb, configurationId)
            )
            assertEquals(
                emptyList<TogglesPredefinedConfigurationValue>(),
                DatabaseHelper.getPredefinedConfigurationValueByConfigurationId(
                    migratedDb,
                    configurationId
                )
            )
        }
    }

    @Test
    @Throws(IOException::class)
    fun test10to11KeepsKeysWithSurroundingWhitespace() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 10)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            " my_key ",
            Toggle.TYPE.STRING,
            0,
        )

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 11, true, MIGRATION_10_11)

        assertEquals(listOf(configurationId), DatabaseHelper.getConfigurationIds(migratedDb))
    }

    @Test
    @Throws(IOException::class)
    fun test10to11ConfigurationValueSurvivesConfigurationTableRebuild() {
        // The migration rebuilds `configuration` (create temp, INSERT...SELECT, DROP TABLE,
        // rename) to tighten configurationKey to NOT NULL. `configurationValue` has a foreign
        // key configurationId -> configuration(id) ON DELETE CASCADE, so if foreign key
        // enforcement were active during the migration, SQLite's implicit delete on DROP TABLE
        // could cascade and destroy every configuration value row. This test proves a live
        // configuration's value row survives the rebuild intact.
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 10)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val scopeId =
            DatabaseHelper.insertScope(originalDb, applicationId, TogglesScope.SCOPE_DEFAULT)
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "true", scopeId)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 11, true, MIGRATION_10_11)

        val values = DatabaseHelper.getConfigurationValuesByConfigurationIdAndScope(
            migratedDb,
            configurationId,
            scopeId
        )
        assertEquals(1, values.size)
        assertEquals("true", values[0].value)
    }

    @Test
    @Throws(IOException::class)
    fun test11to12() {
        testHelper.createDatabase(TEST_DB_NAME, 11)
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 12, true, MIGRATION_11_12)
    }

    @Test
    @Throws(IOException::class)
    fun test11to12NonNullValueRowSurvivesWithValueIntact() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 11)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val scopeId =
            DatabaseHelper.insertScope(originalDb, applicationId, TogglesScope.SCOPE_DEFAULT)
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "true", scopeId)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 12, true, MIGRATION_11_12)

        val values = DatabaseHelper.getConfigurationValuesByConfigurationIdAndScope(
            migratedDb,
            configurationId,
            scopeId
        )
        assertEquals(1, values.size)
        assertEquals("true", values[0].value)
    }

    @Test
    @Throws(IOException::class)
    fun test11to12NullValueRowIsRemoved() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 11)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val scopeId =
            DatabaseHelper.insertScope(originalDb, applicationId, TogglesScope.SCOPE_DEFAULT)
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, null, scopeId)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 12, true, MIGRATION_11_12)

        assertEquals(
            emptyList<Long>(),
            DatabaseHelper.getConfigurationValueIdsByConfigurationId(migratedDb, configurationId)
        )
    }

    @Test
    @Throws(IOException::class)
    fun test11to12BothIndicesStillExist() {
        testHelper.createDatabase(TEST_DB_NAME, 11)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 12, true, MIGRATION_11_12)

        migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' " +
                "AND name = 'index_configurationValue_configurationId_scope'"
        ).use { cursor ->
            assertEquals(1, cursor.count)
        }
        migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' " +
                "AND name = 'index_configurationValue_scope'"
        ).use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    @Test
    @Throws(IOException::class)
    fun test11to12DeletingScopeCascadesToItsValueRows() {
        // Proves the scope -> scope(id) ON DELETE CASCADE foreign key survived the rebuild. A
        // rebuild that quietly dropped this FK would still pass a naive schema check but would
        // leave orphaned value rows behind on scope deletion.
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 11)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val scopeId =
            DatabaseHelper.insertScope(originalDb, applicationId, TogglesScope.SCOPE_DEFAULT)
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "true", scopeId)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 12, true, MIGRATION_11_12)

        // The raw framework database opened by MigrationTestHelper does not go through Room's
        // onConfigure, which is what normally enables this pragma - enable it explicitly to
        // exercise the FK's ON DELETE CASCADE clause.
        migratedDb.execSQL("PRAGMA foreign_keys=ON")
        migratedDb.execSQL("DELETE FROM scope WHERE id = ?", arrayOf<Any>(scopeId))

        assertEquals(
            emptyList<Long>(),
            DatabaseHelper.getConfigurationValueIdsByConfigurationId(migratedDb, configurationId)
        )
    }

    @Test
    @Throws(IOException::class)
    fun test11to12DeletingConfigurationCascadesToItsValueRows() {
        // Proves the configurationId -> configuration(id) ON DELETE CASCADE foreign key survived
        // the rebuild - the other FK the table declares, distinct from the scope FK proven above.
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 11)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )
        val scopeId =
            DatabaseHelper.insertScope(originalDb, applicationId, TogglesScope.SCOPE_DEFAULT)
        val configurationId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "MyBoolean",
            Toggle.TYPE.BOOLEAN,
            0,
        )
        DatabaseHelper.insertConfigurationValue(originalDb, configurationId, "true", scopeId)

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 12, true, MIGRATION_11_12)

        migratedDb.execSQL("PRAGMA foreign_keys=ON")
        migratedDb.execSQL("DELETE FROM configuration WHERE id = ?", arrayOf<Any>(configurationId))

        assertEquals(
            emptyList<Long>(),
            DatabaseHelper.getConfigurationValueIdsByConfigurationId(migratedDb, configurationId)
        )
    }

    companion object {
        private const val TEST_DB_NAME = "test_db"
    }
}
