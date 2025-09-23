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
class TogglesProviderMatcherConfigurationKeyTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val contentResolver = context.contentResolver

    @Inject
    lateinit var togglesDatabase: TogglesDatabase

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = contentResolver.getType(TogglesProviderContract.configurationUri("fakeKey"))
        assertEquals("vnd.android.cursor.item/vnd.se.eelde.toggles.configuration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testInsert() {
        contentResolver.insert(
            TogglesProviderContract.configurationUri("key"),
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testUpdate() {
        contentResolver.update(
            TogglesProviderContract.configurationUri("key"),
            null,
            null,
            null
        )
    }

    @Test
    fun testQuery() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "${this@TogglesProviderMatcherConfigurationKeyTest::class.simpleName}QueryKey"
        }

        try {
            contentResolver.insert(
                TogglesProviderContract.configurationUri(),
                togglesConfiguration.toContentValues(),
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to insert configuration, ${togglesConfiguration.key}", e)
        }

        val configurationUri = TogglesProviderContract.configurationUri(togglesConfiguration.key)

        val cursor = contentResolver.query(configurationUri, null, null, null, null)!!
        assertTrue(cursor.moveToFirst())
        TogglesConfiguration.fromCursor(cursor).also { cursorConfiguration ->
            assertEquals(togglesConfiguration.key, cursorConfiguration.key)
            assertEquals(togglesConfiguration.type, cursorConfiguration.type)
        }
    }

    @Test
    fun testDelete() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "${this@TogglesProviderMatcherConfigurationKeyTest::class.simpleName}DeleteKey"
        }

        contentResolver.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val rowsDeleted = contentResolver.delete(
            TogglesProviderContract.configurationUri(togglesConfiguration.key),
            null,
            null
        )
        assertEquals(1, rowsDeleted)
    }
}
