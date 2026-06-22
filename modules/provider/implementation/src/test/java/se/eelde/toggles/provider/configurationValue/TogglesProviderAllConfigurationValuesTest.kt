package se.eelde.toggles.provider.configurationValue

import android.app.Application
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
import se.eelde.toggles.core.ToggleScope
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.provider.TogglesProvider
import se.eelde.toggles.provider.di.ToggleTestApplication_Application
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = ToggleTestApplication_Application::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderAllConfigurationValuesTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var togglesDatabase: TogglesDatabase

    private lateinit var togglesProvider: TogglesProvider

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

    private fun insertConfigurationWithDefaultValue(key: String, value: String): Long {
        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            TogglesConfiguration {
                type = Toggle.TYPE.STRING
                this.key = key
            }.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            TogglesConfigurationValue {
                configurationId = configId
                this.value = value
            }.toContentValues(),
        )
        return configId
    }

    @Test
    fun testGetTypeAllConfigurationValues() {
        val type = togglesProvider.getType(TogglesProviderContract.configurationValuesUri())
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.configurationValue", type)
    }

    @Test
    fun testQueryReturnsValuesFromAllConfigurations() {
        insertConfigurationWithDefaultValue("keyOne", "valueOne")
        insertConfigurationWithDefaultValue("keyTwo", "valueTwo")

        togglesProvider.query(
            TogglesProviderContract.configurationValuesUri(),
            null,
            null,
            null,
            null
        ).use { cursor ->
            val values = mutableListOf<String>()
            while (cursor.moveToNext()) {
                values.add(requireNotNull(TogglesConfigurationValue.fromCursor(cursor).value))
            }
            assertEquals(2, values.size)
            assertTrue(values.contains("valueOne"))
            assertTrue(values.contains("valueTwo"))
        }
    }

    @Test
    fun testQueryReturnsValuesFromAllScopes() {
        val configId = insertConfigurationWithDefaultValue("multiScopeKey", "defaultValue")

        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            TogglesConfigurationValue {
                configurationId = configId
                value = "developmentValue"
                scope = getDevelopmentScopeId()
            }.toContentValues(),
        )

        togglesProvider.query(
            TogglesProviderContract.configurationValuesUri(),
            null,
            null,
            null,
            null
        ).use { cursor ->
            val values = mutableListOf<String>()
            while (cursor.moveToNext()) {
                values.add(requireNotNull(TogglesConfigurationValue.fromCursor(cursor).value))
            }
            assertEquals(2, values.size)
            assertTrue(values.contains("defaultValue"))
            assertTrue(values.contains("developmentValue"))
        }
    }

    @Test
    fun testQueryWithNoValuesReturnsEmptyCursor() {
        togglesProvider.query(
            TogglesProviderContract.configurationValuesUri(),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertFalse(cursor.moveToFirst())
            assertEquals(0, cursor.count)
        }
    }

    private fun getDevelopmentScopeId(): Long {
        togglesProvider.query(
            TogglesProviderContract.scopeUri(),
            null,
            null,
            null,
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val scope = ToggleScope.fromCursor(cursor)
                if (scope.name == "Development scope") {
                    return scope.id
                }
            }
        }
        error("Development scope not found")
    }
}
