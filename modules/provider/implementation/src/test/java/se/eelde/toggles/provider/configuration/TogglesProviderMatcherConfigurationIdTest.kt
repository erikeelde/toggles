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
class TogglesProviderMatcherConfigurationIdTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var togglesProvider: TogglesProvider

    val togglesConfiguration = TogglesConfiguration {
        type = Toggle.TYPE.BOOLEAN
        key = "myConfigurationkey"
    }

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
        val type = togglesProvider.getType(TogglesProviderContract.configurationUri(0))
        assertEquals("vnd.android.cursor.item/vnd.se.eelde.toggles.configuration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testInsert() {
        togglesProvider.insert(
            TogglesProviderContract.configurationUri(0),
            null
        )
    }

    @Test
    fun testUpdate() {
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val updatedConfiguration =
            togglesConfiguration.copy(key = "newKey", type = Toggle.TYPE.STRING)

        val rowsUpdated = togglesProvider.update(
            TogglesProviderContract.configurationUri(uri.lastPathSegment!!),
            updatedConfiguration.toContentValues(),
            null,
            null
        )

        val query = togglesProvider.query(uri, null, null, null, null)
        assertTrue(query.moveToFirst())
        val fromCursor = TogglesConfiguration.fromCursor(query)

        assertEquals(1, rowsUpdated)
        assertEquals(1, fromCursor.id)
    }

    @Test
    fun testQuery() {
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val configurationUri = TogglesProviderContract.configurationUri(uri.lastPathSegment!!)

        val cursor = togglesProvider.query(configurationUri, null, null, null, null)
        assertTrue(cursor.moveToFirst())
        TogglesConfiguration.fromCursor(cursor).also { cursorConfiguration ->
            assertEquals(togglesConfiguration.key, cursorConfiguration.key)
            assertEquals(togglesConfiguration.type, cursorConfiguration.type)
        }
    }

    @Test
    fun testDelete() {
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        val rowsDeleted = togglesProvider.delete(
            TogglesProviderContract.configurationUri(uri.lastPathSegment!!),
            null,
            null
        )
        assertEquals(1, rowsDeleted)
    }
}
