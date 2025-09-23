package se.eelde.toggles.provider

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleValue
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TogglesProviderPredefinedConfigurationValuesTeste {
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
        val type = contentResolver.getType(TogglesProviderContract.toggleValueUri())
        assertEquals(
            "vnd.android.cursor.dir/vnd.se.eelde.toggles.predefinedConfigurationValue",
            type
        )
    }

    @Test
    fun testInsertPredefinedConfigurationValueType() = runTest {
        val insertToggleKey =
            "${this@TogglesProviderPredefinedConfigurationValuesTeste::class.simpleName}InsertKey"

        val insertToggle = getToggle(insertToggleKey)
        val insertToggleUri = contentResolver.insert(
            TogglesProviderContract.toggleUri(),
            insertToggle.toContentValues()
        )!!

        val configId = insertToggleUri.lastPathSegment!!.toLong()
        val toggleValue = ToggleValue {
            configurationId = configId
            value = "FIRST"
        }

        contentResolver.insert(
            TogglesProviderContract.toggleValueUri(),
            toggleValue.toContentValues()
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForToggleValues() {
        contentResolver.update(
            TogglesProviderContract.toggleValueUri(),
            getToggleValue("dummyToggleValue").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForToggleValues() {
        contentResolver.update(
            TogglesProviderContract.toggleValueUri(),
            getToggleValue("dummyToggleValue").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggleValues() {
        contentResolver.delete(TogglesProviderContract.toggleValueUri(), null, null)
    }

    private fun getToggleValue(value: String): ToggleValue {
        return ToggleValue {
            id = 0
            this.value = value
        }
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
