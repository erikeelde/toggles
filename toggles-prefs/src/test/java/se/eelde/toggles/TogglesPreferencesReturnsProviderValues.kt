package se.eelde.toggles

import android.app.Application
import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build.VERSION_CODES.O
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ContentProviderController
import org.robolectric.annotation.Config
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.prefs.TogglesPreferences
import se.eelde.toggles.prefs.TogglesPreferencesImpl

@RunWith(AndroidJUnit4::class)
@Config(sdk = [O])
internal class TogglesPreferencesReturnsProviderValues {

    private lateinit var togglesPreferences: TogglesPreferences
    private val key = "myKey"

    private enum class TestEnum {
        FIRST, SECOND
    }

    private lateinit var contentProviderController: ContentProviderController<NewApiMockContentProvider>

    @Before
    fun setUp() {
        val info =
            ProviderInfo().apply { authority = TogglesProviderContract.configurationUri().authority!! }
        contentProviderController =
            Robolectric.buildContentProvider(NewApiMockContentProvider::class.java).create(info)

        togglesPreferences =
            TogglesPreferencesImpl(ApplicationProvider.getApplicationContext<Application>())
    }

    @Test
    fun `return provider enum when available`() {
        assertEquals(
            TestEnum.FIRST,
            togglesPreferences.getEnum(key, TestEnum::class.java, TestEnum.FIRST)
        )
        assertEquals(
            TestEnum.FIRST,
            togglesPreferences.getEnum(key, TestEnum::class.java, TestEnum.SECOND)
        )
    }

    @Test
    fun `return provider string when available`() {
        assertEquals("first", togglesPreferences.getString(key, "first"))
        assertEquals("first", togglesPreferences.getString(key, "second"))
    }

    @Test
    fun `return provider boolean when available`() {
        assertEquals(true, togglesPreferences.getBoolean(key, true))
        assertEquals(true, togglesPreferences.getBoolean(key, false))
    }

    @Test
    fun `return provider int when available`() {
        assertEquals(1, togglesPreferences.getInt(key, 1))
        assertEquals(1, togglesPreferences.getInt(key, 2))
    }
}

internal class NewApiMockContentProvider : ContentProvider() {
    data class StoredConfiguration(val id: Long, val key: String, val type: String)
    data class StoredValue(val id: Long, val configurationId: Long, val value: String, val scope: Long)

    private var nextConfigId = 1L
    private var nextValueId = 1L
    private val configurations = mutableListOf<StoredConfiguration>()
    private val configurationValues = mutableListOf<StoredValue>()

    private fun matchUri(uri: Uri): String {
        val segments = uri.pathSegments
        return when {
            segments.size == 1 && segments[0] == "scope" -> "SCOPES"
            segments.size == 1 && segments[0] == "configuration" -> "CONFIGURATIONS"
            segments.size == 2 && segments[0] == "configuration" -> "CONFIGURATION_KEY"
            segments.size == 3 && segments[0] == "configuration" && segments[2] == "values" -> "CONFIGURATION_VALUES"
            segments.size == 1 && segments[0] == "predefinedConfigurationValue" -> "PREDEFINED"
            else -> "UNKNOWN"
        }
    }

    override fun onCreate(): Boolean = true

    @Suppress("ReturnCount")
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        when (matchUri(uri)) {
            "SCOPES" -> {
                val cursor = MatrixCursor(
                    arrayOf(
                        ColumnNames.ToggleScope.COL_ID,
                        ColumnNames.ToggleScope.COL_NAME,
                        ColumnNames.ToggleScope.COL_SELECTED_TIMESTAMP
                    )
                )
                cursor.addRow(
                    arrayOf<Any>(1L, ColumnNames.ToggleScope.DEFAULT_SCOPE, System.currentTimeMillis() - 1000)
                )
                cursor.addRow(arrayOf<Any>(2L, "user", System.currentTimeMillis()))
                return cursor
            }
            "CONFIGURATION_KEY" -> {
                val cursor = MatrixCursor(
                    arrayOf(
                        ColumnNames.Configuration.COL_ID,
                        ColumnNames.Configuration.COL_KEY,
                        ColumnNames.Configuration.COL_TYPE
                    )
                )
                val key = uri.pathSegments[1]
                configurations.find { it.key == key }?.let { config ->
                    cursor.addRow(arrayOf<Any>(config.id, config.key, config.type))
                }
                return cursor
            }
            "CONFIGURATION_VALUES" -> {
                val cursor = MatrixCursor(
                    arrayOf(
                        ColumnNames.ConfigurationValue.COL_ID,
                        ColumnNames.ConfigurationValue.COL_CONFIG_ID,
                        ColumnNames.ConfigurationValue.COL_VALUE,
                        ColumnNames.ConfigurationValue.COL_SCOPE
                    )
                )
                val configId = uri.pathSegments[1].toLong()
                configurationValues.filter { it.configurationId == configId }.forEach { value ->
                    cursor.addRow(arrayOf<Any>(value.id, value.configurationId, value.value, value.scope))
                }
                return cursor
            }
            else -> throw UnsupportedOperationException("Not yet implemented $uri")
        }
    }

    @Suppress("ReturnCount")
    override fun insert(uri: Uri, values: ContentValues?): Uri {
        requireNotNull(values)
        when (matchUri(uri)) {
            "CONFIGURATIONS" -> {
                val id = nextConfigId++
                val config = StoredConfiguration(
                    id = id,
                    key = values.getAsString(ColumnNames.Configuration.COL_KEY),
                    type = values.getAsString(ColumnNames.Configuration.COL_TYPE)
                )
                configurations.add(config)
                return ContentUris.withAppendedId(uri, id)
            }
            "CONFIGURATION_VALUES" -> {
                val id = nextValueId++
                val value = StoredValue(
                    id = id,
                    configurationId = values.getAsLong(ColumnNames.ConfigurationValue.COL_CONFIG_ID),
                    value = values.getAsString(ColumnNames.ConfigurationValue.COL_VALUE),
                    scope = values.getAsLong(ColumnNames.ConfigurationValue.COL_SCOPE)
                )
                configurationValues.add(value)
                return ContentUris.withAppendedId(uri, id)
            }
            "PREDEFINED" -> {
                return ContentUris.withAppendedId(uri, 1L)
            }
            else -> throw UnsupportedOperationException("Not yet implemented $uri")
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        requireNotNull(values)
        when (matchUri(uri)) {
            "CONFIGURATION_VALUES" -> {
                val configId = values.getAsLong(ColumnNames.ConfigurationValue.COL_CONFIG_ID)
                val scope = values.getAsLong(ColumnNames.ConfigurationValue.COL_SCOPE)
                val newValue = values.getAsString(ColumnNames.ConfigurationValue.COL_VALUE)
                val index = configurationValues.indexOfFirst {
                    it.configurationId == configId && it.scope == scope
                }
                if (index >= 0) {
                    configurationValues[index] = configurationValues[index].copy(value = newValue)
                    return 1
                }
                return 0
            }
            else -> throw UnsupportedOperationException("Not yet implemented $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int =
        error("Not implemented")

    override fun getType(uri: Uri): String = error("Not implemented")
}
