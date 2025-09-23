package se.eelde.toggles.provider

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TogglesProviderMatcherCurrentConfigurationTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val contentResolver = context.contentResolver

    @Inject
    lateinit var togglesDatabase: TogglesDatabase

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = contentResolver.getType(TogglesProviderContract.toggleUri())
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.currentConfiguration", type)
    }

    @Test
    fun testInsertToggle() {
        val insertToggleKey = "insertToggleKey"

        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(insertToggleKey)
        val insertToggleUri = contentResolver.insert(uri, insertToggle.toContentValues())!!
        assertNotNull(insertToggleUri)

        var cursor = contentResolver.query(
            TogglesProviderContract.toggleUri(insertToggleKey),
            null,
            null,
            null,
            null
        )!!
        assertNotNull(cursor)
        assertEquals(1, cursor.count)

        cursor.moveToFirst()
        var queryToggle = Toggle.fromCursor(cursor)

        assertEquals(insertToggle.key, queryToggle.key)
        assertEquals(insertToggle.value, queryToggle.value)
        assertEquals(insertToggle.type, queryToggle.type)

        val toggleUri = TogglesProviderContract.toggleUri(
            Integer.parseInt(insertToggleUri.lastPathSegment!!).toLong()
        )
        cursor = contentResolver.query(toggleUri, null, null, null, null)!!
        assertNotNull(cursor)
        assertEquals(1, cursor.count)

        cursor.moveToFirst()
        queryToggle = Toggle.fromCursor(cursor)

        assertEquals(insertToggle.key, queryToggle.key)
        assertEquals(insertToggle.value, queryToggle.value)
        assertEquals(insertToggle.type, queryToggle.type)
    }

    @Test
    fun testUpdateToggle() {
        val updateToggleKey =
            "${this@TogglesProviderMatcherCurrentConfigurationTest::class.simpleName}Updatekey"

        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(updateToggleKey)
        val insertToggleUri = contentResolver.insert(uri, insertToggle.toContentValues())
        assertNotNull(insertToggleUri)

        var cursor = contentResolver.query(
            TogglesProviderContract.toggleUri(updateToggleKey),
            null,
            null,
            null,
            null
        )!!
        assertNotNull(cursor)
        assertTrue(cursor.moveToFirst())

        val providerToggle = Toggle.fromCursor(cursor)
        assertEquals(insertToggle.key, providerToggle.key)
        assertEquals(insertToggle.value, providerToggle.value)
        assertEquals(insertToggle.type, providerToggle.type)

        val updateToggle = Toggle {
            id = providerToggle.id
            type = providerToggle.type
            key = providerToggle.key
            value = providerToggle.value!! + providerToggle.value!!
        }

        val update = contentResolver.update(
            TogglesProviderContract.toggleUri(updateToggle.id),
            updateToggle.toContentValues(),
            null,
            null
        )
        assertEquals(1, update)

        cursor = contentResolver.query(
            TogglesProviderContract.toggleUri(updateToggleKey),
            null,
            null,
            null,
            null
        )!!
        assertNotNull(cursor)

        assertTrue(cursor.moveToFirst())
        val updatedToggle = Toggle.fromCursor(cursor)

        assertEquals(insertToggle.value!! + insertToggle.value!!, updatedToggle.value)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForToggles() {
        contentResolver.update(
            TogglesProviderContract.toggleUri(),
            getToggle("dummyToggle").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForToggles() {
        contentResolver.update(
            TogglesProviderContract.toggleUri(),
            getToggle("dummyToggle").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggles() {
        contentResolver.delete(TogglesProviderContract.toggleUri(), null, null)
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
