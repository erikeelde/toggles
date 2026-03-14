package se.eelde.toggles.provider.configurationValue

import android.app.Application
import android.content.ContentValues
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
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
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderMatcherConfigurationValueTest {
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
    fun testGetTypeConfigurationValueById() {
        val type = togglesProvider.getType(TogglesProviderContract.configurationValueUri(1L))
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.configurationValue", type)
    }

    @Test
    fun testGetTypeConfigurationValueByKey() {
        val type = togglesProvider.getType(TogglesProviderContract.configurationValueUri("key"))
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.configurationValue", type)
    }

    @Test
    fun testQueryById() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
            assertEquals(configId, fromCursor.configurationId)
            assertEquals("true", fromCursor.value)
        }
    }

    @Test
    fun testQueryByKey() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri("myConfigurationkey"),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
            assertEquals(configId, fromCursor.configurationId)
            assertEquals("true", fromCursor.value)
        }
    }

    @Test
    fun testInsert() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
        }
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        assertTrue(uri.toString().contains("/values/"))
    }

    @Test
    fun testUpdate() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        val updatedValue = configValue.copy(value = "false")
        val rowsUpdated = togglesProvider.update(
            TogglesProviderContract.configurationValueUri(configId),
            updatedValue.toContentValues(),
            null,
            null
        )

        assertEquals(1, rowsUpdated)

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
            assertEquals("false", fromCursor.value)
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testDeleteById() {
        togglesProvider.delete(
            TogglesProviderContract.configurationValueUri(1L),
            null,
            null,
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testDeleteByKey() {
        togglesProvider.delete(
            TogglesProviderContract.configurationValueUri("key"),
            null,
            null,
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testInsertByKey() {
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri("key"),
            ContentValues()
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testUpdateByKey() {
        togglesProvider.update(
            TogglesProviderContract.configurationValueUri("key"),
            ContentValues(),
            null,
            null
        )
    }

    @Test
    fun testQueryByIdForNonExistentConfigurationReturnsEmptyCursor() {
        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(999L),
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
    fun testQueryByKeyForNonExistentConfigurationReturnsEmptyCursor() {
        togglesProvider.query(
            TogglesProviderContract.configurationValueUri("nonExistentKey"),
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
    fun testUpdateNonExistentConfigurationValueReturnsZero() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "updateNonExistentValueKey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val nonExistentValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = 999L
        }
        val rowsUpdated = togglesProvider.update(
            TogglesProviderContract.configurationValueUri(configId),
            nonExistentValue.toContentValues(),
            null,
            null
        )

        assertEquals(0, rowsUpdated)
    }

    @Test
    fun testInsertValueWithSpecificScope() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "scopeSpecificKey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val defaultScopeId = getDefaultScopeId()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "defaultValue"
            scope = defaultScopeId
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
            assertEquals(configId, fromCursor.configurationId)
            assertEquals("defaultValue", fromCursor.value)
            assertEquals(defaultScopeId, fromCursor.scope)
        }
    }

    @Test
    fun testInsertValuesInMultipleScopes() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.STRING
            key = "multiScopeKey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val defaultScopeId = getDefaultScopeId()
        val developmentScopeId = getDevelopmentScopeId()

        val defaultValue = TogglesConfigurationValue {
            configurationId = configId
            value = "defaultValue"
            scope = defaultScopeId
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            defaultValue.toContentValues(),
        )

        val developmentValue = TogglesConfigurationValue {
            configurationId = configId
            value = "developmentValue"
            scope = developmentScopeId
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            developmentValue.toContentValues(),
        )

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertEquals(2, cursor.count)
            val values = mutableListOf<String>()
            while (cursor.moveToNext()) {
                val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
                values.add(requireNotNull(fromCursor.value))
            }
            assertTrue(values.contains("defaultValue"))
            assertTrue(values.contains("developmentValue"))
        }
    }

    @Test
    fun testUpdateValueInSpecificScope() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "scopeUpdateKey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val defaultScopeId = getDefaultScopeId()
        val developmentScopeId = getDevelopmentScopeId()

        val defaultValue = TogglesConfigurationValue {
            configurationId = configId
            value = "defaultValue"
            scope = defaultScopeId
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            defaultValue.toContentValues(),
        )

        val devValue = TogglesConfigurationValue {
            configurationId = configId
            value = "devValue"
            scope = developmentScopeId
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            devValue.toContentValues(),
        )

        val updatedDevValue = devValue.copy(value = "updatedDevValue")
        val rowsUpdated = togglesProvider.update(
            TogglesProviderContract.configurationValueUri(configId),
            updatedDevValue.toContentValues(),
            null,
            null
        )
        assertEquals(1, rowsUpdated)

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            val values = mutableMapOf<Long, String>()
            while (cursor.moveToNext()) {
                val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
                values[fromCursor.scope] = requireNotNull(fromCursor.value)
            }
            assertEquals("defaultValue", values[defaultScopeId])
            assertEquals("updatedDevValue", values[developmentScopeId])
        }
    }

    private fun getDefaultScopeId(): Long {
        togglesProvider.query(
            TogglesProviderContract.scopeUri(),
            null,
            null,
            null,
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val scope = ToggleScope.fromCursor(cursor)
                if (scope.name == "toggles_default") {
                    return scope.id
                }
            }
        }
        error("Default scope not found")
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
