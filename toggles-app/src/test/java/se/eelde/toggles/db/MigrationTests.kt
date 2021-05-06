package se.eelde.toggles.db

import android.os.Build
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.izettle.wrench.database.WrenchDatabase
import com.izettle.wrench.database.migrations.Migrations.MIGRATION_1_2
import com.izettle.wrench.database.migrations.Migrations.MIGRATION_2_3
import com.izettle.wrench.database.migrations.Migrations.MIGRATION_3_4
import com.izettle.wrench.database.migrations.Migrations.MIGRATION_4_5
import com.izettle.wrench.database.tables.ConfigurationTable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.core.Toggle
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class MigrationTests {
    // Unable to migrate to unitTest due to https://github.com/robolectric/robolectric/issues/2065

    @get:Rule
    var testHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WrenchDatabase::class.java.canonicalName!!,
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
    fun test2to3() {
        // Create the database with version 2
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 2)

        val testApplicationId = DatabaseHelper.insertWrenchApplication(originalDb, "TestApplication", "com.izettle.wrench.testapplication")

        // insert data
        DatabaseHelper.insertWrenchConfiguration(originalDb, testApplicationId, "Integerkey", Toggle.TYPE.INTEGER)
        DatabaseHelper.insertWrenchConfiguration(originalDb, testApplicationId, "Stringkey", Toggle.TYPE.STRING)
        DatabaseHelper.insertWrenchConfiguration(originalDb, testApplicationId, "Booleankey", Toggle.TYPE.BOOLEAN)
        DatabaseHelper.insertWrenchConfiguration(originalDb, testApplicationId, "Enumkey", Toggle.TYPE.ENUM)

        originalDb.close()

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 3, true, MIGRATION_2_3)

        var cursor = DatabaseHelper.getWrenchConfigurationByKey(migratedDb, "Integerkey")
        assertTrue(cursor.moveToFirst())
        assertEquals(Toggle.TYPE.INTEGER, cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE)))
        cursor.close()

        cursor = DatabaseHelper.getWrenchConfigurationByKey(migratedDb, "Stringkey")
        assertTrue(cursor.moveToFirst())
        assertEquals(Toggle.TYPE.STRING, cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE)))
        cursor.close()

        cursor = DatabaseHelper.getWrenchConfigurationByKey(migratedDb, "Booleankey")
        assertTrue(cursor.moveToFirst())
        assertEquals(Toggle.TYPE.BOOLEAN, cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE)))
        cursor.close()

        cursor = DatabaseHelper.getWrenchConfigurationByKey(migratedDb, "Enumkey")
        assertTrue(cursor.moveToFirst())
        assertEquals(Toggle.TYPE.ENUM, cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE)))
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun test3to4() {
        val originalDb = testHelper.createDatabase(TEST_DB_NAME, 3)

        val testApplicationId = DatabaseHelper.insertWrenchApplication(originalDb, "TestApplication", "se.eelde.toggles.testapplication")

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 4, true, MIGRATION_3_4)

        val application = DatabaseHelper.getWrenchApplication(migratedDb, testApplicationId)

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

    companion object {
        private const val TEST_DB_NAME = "test_db"
    }
}
