package se.eelde.toggles

import android.app.Application
import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
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
import se.eelde.toggles.core.Toggle
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

    private lateinit var contentProviderController: ContentProviderController<MockContentProvider>

    @Before
    fun setUp() {
        val info =
            ProviderInfo().apply { authority = TogglesProviderContract.toggleUri().authority!! }
        contentProviderController =
            Robolectric.buildContentProvider(MockContentProvider::class.java).create(info)

        togglesPreferences =
            TogglesPreferencesImpl(ApplicationProvider.getApplicationContext<Application>())
    }

    @Test
    fun `return provider enum when available`() {
        assertEquals(0, contentProviderController.get().toggles.size)

        assertEquals(
            TestEnum.FIRST,
            togglesPreferences.getEnum(
                key,
                TestEnum::class.java,
                TestEnum.FIRST
            )
        )
        assertEquals(
            TestEnum.FIRST,
            togglesPreferences.getEnum(
                key,
                TestEnum::class.java,
                TestEnum.SECOND
            )
        )
    }

    @Test
    fun `return provider string when available`() {
        assertEquals(0, contentProviderController.get().toggles.size)

        assertEquals("first", togglesPreferences.getString(key, "first"))
        assertEquals("first", togglesPreferences.getString(key, "second"))
    }

    @Test
    fun `return provider boolean when available`() {
        assertEquals(0, contentProviderController.get().toggles.size)

        assertEquals(true, togglesPreferences.getBoolean(key, true))
        assertEquals(true, togglesPreferences.getBoolean(key, false))
    }

    @Test
    fun `return provider int when available`() {
        assertEquals(0, contentProviderController.get().toggles.size)

        assertEquals(1, togglesPreferences.getInt(key, 1))
        assertEquals(1, togglesPreferences.getInt(key, 2))
    }
}

internal class MockContentProvider : ContentProvider() {
    companion object {
        private const val CURRENT_CONFIGURATION_ID = 1
        private const val CURRENT_CONFIGURATION_KEY = 2
        private const val CURRENT_CONFIGURATIONS = 3
        private const val PREDEFINED_CONFIGURATION_VALUES = 5

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(
                TogglesProviderContract.toggleUri().authority!!,
                "currentConfiguration/#",
                CURRENT_CONFIGURATION_ID
            )
            uriMatcher.addURI(
                TogglesProviderContract.toggleUri().authority!!,
                "currentConfiguration/*",
                CURRENT_CONFIGURATION_KEY
            )
            uriMatcher.addURI(
                TogglesProviderContract.toggleUri().authority!!,
                "currentConfiguration",
                CURRENT_CONFIGURATIONS
            )
            uriMatcher.addURI(
                TogglesProviderContract.toggleUri().authority!!,
                "predefinedConfigurationValue",
                PREDEFINED_CONFIGURATION_VALUES
            )
        }
    }

    val toggles: MutableMap<String, Toggle> = mutableMapOf()
    private val toggleValues: MutableList<String> = mutableListOf()

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        when (uriMatcher.match(uri)) {
            CURRENT_CONFIGURATION_ID -> {
                throw IllegalArgumentException("toggle exists")
            }
            CURRENT_CONFIGURATION_KEY -> {
                val cursor = MatrixCursor(
                    arrayOf(
                        ColumnNames.Toggle.COL_ID,
                        ColumnNames.Toggle.COL_KEY,
                        ColumnNames.Toggle.COL_TYPE,
                        ColumnNames.Toggle.COL_VALUE
                    )
                )

                uri.lastPathSegment?.let { key ->
                    toggles[key]?.let { toggle ->
                        cursor.addRow(arrayOf(toggle.id, toggle.key, toggle.type, toggle.value))
                    }
                }

                return cursor
            }
            else -> {
                throw UnsupportedOperationException("Not yet implemented $uri")
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val insertId: Long
        when (uriMatcher.match(uri)) {
            CURRENT_CONFIGURATIONS -> {
                val toggle = Toggle.fromContentValues(values!!)
                require(!toggles.containsKey(toggle.key)) { "toggle exists" }
                toggles[toggle.key] = toggle
                insertId = toggles.size.toLong()
                toggle.id = insertId
            }
            PREDEFINED_CONFIGURATION_VALUES -> {
                toggleValues.add(values!!.getAsString(ColumnNames.ToggleValue.COL_VALUE))
                insertId = toggleValues.size.toLong()
            }
            else -> {
                throw UnsupportedOperationException("Not yet implemented $uri")
            }
        }
        return ContentUris.withAppendedId(uri, insertId)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = error("Error")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int =
        error("Error")

    override fun getType(uri: Uri): String = error("Error")
}
