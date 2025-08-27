package se.eelde.toggles.provider.configuration

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.DatabaseModule
import se.eelde.toggles.database.TogglesDatabase
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(DatabaseModule::class)
class TogglesProviderMatcherConfigurationTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val contentResolver = context.contentResolver

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
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = contentResolver.getType(TogglesProviderContract.configurationUri())
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.configuration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testUpdate() {
        contentResolver.update(
            TogglesProviderContract.configurationUri(),
            null,
            null,
            null
        )
    }

    @Test
    fun testInsert() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val uri = contentResolver.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        assertEquals(
            "content://se.eelde.toggles.configprovider/configuration/1?API_VERSION=1",
            uri.toString()
        )
    }

    @Test
    fun testQuery() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        contentResolver.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val cursor = contentResolver.query(
            TogglesProviderContract.configurationUri(),
            null,
            null,
            null,
            null
        )!!
        assertTrue(cursor.moveToFirst())
        TogglesConfiguration.fromCursor(cursor).also { cursorConfiguration ->
            assertEquals(togglesConfiguration.key, cursorConfiguration.key)
            assertEquals(togglesConfiguration.type, cursorConfiguration.type)
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testDelete() {
        contentResolver.delete(
            TogglesProviderContract.configurationUri(),
            null,
            null,
        )
    }
}
