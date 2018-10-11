package com.izettle.wrench.preferences

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
import com.izettle.wrench.core.Bolt
import com.izettle.wrench.core.ColumnNames
import com.izettle.wrench.core.WrenchProviderContract
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ContentProviderController
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [O])
class WrenchPreferencesReturnsProviderValues {

    private lateinit var wrenchPreferences: WrenchPreferences
    private val key = "myKey"

    private enum class TestEnum {
        FIRST, SECOND
    }

    private lateinit var contentProviderController: ContentProviderController<MockContentProvider>

    @Before
    fun setUp() {

        val info = ProviderInfo().apply { authority = WrenchProviderContract.WRENCH_AUTHORITY }
        contentProviderController = Robolectric.buildContentProvider(MockContentProvider::class.java).create(info)

        wrenchPreferences = WrenchPreferences(ApplicationProvider.getApplicationContext<Application>())
    }

    @Test
    fun `return provider enum when available`() {
        assertEquals(0, contentProviderController.get().bolts.size)

        assertEquals(TestEnum.FIRST, wrenchPreferences.getEnum(key, TestEnum::class.java, TestEnum.FIRST))
        assertEquals(TestEnum.FIRST, wrenchPreferences.getEnum(key, TestEnum::class.java, TestEnum.SECOND))
    }

    @Test
    fun `return provider string when available`() {
        assertEquals(0, contentProviderController.get().bolts.size)

        assertEquals("first", wrenchPreferences.getString(key, "first"))
        assertEquals("first", wrenchPreferences.getString(key, "second"))
    }

    @Test
    fun `return provider boolean when available`() {
        assertEquals(0, contentProviderController.get().bolts.size)

        assertEquals(true, wrenchPreferences.getBoolean(key, true))
        assertEquals(true, wrenchPreferences.getBoolean(key, false))
    }

    @Test
    fun `return provider int when available`() {
        assertEquals(0, contentProviderController.get().bolts.size)

        assertEquals(1, wrenchPreferences.getInt(key, 1))
        assertEquals(1, wrenchPreferences.getInt(key, 2))
    }
}


class MockContentProvider : ContentProvider() {
    companion object {
        private const val CURRENT_CONFIGURATION_ID = 1
        private const val CURRENT_CONFIGURATION_KEY = 2
        private const val CURRENT_CONFIGURATIONS = 3
        private const val PREDEFINED_CONFIGURATION_VALUES = 5

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(WrenchProviderContract.WRENCH_AUTHORITY, "currentConfiguration/#", CURRENT_CONFIGURATION_ID)
            uriMatcher.addURI(WrenchProviderContract.WRENCH_AUTHORITY, "currentConfiguration/*", CURRENT_CONFIGURATION_KEY)
            uriMatcher.addURI(WrenchProviderContract.WRENCH_AUTHORITY, "currentConfiguration", CURRENT_CONFIGURATIONS)
            uriMatcher.addURI(WrenchProviderContract.WRENCH_AUTHORITY, "predefinedConfigurationValue", PREDEFINED_CONFIGURATION_VALUES)
        }
    }

    val bolts: MutableMap<String, Bolt> = mutableMapOf()
    private val nuts: MutableList<String> = mutableListOf()


    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        when (uriMatcher.match(uri)) {
            CURRENT_CONFIGURATION_ID -> {
                throw IllegalArgumentException("bolt exists")
            }
            CURRENT_CONFIGURATION_KEY -> {
                val cursor = MatrixCursor(arrayOf(ColumnNames.Bolt.COL_ID, ColumnNames.Bolt.COL_KEY, ColumnNames.Bolt.COL_TYPE, ColumnNames.Bolt.COL_VALUE))

                uri.lastPathSegment?.let { key ->
                    bolts[key]?.let { bolt ->
                        cursor.addRow(arrayOf(bolt.id, bolt.key, bolt.type, bolt.value))
                    }
                }

                return cursor
            }
            else -> {
                throw UnsupportedOperationException("Not yet implemented " + uri.toString())
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues): Uri {
        val insertId: Long
        when (uriMatcher.match(uri)) {
            CURRENT_CONFIGURATIONS -> {
                val bolt = Bolt.fromContentValues(values)
                if (bolts.containsKey(bolt.key)) {
                    throw IllegalArgumentException("bolt exists")
                }
                bolts[bolt.key] = bolt
                insertId = bolts.size.toLong()
                bolt.id = insertId
            }
            PREDEFINED_CONFIGURATION_VALUES -> {
                nuts.add(values.getAsString(ColumnNames.Nut.COL_VALUE))
                insertId = nuts.size.toLong()
            }
            else -> {
                throw UnsupportedOperationException("Not yet implemented $uri")
            }
        }
        return ContentUris.withAppendedId(uri, insertId)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw IllegalStateException()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw IllegalStateException()
    }

    override fun getType(uri: Uri): String? {
        throw IllegalStateException()
    }
}