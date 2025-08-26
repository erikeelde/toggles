package se.eelde.toggles.provider.configuration

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.provider.TogglesProvider
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderMatcherConfigurationTest {
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
        val type = togglesProvider.getType(TogglesProviderContract.configurationUri())
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.configuration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testUpdate() {
        togglesProvider.update(
            TogglesProviderContract.configurationUri(),
            null,
            null,
            null
        )
    }

    @Test
    fun testInsert() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        assertEquals(
            "content://se.eelde.toggles.configprovider/configuration/1?API_VERSION=1",
            uri.toString()
        )
    }

    val togglesConfiguration = TogglesConfiguration {
        type = Toggle.TYPE.BOOLEAN
        key = "myConfigurationkey"
    }

    @Test
    fun testQuery() {
        togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val cursor = togglesProvider.query(
            TogglesProviderContract.configurationUri(),
            null,
            null,
            null,
            null
        )
        assertTrue(cursor.moveToFirst())
        TogglesConfiguration.fromCursor(cursor).also { cursorConfiguration ->
            assertEquals(togglesConfiguration.key, cursorConfiguration.key)
            assertEquals(togglesConfiguration.type, cursorConfiguration.type)
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testDelete() {
        togglesProvider.delete(
            TogglesProviderContract.configurationUri(),
            null,
            null,
        )
    }
}
