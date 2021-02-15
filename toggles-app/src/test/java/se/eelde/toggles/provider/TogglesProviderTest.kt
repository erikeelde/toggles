package se.eelde.toggles.provider

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
import com.izettle.wrench.database.WrenchDatabase
import se.eelde.toggles.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.BuildConfig
import se.eelde.toggles.R
import se.eelde.toggles.core.TogglesProviderContract
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(DatabaseModule::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var togglesProvider: TogglesProvider

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Singleton
        @Provides
        fun provideWrenchDb(@ApplicationContext context: Context): WrenchDatabase {
            return Room.inMemoryDatabaseBuilder(context, WrenchDatabase::class.java)
                .allowMainThreadQueries().build()
        }
    }

    @Before
    fun setUp() {
        hiltRule.inject()

        val contentProviderController =
            Robolectric.buildContentProvider(TogglesProvider::class.java)
                .create(BuildConfig.CONFIG_AUTHORITY)
        togglesProvider = contentProviderController.get()

        val context = ApplicationProvider.getApplicationContext<Application>()
        val appIcon = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
        shadowOf(context.packageManager).setApplicationIcon(
            context.applicationInfo.packageName,
            appIcon
        )

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun testInsertBolt() {
        val insertBoltKey = "insertBoltKey"

        val uri = TogglesProviderContract.boltUri()
        val insertBolt = getBolt(insertBoltKey)
        val insertBoltUri = togglesProvider.insert(uri, insertBolt.toContentValues())
        Assert.assertNotNull(insertBoltUri)

        var cursor = togglesProvider.query(
            TogglesProviderContract.boltUri(insertBoltKey),
            null,
            null,
            null,
            null
        )
        Assert.assertNotNull(cursor)
        Assert.assertEquals(1, cursor.count)

        cursor.moveToFirst()
        var queryBolt = Bolt.fromCursor(cursor)

        Assert.assertEquals(insertBolt.key, queryBolt.key)
        Assert.assertEquals(insertBolt.value, queryBolt.value)
        Assert.assertEquals(insertBolt.type, queryBolt.type)

        val boltUri = TogglesProviderContract.boltUri(
            Integer.parseInt(insertBoltUri.lastPathSegment!!).toLong()
        )
        cursor = togglesProvider.query(boltUri, null, null, null, null)
        Assert.assertNotNull(cursor)
        Assert.assertEquals(1, cursor.count)

        cursor.moveToFirst()
        queryBolt = Bolt.fromCursor(cursor)

        Assert.assertEquals(insertBolt.key, queryBolt.key)
        Assert.assertEquals(insertBolt.value, queryBolt.value)
        Assert.assertEquals(insertBolt.type, queryBolt.type)
    }

    @Test
    fun testUpdateBolt() {
        val updateBoltKey = "updateBoltKey"

        val uri = TogglesProviderContract.boltUri()
        val insertBolt = getBolt(updateBoltKey)
        val insertBoltUri = togglesProvider.insert(uri, insertBolt.toContentValues())
        Assert.assertNotNull(insertBoltUri)

        var cursor = togglesProvider.query(
            TogglesProviderContract.boltUri(updateBoltKey),
            null,
            null,
            null,
            null
        )
        Assert.assertNotNull(cursor)
        Assert.assertTrue(cursor.moveToFirst())

        val providerBolt = Bolt.fromCursor(cursor)
        Assert.assertEquals(insertBolt.key, providerBolt.key)
        Assert.assertEquals(insertBolt.value, providerBolt.value)
        Assert.assertEquals(insertBolt.type, providerBolt.type)

        val updateBolt = Bolt(
            providerBolt.id,
            providerBolt.type,
            providerBolt.key,
            providerBolt.value!! + providerBolt.value!!
        )

        val update = togglesProvider.update(
            TogglesProviderContract.boltUri(updateBolt.id),
            updateBolt.toContentValues(),
            null,
            null
        )
        Assert.assertEquals(1, update)

        cursor = togglesProvider.query(
            TogglesProviderContract.boltUri(updateBoltKey),
            null,
            null,
            null,
            null
        )
        Assert.assertNotNull(cursor)

        Assert.assertTrue(cursor.moveToFirst())
        val updatedBolt = Bolt.fromCursor(cursor)

        Assert.assertEquals(insertBolt.value!! + insertBolt.value!!, updatedBolt.value)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingInsertForBoltWithId() {
        togglesProvider.insert(
            TogglesProviderContract.boltUri(0),
            getBolt("dummyBolt").toContentValues()
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingInsertForBoltWithKey() {
        togglesProvider.insert(
            TogglesProviderContract.boltUri("fake"),
            getBolt("dummyBolt").toContentValues()
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForBoltWithKey() {
        togglesProvider.update(
            TogglesProviderContract.boltUri("fake"),
            getBolt("dummyBolt").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForBolts() {
        togglesProvider.update(
            TogglesProviderContract.boltUri(),
            getBolt("dummyBolt").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForNuts() {
        togglesProvider.update(
            TogglesProviderContract.nutUri(),
            getNut("dummyNut").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForBolts() {
        togglesProvider.update(
            TogglesProviderContract.boltUri(),
            getBolt("dummyBolt").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForNuts() {
        togglesProvider.update(
            TogglesProviderContract.nutUri(),
            getNut("dummyNut").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForBoltWithId() {
        togglesProvider.delete(TogglesProviderContract.boltUri(0), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForBoltWithKey() {
        togglesProvider.delete(TogglesProviderContract.boltUri("fake"), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForBolts() {
        togglesProvider.delete(TogglesProviderContract.boltUri(), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForNuts() {
        togglesProvider.delete(TogglesProviderContract.nutUri(), null, null)
    }

    private fun getNut(value: String): Nut {
        return Nut(0, value)
    }

    private fun getBolt(key: String): Bolt {
        return Bolt(0L, "bolttype", key, "boltvalue")
    }
}
