package se.eelde.toggles.provider

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.di.DatabaseModule
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(DatabaseModule::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderMatcherCurrentConfigurationTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var togglesProvider: TogglesProvider

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Singleton
        @Provides
        fun provideTogglesDb(@ApplicationContext context: Context): TogglesDatabase {
            return Room.inMemoryDatabaseBuilder(context, TogglesDatabase::class.java)
                .allowMainThreadQueries().build()
        }
    }

    @Inject
    lateinit var togglesDatabase: TogglesDatabase

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
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = togglesProvider.getType(TogglesProviderContract.toggleUri())
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.currentConfiguration", type)
    }

    @Test
    fun testInsertToggle() {
        val insertToggleKey = "insertToggleKey"

        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(insertToggleKey)
        val insertToggleUri = togglesProvider.insert(uri, insertToggle.toContentValues())
        assertNotNull(insertToggleUri)

        var cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(insertToggleKey),
            null,
            null,
            null,
            null
        )
        assertNotNull(cursor)
        assertEquals(1, cursor.count)

        cursor.moveToFirst()
        var queryToggle = Toggle.fromCursor(cursor)

        assertEquals(insertToggle.key, queryToggle.key)
        assertEquals(insertToggle.value, queryToggle.value)
        assertEquals(insertToggle.type, queryToggle.type)

        val toggleUri = TogglesProviderContract.toggleUri(
            Integer.parseInt(insertToggleUri.lastPathSegment!!).toLong()
        )
        cursor = togglesProvider.query(toggleUri, null, null, null, null)
        assertNotNull(cursor)
        assertEquals(1, cursor.count)

        cursor.moveToFirst()
        queryToggle = Toggle.fromCursor(cursor)

        assertEquals(insertToggle.key, queryToggle.key)
        assertEquals(insertToggle.value, queryToggle.value)
        assertEquals(insertToggle.type, queryToggle.type)
    }

    @Test
    fun testUpdateToggle() {
        val updateToggleKey = "updateToggleKey"

        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(updateToggleKey)
        val insertToggleUri = togglesProvider.insert(uri, insertToggle.toContentValues())
        assertNotNull(insertToggleUri)

        var cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(updateToggleKey),
            null,
            null,
            null,
            null
        )
        assertNotNull(cursor)
        assertTrue(cursor.moveToFirst())

        val providerToggle = Toggle.fromCursor(cursor)
        assertEquals(insertToggle.key, providerToggle.key)
        assertEquals(insertToggle.value, providerToggle.value)
        assertEquals(insertToggle.type, providerToggle.type)

        val updateToggle = Toggle {
            id = providerToggle.id
            type = providerToggle.type
            key = providerToggle.key
            value = providerToggle.value!! + providerToggle.value!!
        }

        val update = togglesProvider.update(
            TogglesProviderContract.toggleUri(updateToggle.id),
            updateToggle.toContentValues(),
            null,
            null
        )
        assertEquals(1, update)

        cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(updateToggleKey),
            null,
            null,
            null,
            null
        )
        assertNotNull(cursor)

        assertTrue(cursor.moveToFirst())
        val updatedToggle = Toggle.fromCursor(cursor)

        assertEquals(insertToggle.value!! + insertToggle.value!!, updatedToggle.value)
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
    fun testMissingQueryForToggles() {
        togglesProvider.update(
            TogglesProviderContract.toggleUri(),
            getToggle("dummyToggle").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggles() {
        togglesProvider.delete(TogglesProviderContract.toggleUri(), null, null)
    }

    private fun getToggle(key: String): Toggle {
        return Toggle {
            id = 0L
            type = "toggletype"
            this.key = key
            value = "togglevalue"
        }
    }
}
