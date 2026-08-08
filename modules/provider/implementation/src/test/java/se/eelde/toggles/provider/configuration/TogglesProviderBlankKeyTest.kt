package se.eelde.toggles.provider.configuration

import android.app.Application
import android.content.ContentValues
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.provider.TogglesProvider
import se.eelde.toggles.provider.di.ToggleTestApplication_Application
import javax.inject.Inject

/**
 * The official clients cannot build a blank-key configuration - the builders reject it. These
 * tests go around the builders with raw [ContentValues] to cover a caller writing straight to the
 * provider, since the unique `(applicationId, configurationKey)` index would otherwise collapse
 * every unnamed toggle onto one shared row.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = ToggleTestApplication_Application::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderBlankKeyTest {
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
    fun insertConfigurationRejectsABlankKey() {
        BLANK_KEYS.forEach { key ->
            assertThrows(IllegalArgumentException::class.java) {
                togglesProvider.insert(
                    TogglesProviderContract.configurationUri(),
                    configurationValues(key)
                )
            }
        }
    }

    @Test
    fun updateConfigurationRejectsABlankKey() {
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            configurationValues("myConfigurationkey")
        )
        val configurationId = requireNotNull(uri?.lastPathSegment).toLong()

        BLANK_KEYS.forEach { key ->
            assertThrows(IllegalArgumentException::class.java) {
                togglesProvider.update(
                    TogglesProviderContract.configurationUri(configurationId),
                    configurationValues(key),
                    null,
                    null
                )
            }
        }
    }

    @Test
    fun insertCurrentConfigurationRejectsABlankKey() {
        BLANK_KEYS.forEach { key ->
            val values = ContentValues().apply {
                put(ColumnNames.Toggle.COL_ID, 0L)
                put(ColumnNames.Toggle.COL_TYPE, Toggle.TYPE.BOOLEAN)
                put(ColumnNames.Toggle.COL_KEY, key)
                put(ColumnNames.Toggle.COL_VALUE, "true")
            }

            assertThrows(IllegalArgumentException::class.java) {
                togglesProvider.insert(TogglesProviderContract.toggleUri(), values)
            }
        }
    }

    private fun configurationValues(key: String) = ContentValues().apply {
        put(ColumnNames.Configuration.COL_ID, 0L)
        put(ColumnNames.Configuration.COL_TYPE, Toggle.TYPE.BOOLEAN)
        put(ColumnNames.Configuration.COL_KEY, key)
    }

    companion object {
        private val BLANK_KEYS = listOf("", "   ", "\t\n")
    }
}
