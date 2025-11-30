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
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.database.migrations.Migrations.LEGACY_SCOPE_NAME
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_1_2
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_2_3
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_3_4
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_4_5
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_5_6
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_6_7
import se.eelde.toggles.database.migrations.Migrations.MIGRATION_7_8
import se.eelde.toggles.database.tables.ConfigurationTable
import java.io.IOException

@RunWith(AndroidJUnit4::class)
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
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 7)

        val applicationId = DatabaseHelper.insertApplication(
            originalDb,
            "TestApplication",
            "se.eelde.toggles.application",
            "se.eelde.toggles.application",
        )

        val scopeId = DatabaseHelper.insertScope(
            originalDb,
            applicationId,
            TogglesScope.SCOPE_DEFAULT
        )

        val configId = DatabaseHelper.insertConfiguration(
            originalDb,
            applicationId,
            "test_key",
            Toggle.TYPE.BOOLEAN
        )

        // Insert multiple values for the same config+scope (old schema allowed this)
        DatabaseHelper.insertConfigurationValue(originalDb, configId, "false", scopeId)
        DatabaseHelper.insertConfigurationValue(originalDb, configId, "true", scopeId)

        val upgradedDatabase =
            testHelper.runMigrationsAndValidate(TEST_DB_NAME, 8, true, MIGRATION_7_8)

        // After migration, should have only one value per config+scope
        val values = DatabaseHelper.getConfigurationValues(upgradedDatabase, configId, scopeId)
        assertEquals("Migration should leave only one value per config+scope", 1, values.size)
    }

    companion object {
        private const val TEST_DB_NAME = "test_db"
    }
}
