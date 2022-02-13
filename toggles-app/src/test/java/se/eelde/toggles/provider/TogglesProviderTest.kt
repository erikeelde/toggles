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
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleValue
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.WrenchDatabase
import se.eelde.toggles.di.DatabaseModule
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
    fun testInsertToggle() {
        val insertToggleKey = "insertToggleKey"

        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(insertToggleKey)
        val insertToggleUri = togglesProvider.insert(uri, insertToggle.toContentValues())
        Assert.assertNotNull(insertToggleUri)

        var cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(insertToggleKey),
            null,
            null,
            null,
            null
        )
        Assert.assertNotNull(cursor)
        Assert.assertEquals(1, cursor.count)

        cursor.moveToFirst()
        var queryToggle = Toggle.fromCursor(cursor)

        Assert.assertEquals(insertToggle.key, queryToggle.key)
        Assert.assertEquals(insertToggle.value, queryToggle.value)
        Assert.assertEquals(insertToggle.type, queryToggle.type)

        val toggleUri = TogglesProviderContract.toggleUri(
            Integer.parseInt(insertToggleUri.lastPathSegment!!).toLong()
        )
        cursor = togglesProvider.query(toggleUri, null, null, null, null)
        Assert.assertNotNull(cursor)
        Assert.assertEquals(1, cursor.count)

        cursor.moveToFirst()
        queryToggle = Toggle.fromCursor(cursor)

        Assert.assertEquals(insertToggle.key, queryToggle.key)
        Assert.assertEquals(insertToggle.value, queryToggle.value)
        Assert.assertEquals(insertToggle.type, queryToggle.type)
    }

    @Test
    fun testUpdateToggle() {
        val updateToggleKey = "updateToggleKey"

        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(updateToggleKey)
        val insertToggleUri = togglesProvider.insert(uri, insertToggle.toContentValues())
        Assert.assertNotNull(insertToggleUri)

        var cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(updateToggleKey),
            null,
            null,
            null,
            null
        )
        Assert.assertNotNull(cursor)
        Assert.assertTrue(cursor.moveToFirst())

        val providerToggle = Toggle.fromCursor(cursor)
        Assert.assertEquals(insertToggle.key, providerToggle.key)
        Assert.assertEquals(insertToggle.value, providerToggle.value)
        Assert.assertEquals(insertToggle.type, providerToggle.type)

        val updateToggle = Toggle(
            providerToggle.id,
            providerToggle.type,
            providerToggle.key,
            providerToggle.value!! + providerToggle.value!!
        )

        val update = togglesProvider.update(
            TogglesProviderContract.toggleUri(updateToggle.id),
            updateToggle.toContentValues(),
            null,
            null
        )
        Assert.assertEquals(1, update)

        cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(updateToggleKey),
            null,
            null,
            null,
            null
        )
        Assert.assertNotNull(cursor)

        Assert.assertTrue(cursor.moveToFirst())
        val updatedToggle = Toggle.fromCursor(cursor)

        Assert.assertEquals(insertToggle.value!! + insertToggle.value!!, updatedToggle.value)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingInsertForToggleWithId() {
        togglesProvider.insert(
            TogglesProviderContract.toggleUri(0),
            getToggle("dummyToggle").toContentValues()
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingInsertForToggleWithKey() {
        togglesProvider.insert(
            TogglesProviderContract.toggleUri("fake"),
            getToggle("dummyToggle").toContentValues()
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForToggleWithKey() {
        togglesProvider.update(
            TogglesProviderContract.toggleUri("fake"),
            getToggle("dummyToggle").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForToggles() {
        togglesProvider.update(
            TogglesProviderContract.toggleUri(),
            getToggle("dummyToggle").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForToggleValues() {
        togglesProvider.update(
            TogglesProviderContract.toggleValueUri(),
            getToggleValue("dummyToggleValue").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForToggles() {
        togglesProvider.update(
            TogglesProviderContract.toggleUri(),
            getToggle("dummyToggle").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForToggleValues() {
        togglesProvider.update(
            TogglesProviderContract.toggleValueUri(),
            getToggleValue("dummyToggleValue").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggleWithId() {
        togglesProvider.delete(TogglesProviderContract.toggleUri(0), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggleWithKey() {
        togglesProvider.delete(TogglesProviderContract.toggleUri("fake"), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggles() {
        togglesProvider.delete(TogglesProviderContract.toggleUri(), null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggleValues() {
        togglesProvider.delete(TogglesProviderContract.toggleValueUri(), null, null)
    }

    private fun getToggleValue(value: String): ToggleValue {
        return ToggleValue(0, value)
    }

    private fun getToggle(key: String): Toggle {
        return Toggle(0L, "toggletype", key, "togglevalue")
    }
}
