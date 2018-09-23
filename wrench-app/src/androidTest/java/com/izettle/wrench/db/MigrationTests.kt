package com.izettle.wrench.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.izettle.wrench.core.Bolt
import com.izettle.wrench.database.WrenchDatabase
import com.izettle.wrench.database.migrations.Migrations.MIGRATION_1_2
import com.izettle.wrench.database.migrations.Migrations.MIGRATION_2_3
import com.izettle.wrench.database.tables.ConfigurationTable
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTests {
    @Rule
    var testHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            WrenchDatabase::class.java.canonicalName!!,
            FrameworkSQLiteOpenHelperFactory())

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
        DatabaseHelper.insertWrenchConfiguration(originalDb, testApplicationId, "Integerkey", Bolt.TYPE.INTEGER)
        DatabaseHelper.insertWrenchConfiguration(originalDb, testApplicationId, "Stringkey", Bolt.TYPE.STRING)
        DatabaseHelper.insertWrenchConfiguration(originalDb, testApplicationId, "Booleankey", Bolt.TYPE.BOOLEAN)
        DatabaseHelper.insertWrenchConfiguration(originalDb, testApplicationId, "Enumkey", Bolt.TYPE.ENUM)

        originalDb.close()

        val migratedDb = testHelper.runMigrationsAndValidate(TEST_DB_NAME, 3, true, MIGRATION_2_3)

        var cursor = DatabaseHelper.getWrenchConfigurationByKey(migratedDb, "Integerkey")
        assertTrue(cursor.moveToFirst())
        assertEquals(Bolt.TYPE.INTEGER, cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE)))
        cursor.close()

        cursor = DatabaseHelper.getWrenchConfigurationByKey(migratedDb, "Stringkey")
        assertTrue(cursor.moveToFirst())
        assertEquals(Bolt.TYPE.STRING, cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE)))
        cursor.close()

        cursor = DatabaseHelper.getWrenchConfigurationByKey(migratedDb, "Booleankey")
        assertTrue(cursor.moveToFirst())
        assertEquals(Bolt.TYPE.BOOLEAN, cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE)))
        cursor.close()

        cursor = DatabaseHelper.getWrenchConfigurationByKey(migratedDb, "Enumkey")
        assertTrue(cursor.moveToFirst())
        assertEquals(Bolt.TYPE.ENUM, cursor.getString(cursor.getColumnIndex(ConfigurationTable.COL_TYPE)))
        cursor.close()
    }

    companion object {
        private val TEST_DB_NAME = "test_db"
    }

}
