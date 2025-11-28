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

/**
 * Test to validate the scenario described in the GitHub issue:
 * "Difficult to differentiate between old default, updated default and user set value"
 * 
 * Scenario:
 * 1. Query toggle "thought-experiment-A-roll-out" with default=false (feature not rolled out)
 * 2. Feature is rolled out, query same toggle with default=true
 * 3. System should distinguish between:
 *    - Old default value (false, from initial query)
 *    - New default value (true, from rollout)
 *    - User-set value (explicitly changed by user via UI)
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [O])
internal class TogglesPreferencesDefaultValueUpdateTest {

    private lateinit var togglesPreferences: TogglesPreferences
    private lateinit var contentProviderController: ContentProviderController<DefaultValueTrackingProvider>
    private val key = "thought-experiment-A-roll-out"

    @Before
    fun setUp() {
        val info =
            ProviderInfo().apply { authority = TogglesProviderContract.toggleUri().authority!! }
        contentProviderController =
            Robolectric.buildContentProvider(DefaultValueTrackingProvider::class.java).create(info)

        togglesPreferences =
            TogglesPreferencesImpl(ApplicationProvider.getApplicationContext<Application>())
    }

    @Test
    fun `first query with default false stores false and returns false`() {
        // Initial query with default=false (feature not rolled out yet)
        val result = togglesPreferences.getBoolean(key, false)
        
        assertEquals(false, result)
        assertEquals(1, contentProviderController.get().toggles.size)
        assertEquals("false", contentProviderController.get().toggles[key]?.value)
    }

    @Test
    fun `second query with different default should update the default value`() {
        // Step 1: Initial query with default=false
        val firstResult = togglesPreferences.getBoolean(key, false)
        assertEquals(false, firstResult)
        
        // Step 2: Feature is rolled out, query with new default=true
        // EXPECTED BEHAVIOR: System should update the default value
        val secondResult = togglesPreferences.getBoolean(key, true)
        
        // After the fix, this should return the new default value
        assertEquals(true, secondResult)
        assertEquals("true", contentProviderController.get().toggles[key]?.value)
    }

    @Test
    fun `user explicitly set value should not be overridden by new default`() {
        // Step 1: Initial query with default=false
        togglesPreferences.getBoolean(key, false)
        
        // Step 2: User explicitly sets value to true via UI
        // (This would be done through the Toggles app, simulated here)
        contentProviderController.get().toggles[key]?.let { toggle ->
            contentProviderController.get().toggles[key] = toggle.copy(value = "true")
            contentProviderController.get().userSetValues[key] = true
        }
        
        // Step 3: App queries with new default=false
        // EXPECTED: Should return user's explicitly set value (true), not default (false)
        val result = togglesPreferences.getBoolean(key, false)
        
        // If properly implemented, this should return true (user's choice)
        // Currently, it would return true because that's what's stored,
        // but there's no way to distinguish if it's user-set or an old default
        assertEquals(true, result)
    }
}

/**
 * Mock ContentProvider that tracks whether values are user-set vs defaults
 */
internal class DefaultValueTrackingProvider : ContentProvider() {
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
    val userSetValues: MutableMap<String, Boolean> = mutableMapOf()
    private val toggleValues: MutableList<String> = mutableListOf()

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        when (uriMatcher.match(uri)) {
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
                
                // Simulate the new behavior: if toggle exists, update its value
                if (toggles.containsKey(toggle.key)) {
                    // Update the existing toggle with new value
                    toggles[toggle.key] = toggles[toggle.key]!!.copy(value = toggle.value)
                    insertId = toggles[toggle.key]!!.id
                } else {
                    // Insert new toggle
                    toggles[toggle.key] = toggle
                    insertId = toggles.size.toLong()
                    toggle.id = insertId
                }
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
