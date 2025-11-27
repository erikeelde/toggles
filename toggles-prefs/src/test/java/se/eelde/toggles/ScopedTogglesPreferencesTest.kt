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
internal class ScopedTogglesPreferencesTest {

    private val key = "scopedKey"
    private lateinit var contentProviderController: ContentProviderController<ScopedMockContentProvider>

    @Before
    fun setUp() {
        val info =
            ProviderInfo().apply { authority = TogglesProviderContract.toggleUri().authority!! }
        contentProviderController =
            Robolectric.buildContentProvider(ScopedMockContentProvider::class.java).create(info)
    }

    @Test
    fun `return different values for different scopes`() {
        val provider = contentProviderController.get()

        // Set up different values for different scopes
        provider.scopedToggles["user1"] = mutableMapOf(
            key to Toggle {
                id = 1
                type = Toggle.TYPE.STRING
                this.key = key
                value = "user1_value"
            }
        )
        provider.scopedToggles["user2"] = mutableMapOf(
            key to Toggle {
                id = 2
                type = Toggle.TYPE.STRING
                this.key = key
                value = "user2_value"
            }
        )

        // Create preferences for each scope
        val prefsUser1 = TogglesPreferencesImpl(
            ApplicationProvider.getApplicationContext<Application>(),
            scope = "user1"
        )
        val prefsUser2 = TogglesPreferencesImpl(
            ApplicationProvider.getApplicationContext<Application>(),
            scope = "user2"
        )

        // Verify each scope gets its own value
        assertEquals("user1_value", prefsUser1.getString(key, "default"))
        assertEquals("user2_value", prefsUser2.getString(key, "default"))
    }

    @Test
    fun `return different boolean values for different scopes`() {
        val provider = contentProviderController.get()

        // Set up different boolean values for different scopes
        provider.scopedToggles["admin"] = mutableMapOf(
            key to Toggle {
                id = 1
                type = Toggle.TYPE.BOOLEAN
                this.key = key
                value = "true"
            }
        )
        provider.scopedToggles["guest"] = mutableMapOf(
            key to Toggle {
                id = 2
                type = Toggle.TYPE.BOOLEAN
                this.key = key
                value = "false"
            }
        )

        val prefsAdmin = TogglesPreferencesImpl(
            ApplicationProvider.getApplicationContext<Application>(),
            scope = "admin"
        )
        val prefsGuest = TogglesPreferencesImpl(
            ApplicationProvider.getApplicationContext<Application>(),
            scope = "guest"
        )

        assertEquals(true, prefsAdmin.getBoolean(key, false))
        assertEquals(false, prefsGuest.getBoolean(key, true))
    }

    @Test
    fun `use default scope when no scope specified`() {
        val provider = contentProviderController.get()

        // Set up value in default scope
        provider.scopedToggles[null] = mutableMapOf(
            key to Toggle {
                id = 1
                type = Toggle.TYPE.INTEGER
                this.key = key
                value = "42"
            }
        )

        val prefs = TogglesPreferencesImpl(
            ApplicationProvider.getApplicationContext<Application>()
        )

        assertEquals(42, prefs.getInt(key, 0))
    }
}

internal class ScopedMockContentProvider : ContentProvider() {
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

    // Map of scope name to toggles map
    val scopedToggles: MutableMap<String?, MutableMap<String, Toggle>> = mutableMapOf()
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

                // Extract scope from query parameter
                val scope = uri.getQueryParameter("SCOPE")

                uri.lastPathSegment?.let { key ->
                    scopedToggles[scope]?.get(key)?.let { toggle ->
                        cursor.addRow(arrayOf<Any?>(toggle.id, toggle.key, toggle.type, toggle.value))
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
                val scope = uri.getQueryParameter("SCOPE")
                val scopeToggles = scopedToggles.getOrPut(scope) { mutableMapOf() }
                require(!scopeToggles.containsKey(toggle.key)) { "toggle exists" }
                scopeToggles[toggle.key] = toggle
                insertId = scopeToggles.size.toLong()
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
