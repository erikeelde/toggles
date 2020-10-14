package com.izettle.wrench.preferences

import android.app.Application
import android.os.Build.VERSION_CODES.O
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.core.TogglesProviderContract

@RunWith(AndroidJUnit4::class)
@Config(sdk = [O])
class WrenchPreferencesReturnsDefaultWhenMissingProviderTest {

    private lateinit var wrenchPreferences: TogglesPreferences
    private val key = "myKey"

    private enum class TestEnum {
        FIRST, SECOND
    }

    @Before
    fun setUp() {

        val applicationContext = ApplicationProvider.getApplicationContext<Application>()
        wrenchPreferences = TogglesPreferences(applicationContext)

        val query = applicationContext.contentResolver.query(TogglesProviderContract.boltUri(""), null, null, null, null)
        Assert.assertNull(query)
    }

    @Test
    fun `always return default enum when missing backing content provider`() {
        assertEquals(TestEnum.FIRST, wrenchPreferences.getEnum(key, TestEnum::class.java, TestEnum.FIRST))
        assertEquals(TestEnum.SECOND, wrenchPreferences.getEnum(key, TestEnum::class.java, TestEnum.SECOND))
    }

    @Test
    fun `always return default string when missing backing content provider`() {
        assertEquals("first", wrenchPreferences.getString(key, "first"))
        assertEquals("second", wrenchPreferences.getString(key, "second"))
    }

    @Test
    fun `always return default boolean when missing backing content provider`() {
        assertEquals(true, wrenchPreferences.getBoolean(key, true))
        assertEquals(false, wrenchPreferences.getBoolean(key, false))
    }

    @Test
    fun `always return default int when missing backing content provider`() {
        assertEquals(1, wrenchPreferences.getInt(key, 1))
        assertEquals(2, wrenchPreferences.getInt(key, 2))
    }
}
