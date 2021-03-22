package se.eelde.toggles

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.core.TogglesProviderContract

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TogglesPreferencesReturnsDefaultWhenMissingProviderTest {

    private lateinit var togglesPreferences: TogglesPreferences
    private val key = "myKey"

    private enum class TestEnum {
        FIRST, SECOND
    }

    @Before
    fun setUp() {

        val applicationContext = ApplicationProvider.getApplicationContext<Application>()
        togglesPreferences = TogglesPreferencesImpl(applicationContext)

        val query = applicationContext.contentResolver.query(TogglesProviderContract.toggleUri(""), null, null, null, null)
        Assert.assertNull(query)
    }

    @Test
    fun `always return default enum when missing backing content provider`() {
        Assert.assertEquals(
            TestEnum.FIRST,
            togglesPreferences.getEnum(key, TestEnum::class.java, TestEnum.FIRST)
        )
        Assert.assertEquals(
            TestEnum.SECOND,
            togglesPreferences.getEnum(key, TestEnum::class.java, TestEnum.SECOND)
        )
    }

    @Test
    fun `always return default string when missing backing content provider`() {
        Assert.assertEquals("first", togglesPreferences.getString(key, "first"))
        Assert.assertEquals("second", togglesPreferences.getString(key, "second"))
    }

    @Test
    fun `always return default boolean when missing backing content provider`() {
        Assert.assertEquals(true, togglesPreferences.getBoolean(key, true))
        Assert.assertEquals(false, togglesPreferences.getBoolean(key, false))
    }

    @Test
    fun `always return default int when missing backing content provider`() {
        Assert.assertEquals(1, togglesPreferences.getInt(key, 1))
        Assert.assertEquals(2, togglesPreferences.getInt(key, 2))
    }
}