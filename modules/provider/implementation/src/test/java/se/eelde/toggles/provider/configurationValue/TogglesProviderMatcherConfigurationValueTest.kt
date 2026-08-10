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
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = getDefaultScopeId()
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
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = getDefaultScopeId()
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
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()

        val defaultScopeId = getDefaultScopeId()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = defaultScopeId
        }
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        // A fresh insert must return a non-null URI shaped like ".../values/{id}". insert() only
        // returns null when nothing was written (see
        // testInsertWithNonexistentScopeReturnsNullAndWritesNoRow), so requireNotNull here is
        // itself an assertion that this insert actually succeeded.
        assertTrue(requireNotNull(uri).toString().contains("/values/"))

        val insertedId = requireNotNull(uri.lastPathSegment).toLong()
        assertTrue("insert returned id $insertedId — the row was not written", insertedId > 0)

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertTrue("no configuration value row was written", cursor.moveToFirst())
            val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
            assertEquals(configId, fromCursor.configurationId)
            assertEquals("true", fromCursor.value)
            assertEquals(defaultScopeId, fromCursor.scope)
        }
    }

    @Test
    fun testInsertDuplicateReturnsExistingRowWithoutCreatingSecondRow() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "duplicateInsertKey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()
        val defaultScopeId = getDefaultScopeId()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = defaultScopeId
        }
        val firstUri = togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )
        val firstId = requireNotNull(firstUri?.lastPathSegment).toLong()

        // Same (configurationId, scope) pair again: insertSync throws on the unique constraint,
        // and the fallback lookup must resolve to the row that already exists.
        val secondUri = togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.copy(value = "irrelevant").toContentValues(),
        )
        val secondId = requireNotNull(secondUri?.lastPathSegment).toLong()

        assertEquals("duplicate insert must return the existing row's id", firstId, secondId)

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertEquals("duplicate insert must not create a second row", 1, cursor.count)
        }
    }

    @Test
    fun testInsertWithNonexistentScopeReturnsNullAndWritesNoRow() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "nonexistentScopeInsertKey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = 999L
        }
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        assertEquals("insert against a nonexistent scope must return null", null, uri)

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertEquals("failed insert must not write a row", 0, cursor.count)
        }
    }

    @Test
    fun testInsertWithNullValueReturnsNullAndWritesNoRow() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "nullValueInsertKey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()
        val defaultScopeId = getDefaultScopeId()

        // Deliberately omit setValue(): configurationValue.value is NOT NULL at the database
        // level (see MIGRATION_11_12), so TogglesProvider.insert must reject this up front
        // instead of attempting — and failing — the write.
        val configValue = TogglesConfigurationValue {
            configurationId = configId
            scope = defaultScopeId
        }
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        assertEquals("insert with a null value must return null", null, uri)

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null,
            null,
            null,
            null
        ).use { cursor ->
            assertEquals("rejected insert must not write a row", 0, cursor.count)
        }
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
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = getDefaultScopeId()
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
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()

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
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()

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
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()

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
        val configId = requireNotNull(configUri?.lastPathSegment).toLong()

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
