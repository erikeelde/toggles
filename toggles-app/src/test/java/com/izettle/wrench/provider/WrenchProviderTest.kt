package com.izettle.wrench.provider

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.izettle.wrench.core.Bolt
import com.izettle.wrench.core.Nut
import com.izettle.wrench.core.WrenchProviderContract
import com.izettle.wrench.database.WrenchDatabase
import com.izettle.wrench.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import se.eelde.toggles.R
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(DatabaseModule::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class WrenchProviderTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var wrenchProvider: WrenchProvider

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Singleton
        @Provides
        fun provideWrenchDb(@ApplicationContext context: Context): WrenchDatabase {
            return Room.inMemoryDatabaseBuilder(context, WrenchDatabase::class.java).allowMainThreadQueries().build()
        }
    }

    @Before
    fun setUp() {
        hiltRule.inject()

        val contentProviderController = Robolectric.buildContentProvider(WrenchProvider::class.java).create(WrenchProviderContract.WRENCH_AUTHORITY)
        wrenchProvider = contentProviderController.get()

        val context = ApplicationProvider.getApplicationContext<Application>()
        val appIcon = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
        Shadows.shadowOf(context.packageManager)
            .setApplicationIcon(context.applicationInfo.packageName, appIcon)

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun testInsertBoltWithRCType() {
        fun checkTypeConversion(bolt: Bolt, expectedType: String) {
            wrenchProvider.insert(WrenchProviderContract.boltUri(), bolt.toContentValues())

            val cursor = wrenchProvider.query(WrenchProviderContract.boltUri(bolt.key), null, null, null, null)!!

            cursor.moveToFirst()
            val queryBolt = Bolt.fromCursor(cursor)

            Assert.assertEquals(expectedType, queryBolt.type)
        }

        checkTypeConversion(getBolt(key = "stringBoltKey", boltType = "java.lang.String"), "string")
        checkTypeConversion(getBolt(key = "intBoltKey", boltType = "java.lang.Integer"), "integer")
        checkTypeConversion(getBolt(key = "enumBoltKey", boltType = "java.lang.Enum"), "enum")
        checkTypeConversion(getBolt(key = "booleanBoltKey", boltType = "java.lang.Boolean"), "boolean")
    }

    @Test
    fun testInsertBolt() {
        val insertBoltKey = "insertBoltKey"

        val uri = WrenchProviderContract.boltUri()
        val insertBolt = getBolt(insertBoltKey)
        val insertBoltUri = wrenchProvider.insert(uri, insertBolt.toContentValues())
        Assert.assertNotNull(insertBoltUri)

        var cursor = wrenchProvider.query(WrenchProviderContract.boltUri(insertBoltKey), null, null, null, null)
        Assert.assertNotNull(cursor)
        Assert.assertEquals(1, cursor!!.count)

        cursor.moveToFirst()
        var queryBolt = Bolt.fromCursor(cursor)

        Assert.assertEquals(insertBolt.key, queryBolt.key)
        Assert.assertEquals(insertBolt.value, queryBolt.value)
        Assert.assertEquals(insertBolt.type, queryBolt.type)

        cursor = wrenchProvider.query(WrenchProviderContract.boltUri(Integer.parseInt(insertBoltUri!!.lastPathSegment!!).toLong()), null, null, null, null)
        Assert.assertNotNull(cursor)
        Assert.assertEquals(1, cursor!!.count)

        cursor.moveToFirst()
        queryBolt = Bolt.fromCursor(cursor)

        Assert.assertEquals(insertBolt.key, queryBolt.key)
        Assert.assertEquals(insertBolt.value, queryBolt.value)
        Assert.assertEquals(insertBolt.type, queryBolt.type)
    }

    @Test
    fun testUpdateBolt() {
        val updateBoltKey = "updateBoltKey"

        val uri = WrenchProviderContract.boltUri()
        val insertBolt = getBolt(updateBoltKey)
        val insertBoltUri = wrenchProvider.insert(uri, insertBolt.toContentValues())
        Assert.assertNotNull(insertBoltUri)

        var cursor = wrenchProvider.query(WrenchProviderContract.boltUri(updateBoltKey), null, null, null, null)
        Assert.assertNotNull(cursor)
        Assert.assertTrue(cursor!!.moveToFirst())

        val providerBolt = Bolt.fromCursor(cursor)
        Assert.assertEquals(insertBolt.key, providerBolt.key)
        Assert.assertEquals(insertBolt.value, providerBolt.value)
        Assert.assertEquals(insertBolt.type, providerBolt.type)

        val updateBolt = Bolt(providerBolt.id, providerBolt.type, providerBolt.key, providerBolt.value!! + providerBolt.value!!)

        val update = wrenchProvider.update(WrenchProviderContract.boltUri(updateBolt.id), updateBolt.toContentValues(), null, null)
        Assert.assertEquals(1, update)

        cursor = wrenchProvider.query(WrenchProviderContract.boltUri(updateBoltKey), null, null, null, null)
        Assert.assertNotNull(cursor)

        Assert.assertTrue(cursor!!.moveToFirst())
        val updatedBolt = Bolt.fromCursor(cursor)

        Assert.assertEquals(insertBolt.value!! + insertBolt.value!!, updatedBolt.value)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingInsertForBoltWithId() {
        wrenchProvider.insert(WrenchProviderContract.boltUri(0), getBolt("dummyBolt").toContentValues())
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingInsertForBoltWithKey() {
        wrenchProvider.insert(WrenchProviderContract.boltUri("fake"), getBolt("dummyBolt").toContentValues())
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForBoltWithKey() {
        wrenchProvider.update(WrenchProviderContract.boltUri("fake"), getBolt("dummyBolt").toContentValues(), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForBolts() {
        wrenchProvider.update(WrenchProviderContract.boltUri(), getBolt("dummyBolt").toContentValues(), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForNuts() {
        wrenchProvider.update(WrenchProviderContract.nutUri(), getNut("dummyNut").toContentValues(), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForBolts() {
        wrenchProvider.update(WrenchProviderContract.boltUri(), getBolt("dummyBolt").toContentValues(), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForNuts() {
        wrenchProvider.update(WrenchProviderContract.nutUri(), getNut("dummyNut").toContentValues(), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForBoltWithId() {
        wrenchProvider.delete(WrenchProviderContract.boltUri(0), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForBoltWithKey() {
        wrenchProvider.delete(WrenchProviderContract.boltUri("fake"), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForBolts() {
        wrenchProvider.delete(WrenchProviderContract.boltUri(), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForNuts() {
        wrenchProvider.delete(WrenchProviderContract.nutUri(), null, null)
    }

    private fun getNut(value: String): Nut {
        return Nut(0, value)
    }

    private fun getBolt(key: String, boltType: String = "bolttype"): Bolt {
        return Bolt(0L, boltType, key, "boltvalue")
    }
}
