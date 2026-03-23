package se.eelde.toggles.provider.configuration

import android.app.Application
import android.content.ContentValues
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.provider.TogglesProvider
import se.eelde.toggles.provider.di.ToggleTestApplication_Application
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = ToggleTestApplication_Application::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderMatcherConfigurationIdTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var togglesProvider: TogglesProvider

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
                .create("se.eelde.toggles.configprovider")
        togglesProvider = contentProviderController.get()

        val context = ApplicationProvider.getApplicationContext<Application>()

        shadowOf(context.packageManager).setApplicationIcon(
            context.applicationInfo.packageName,
            ColorDrawable(Color.RED)
        )
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = togglesProvider.getType(TogglesProviderContract.configurationUri(0))
        assertEquals("vnd.android.cursor.item/vnd.se.eelde.toggles.configuration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testInsert() {
        togglesProvider.insert(
            TogglesProviderContract.configurationUri(0),
            ContentValues()
        )
    }

    @Test
    fun testUpdate() {
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val updatedConfiguration =
            togglesConfiguration.copy(key = "newKey", type = Toggle.TYPE.STRING)

        val rowsUpdated = togglesProvider.update(
            TogglesProviderContract.configurationUri(requireNotNull(uri.lastPathSegment).toLong()),
            updatedConfiguration.toContentValues(),
            null,
            null
        )

        togglesProvider.query(uri, null, null, null, null).use { query ->
            assertTrue(query.moveToFirst())
            val fromCursor = TogglesConfiguration.fromCursor(query)

            assertEquals(1, rowsUpdated)
            assertEquals(1, fromCursor.id)
            assertEquals("newKey", fromCursor.key)
            assertEquals(Toggle.TYPE.STRING, fromCursor.type)
        }
    }

    @Test
    fun testQuery() {
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val configurationUri = TogglesProviderContract.configurationUri(requireNotNull(uri.lastPathSegment).toLong())

        togglesProvider.query(configurationUri, null, null, null, null).use { cursor ->
            assertTrue(cursor.moveToFirst())
            TogglesConfiguration.fromCursor(cursor).also { cursorConfiguration ->
                assertEquals(togglesConfiguration.key, cursorConfiguration.key)
                assertEquals(togglesConfiguration.type, cursorConfiguration.type)
            }
        }
    }

    @Test
    fun testDelete() {
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val rowsDeleted = togglesProvider.delete(
            TogglesProviderContract.configurationUri(requireNotNull(uri.lastPathSegment).toLong()),
            null,
            null
        )
        assertEquals(1, rowsDeleted)
    }

    @Test
    fun testQueryNonExistentConfigurationByIdReturnsEmptyCursor() {
        togglesProvider.query(
            TogglesProviderContract.configurationUri(999L),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertFalse(cursor.moveToFirst())
            assertEquals(0, cursor.count)
        }
    }

    @Test
    fun testUpdateNonExistentConfigurationByIdReturnsZero() {
        val updatedConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "nonExistentKey"
        }

        val rowsUpdated = togglesProvider.update(
            TogglesProviderContract.configurationUri(999L),
            updatedConfiguration.toContentValues(),
            null,
            null
        )
        assertEquals(0, rowsUpdated)
    }

    @Test
    fun testDeleteNonExistentConfigurationByIdReturnsZero() {
        val rowsDeleted = togglesProvider.delete(
            TogglesProviderContract.configurationUri(999L),
            null,
            null
        )
        assertEquals(0, rowsDeleted)
    }
}
