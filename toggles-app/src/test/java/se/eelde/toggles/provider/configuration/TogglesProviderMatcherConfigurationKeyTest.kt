package se.eelde.toggles.provider.configuration

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
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.di.DatabaseModule
import se.eelde.toggles.provider.TogglesProvider
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(DatabaseModule::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderMatcherConfigurationKeyTest {
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

    val togglesConfiguration = TogglesConfiguration {
        type = Toggle.TYPE.BOOLEAN
        key = "myConfigurationkey"
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
        val type = togglesProvider.getType(TogglesProviderContract.configurationUri("fakeKey"))
        assertEquals("vnd.android.cursor.item/vnd.se.eelde.toggles.configuration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testInsert() {
        togglesProvider.insert(
            TogglesProviderContract.configurationUri("key"),
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testUpdate() {
        togglesProvider.update(
            TogglesProviderContract.configurationUri("key"),
            null,
            null,
            null
        )
    }

    @Test
    fun testQuery() {
        togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val configurationUri = TogglesProviderContract.configurationUri(togglesConfiguration.key)

        val cursor = togglesProvider.query(configurationUri, null, null, null, null)
        assertTrue(cursor.moveToFirst())
        TogglesConfiguration.fromCursor(cursor).also { cursorConfiguration ->
            assertEquals(togglesConfiguration.key, cursorConfiguration.key)
            assertEquals(togglesConfiguration.type, cursorConfiguration.type)
        }
    }

    @Test
    fun testDelete() {
        togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val rowsDeleted = togglesProvider.delete(
            TogglesProviderContract.configurationUri(togglesConfiguration.key),
            null,
            null
        )
        assertEquals(1, rowsDeleted)
    }
}
