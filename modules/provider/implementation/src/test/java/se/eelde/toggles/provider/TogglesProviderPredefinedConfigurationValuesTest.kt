package se.eelde.toggles.provider

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleValue
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderPredefinedConfigurationValuesTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var togglesProvider: TogglesProvider

    @Inject
    lateinit var togglesDatabase: TogglesDatabase

    @Before
    fun setUp() {
        hiltRule.inject()

        val contentProviderController =
            Robolectric.buildContentProvider(TogglesProvider::class.java)
                .create("se.eelde.toggles.configprovider")
        togglesProvider = contentProviderController.get()

        val context = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(context.packageManager).setApplicationIcon(
            context.applicationInfo.packageName,
            ColorDrawable(Color.RED)
        )
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = togglesProvider.getType(TogglesProviderContract.toggleValueUri())
        assertEquals(
            "vnd.android.cursor.dir/vnd.se.eelde.toggles.predefinedConfigurationValue",
            type
        )
    }

    @Test
    fun testInsertPredefinedConfigurationValueType() = runTest {
        val insertToggleKey = "insertToggleKey"

        val toggleUri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(insertToggleKey)
        val insertToggleUri = togglesProvider.insert(toggleUri, insertToggle.toContentValues())

        val configId = insertToggleUri.lastPathSegment!!.toLong()
        val toggleValue = ToggleValue {
            configurationId = configId
            value = "FIRST"
        }

        togglesProvider.insert(
            TogglesProviderContract.toggleValueUri(),
            toggleValue.toContentValues()
        )
        togglesDatabase.togglesPredefinedConfigurationValueDao().getByConfigurationId(configId)
            .test {
                val togglesPredefinedConfigurationValueList = awaitItem()
                assertEquals(1, togglesPredefinedConfigurationValueList.size)
                togglesPredefinedConfigurationValueList[0].let {
                    assertEquals(toggleValue.value, it.value)
                    assertEquals(toggleValue.configurationId, it.configurationId)
                }
            }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForToggleValues() {
        togglesProvider.update(
            TogglesProviderContract.toggleValueUri(),
            getToggleValue("dummyToggleValue").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForToggleValues() {
        togglesProvider.update(
            TogglesProviderContract.toggleValueUri(),
            getToggleValue("dummyToggleValue").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggleValues() {
        togglesProvider.delete(TogglesProviderContract.toggleValueUri(), null, null)
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
