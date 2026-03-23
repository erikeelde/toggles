package se.eelde.toggles.provider

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.provider.di.ToggleTestApplication_Application
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = ToggleTestApplication_Application::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderMatcherCurrentConfigurationIdTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var togglesProvider: TogglesProvider

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
        val type = togglesProvider.getType(TogglesProviderContract.toggleUri(0))
        assertEquals("vnd.android.cursor.item/vnd.se.eelde.toggles.currentConfiguration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingInsertForToggleWithId() {
        togglesProvider.insert(
            TogglesProviderContract.toggleUri(0),
            getToggle("dummyToggle").toContentValues()
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggleWithId() {
        togglesProvider.delete(TogglesProviderContract.toggleUri(0), null, null)
    }

    @Test
    fun testQueryFallsBackToDefaultScope() {
        val toggleKey = "scopeFallbackToggle"
        val insertToggle = getToggle(toggleKey)

        // Insert creates value in default scope only
        val insertUri = togglesProvider.insert(
            TogglesProviderContract.toggleUri(),
            insertToggle.toContentValues()
        )
        val configId = requireNotNull(insertUri.lastPathSegment).toLong()

        // Query by ID should fall back to default scope and find the value
        togglesProvider.query(
            TogglesProviderContract.toggleUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val toggle = Toggle.fromCursor(cursor)
            assertEquals(toggleKey, toggle.key)
            assertEquals("togglevalue", toggle.value)
        }
    }

    @Test
    fun testQueryReturnsSelectedScopeOverDefault() {
        val toggleKey = "scopePriorityToggle"
        val insertToggle = getToggle(toggleKey)

        // Insert creates value in default scope
        val insertUri = togglesProvider.insert(
            TogglesProviderContract.toggleUri(),
            insertToggle.toContentValues()
        )
        val configId = requireNotNull(insertUri.lastPathSegment).toLong()

        // Update creates value in selected (development) scope via insert fallback
        val updateToggle = Toggle {
            id = configId
            type = insertToggle.type
            key = insertToggle.key
            value = "updatedvalue"
        }
        togglesProvider.update(
            TogglesProviderContract.toggleUri(configId),
            updateToggle.toContentValues(),
            null,
            null
        )

        // Query should return the selected scope value, not the default
        togglesProvider.query(
            TogglesProviderContract.toggleUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val toggle = Toggle.fromCursor(cursor)
            assertEquals("updatedvalue", toggle.value)
        }
    }

    @Test
    fun testUpdateCreatesValueInSelectedScopeWhenMissing() {
        val toggleKey = "updateFallbackToggle"
        val insertToggle = getToggle(toggleKey)

        // Insert creates value in default scope only
        val insertUri = togglesProvider.insert(
            TogglesProviderContract.toggleUri(),
            insertToggle.toContentValues()
        )
        val configId = requireNotNull(insertUri.lastPathSegment).toLong()

        // Update targets selected (development) scope - no value exists there yet
        // This should trigger the insert fallback path
        val updateToggle = Toggle {
            id = configId
            type = insertToggle.type
            key = insertToggle.key
            value = "newScopeValue"
        }
        val updatedRows = togglesProvider.update(
            TogglesProviderContract.toggleUri(configId),
            updateToggle.toContentValues(),
            null,
            null
        )
        assertEquals(1, updatedRows)

        // Verify the value was created in the selected scope
        togglesProvider.query(
            TogglesProviderContract.toggleUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val toggle = Toggle.fromCursor(cursor)
            assertEquals("newScopeValue", toggle.value)
        }
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
