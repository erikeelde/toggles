package se.eelde.toggles.core

import android.content.ContentValues
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class TogglesConfigurationKeyValidationTest {

    @Test
    fun buildAcceptsANonBlankKey() {
        val configuration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "my_key"
        }

        assertEquals("my_key", configuration.key)
    }

    @Test
    fun buildRejectsTheUnsetDefaultKey() {
        assertThrows(IllegalArgumentException::class.java) {
            TogglesConfiguration { type = Toggle.TYPE.BOOLEAN }
        }
    }

    @Test
    fun buildRejectsAWhitespaceOnlyKey() {
        assertThrows(IllegalArgumentException::class.java) {
            TogglesConfiguration {
                type = Toggle.TYPE.BOOLEAN
                key = "   "
            }
        }
    }

    @Test
    fun copyRejectsABlankKey() {
        val configuration = TogglesConfiguration {
            type = Toggle.TYPE.STRING
            key = "my_key"
        }

        assertThrows(IllegalArgumentException::class.java) {
            configuration.copy(key = "")
        }
    }

    @Test
    fun fromContentValuesRejectsAMissingKey() {
        val values = ContentValues().apply {
            put(ColumnNames.Configuration.COL_ID, 0L)
            put(ColumnNames.Configuration.COL_TYPE, Toggle.TYPE.BOOLEAN)
        }

        assertThrows(IllegalArgumentException::class.java) {
            TogglesConfiguration.fromContentValues(values)
        }
    }

    @Test
    fun fromContentValuesRejectsAnEmptyKey() {
        assertThrows(IllegalArgumentException::class.java) {
            TogglesConfiguration.fromContentValues(contentValuesWithKey(""))
        }
    }

    @Test
    fun fromContentValuesRejectsAWhitespaceOnlyKey() {
        assertThrows(IllegalArgumentException::class.java) {
            TogglesConfiguration.fromContentValues(contentValuesWithKey("\t\n "))
        }
    }

    @Test
    fun fromContentValuesAcceptsANonBlankKey() {
        assertEquals(
            "my_key",
            TogglesConfiguration.fromContentValues(contentValuesWithKey("my_key")).key
        )
    }

    private fun contentValuesWithKey(key: String) = ContentValues().apply {
        put(ColumnNames.Configuration.COL_ID, 0L)
        put(ColumnNames.Configuration.COL_TYPE, Toggle.TYPE.BOOLEAN)
        put(ColumnNames.Configuration.COL_KEY, key)
    }
}
