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
class TogglesProviderMatcherCurrentConfigurationKeyTest {
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
    fun testUpdateToggle() {
        val updateToggleKey = "updateToggleKey"

        val uri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(updateToggleKey)
        val insertToggleUri = contentResolver.insert(uri, insertToggle.toContentValues())
        assertNotNull(insertToggleUri)

        var cursor = requireNotNull(contentResolver.query(
            TogglesProviderContract.toggleUri(updateToggleKey),
            null,
            null,
            null,
            null
        ))
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
            value = requireNotNull(providerToggle.value) + requireNotNull(providerToggle.value)
        }

        val update = contentResolver.update(
            TogglesProviderContract.toggleUri(updateToggle.id),
            updateToggle.toContentValues(),
            null,
            null
        )
        assertEquals(1, update)

        cursor = requireNotNull(contentResolver.query(
            TogglesProviderContract.toggleUri(updateToggleKey),
            null,
            null,
            null,
            null
        ))
        assertNotNull(cursor)

        assertTrue(cursor.moveToFirst())
        val updatedToggle = Toggle.fromCursor(cursor)

        assertEquals(requireNotNull(insertToggle.value) + requireNotNull(insertToggle.value), updatedToggle.value)
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = contentResolver.getType(TogglesProviderContract.toggleUri("fake"))
        assertEquals("vnd.android.cursor.item/vnd.se.eelde.toggles.currentConfiguration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingInsertForToggleWithKey() {
        contentResolver.insert(
            TogglesProviderContract.toggleUri("fake"),
            getToggle("dummyToggle").toContentValues()
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForToggleWithKey() {
        contentResolver.update(
            TogglesProviderContract.toggleUri("fake"),
            getToggle("dummyToggle").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggleWithKey() {
        contentResolver.delete(TogglesProviderContract.toggleUri("fake"), null, null)
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
