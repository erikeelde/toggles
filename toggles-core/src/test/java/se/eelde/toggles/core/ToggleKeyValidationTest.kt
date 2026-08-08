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
internal class ToggleKeyValidationTest {

    @Test
    fun buildAcceptsANonBlankKey() {
        val toggle = Toggle {
            type = Toggle.TYPE.BOOLEAN
            key = "my_key"
            value = "true"
        }

        assertEquals("my_key", toggle.key)
    }

    @Test
    fun buildRejectsTheUnsetDefaultKey() {
        assertThrows(IllegalArgumentException::class.java) {
            Toggle {
                type = Toggle.TYPE.BOOLEAN
                value = "true"
            }
        }
    }

    @Test
    fun buildRejectsAWhitespaceOnlyKey() {
        assertThrows(IllegalArgumentException::class.java) {
            Toggle {
                type = Toggle.TYPE.BOOLEAN
                key = "   "
                value = "true"
            }
        }
    }

    @Test
    fun buildKeepsSurroundingWhitespaceOnAnOtherwiseRealKey() {
        val toggle = Toggle {
            type = Toggle.TYPE.STRING
            key = " my_key "
        }

        assertEquals(" my_key ", toggle.key)
    }

    @Test
    fun copyRejectsABlankKey() {
        val toggle = Toggle {
            type = Toggle.TYPE.STRING
            key = "my_key"
        }

        assertThrows(IllegalArgumentException::class.java) {
            toggle.copy(key = "")
        }
    }

    @Test
    fun fromContentValuesRejectsAMissingKey() {
        val values = ContentValues().apply {
            put(ColumnNames.Toggle.COL_ID, 0L)
            put(ColumnNames.Toggle.COL_TYPE, Toggle.TYPE.BOOLEAN)
            put(ColumnNames.Toggle.COL_VALUE, "true")
        }

        assertThrows(IllegalArgumentException::class.java) {
            Toggle.fromContentValues(values)
        }
    }

    @Test
    fun fromContentValuesRejectsAnEmptyKey() {
        assertThrows(IllegalArgumentException::class.java) {
            Toggle.fromContentValues(contentValuesWithKey(""))
        }
    }

    @Test
    fun fromContentValuesRejectsAWhitespaceOnlyKey() {
        assertThrows(IllegalArgumentException::class.java) {
            Toggle.fromContentValues(contentValuesWithKey("\t\n "))
        }
    }

    @Test
    fun fromContentValuesAcceptsANonBlankKey() {
        assertEquals("my_key", Toggle.fromContentValues(contentValuesWithKey("my_key")).key)
    }

    private fun contentValuesWithKey(key: String) = ContentValues().apply {
        put(ColumnNames.Toggle.COL_ID, 0L)
        put(ColumnNames.Toggle.COL_TYPE, Toggle.TYPE.BOOLEAN)
        put(ColumnNames.Toggle.COL_KEY, key)
        put(ColumnNames.Toggle.COL_VALUE, "true")
    }
}
