package se.eelde.toggles.provider

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
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
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.TogglesScope
import java.util.Date
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class ScopedTogglesProviderTest {
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
    fun testQueryWithScopeParameter() {
        val toggleKey = "scopedToggleKey"

        // Insert a toggle (will go to default scope)
        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(toggleKey, "default_value")
        val insertToggleUri = togglesProvider.insert(uri, insertToggle.toContentValues())
        assertNotNull(insertToggleUri)

        // Create a custom scope
        val customScope = TogglesScope(
            id = 0,
            applicationId = 1,
            name = "custom_scope",
            timeStamp = Date()
        )
        customScope.id = togglesDatabase.togglesScopeDao().insert(customScope)

        // Insert a different value for the custom scope
        val config = togglesDatabase.togglesConfigurationDao()
            .getTogglesConfiguration(1, toggleKey)
        assertNotNull(config)

        togglesDatabase.togglesConfigurationValueDao().insert(
            se.eelde.toggles.database.TogglesConfigurationValue(
                id = 0,
                configurationId = config!!.id,
                value = "custom_value",
                scope = customScope.id
            )
        )

        // Query without scope parameter - should get default scope value
        var cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(toggleKey),
            null,
            null,
            null,
            null
        )
        assertNotNull(cursor)
        assertTrue(cursor.moveToFirst())
        var toggle = Toggle.fromCursor(cursor)
        assertEquals("default_value", toggle.value)
        cursor.close()

        // Query with custom scope parameter - should get custom scope value
        cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(toggleKey, "custom_scope"),
            null,
            null,
            null,
            null
        )
        assertNotNull(cursor)
        assertTrue(cursor.moveToFirst())
        toggle = Toggle.fromCursor(cursor)
        assertEquals("custom_value", toggle.value)
        cursor.close()
    }

    @Test
    fun testQueryWithNonExistentScope() {
        val toggleKey = "nonExistentScopeKey"

        // Insert a toggle in default scope
        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(toggleKey, "default_value")
        val insertToggleUri = togglesProvider.insert(uri, insertToggle.toContentValues())
        assertNotNull(insertToggleUri)

        // Query with non-existent scope - should fall back to default scope
        val cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(toggleKey, "nonexistent_scope"),
            null,
            null,
            null,
            null
        )
        assertNotNull(cursor)
        assertTrue(cursor.moveToFirst())
        val toggle = Toggle.fromCursor(cursor)
        assertEquals("default_value", toggle.value)
        cursor.close()
    }

    @Test
    fun testMultipleScopesWithDifferentValues() {
        val toggleKey = "multiScopeKey"

        // Insert base toggle
        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(toggleKey, "value_default")
        togglesProvider.insert(uri, insertToggle.toContentValues())

        // Create scope1
        val scope1 = TogglesScope(
            id = 0,
            applicationId = 1,
            name = "scope1",
            timeStamp = Date()
        )
        scope1.id = togglesDatabase.togglesScopeDao().insert(scope1)

        // Create scope2
        val scope2 = TogglesScope(
            id = 0,
            applicationId = 1,
            name = "scope2",
            timeStamp = Date()
        )
        scope2.id = togglesDatabase.togglesScopeDao().insert(scope2)

        // Get configuration
        val config = togglesDatabase.togglesConfigurationDao()
            .getTogglesConfiguration(1, toggleKey)
        assertNotNull(config)

        // Insert different values for each scope
        togglesDatabase.togglesConfigurationValueDao().insert(
            se.eelde.toggles.database.TogglesConfigurationValue(
                id = 0,
                configurationId = config!!.id,
                value = "value_scope1",
                scope = scope1.id
            )
        )

        togglesDatabase.togglesConfigurationValueDao().insert(
            se.eelde.toggles.database.TogglesConfigurationValue(
                id = 0,
                configurationId = config.id,
                value = "value_scope2",
                scope = scope2.id
            )
        )

        // Verify each scope returns its own value
        var cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(toggleKey, "scope1"),
            null,
            null,
            null,
            null
        )
        assertTrue(cursor.moveToFirst())
        assertEquals("value_scope1", Toggle.fromCursor(cursor).value)
        cursor.close()

        cursor = togglesProvider.query(
            TogglesProviderContract.toggleUri(toggleKey, "scope2"),
            null,
            null,
            null,
            null
        )
        assertTrue(cursor.moveToFirst())
        assertEquals("value_scope2", Toggle.fromCursor(cursor).value)
        cursor.close()
    }

    private fun getToggle(key: String, value: String): Toggle {
        return Toggle {
            id = 0L
            type = Toggle.TYPE.STRING
            this.key = key
            this.value = value
        }
    }
}
