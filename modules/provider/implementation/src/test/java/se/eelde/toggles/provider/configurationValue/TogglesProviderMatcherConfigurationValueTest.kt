package se.eelde.toggles.provider.configurationValue

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
import se.eelde.toggles.core.TogglesConfigurationValue
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.provider.TogglesProvider
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderMatcherConfigurationValueTest {
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
    fun testGetTypeConfigurationValueById() {
        val type = togglesProvider.getType(TogglesProviderContract.configurationValueUri(1L))
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.configurationValue", type)
    }

    @Test
    fun testGetTypeConfigurationValueByKey() {
        val type = togglesProvider.getType(TogglesProviderContract.configurationValueUri("key"))
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.configurationValue", type)
    }

    @Test
    fun testQueryById() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null, null, null, null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
            assertEquals(configId, fromCursor.configurationId)
            assertEquals("true", fromCursor.value)
        }
    }

    @Test
    fun testQueryByKey() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri("myConfigurationkey"),
            null, null, null, null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
            assertEquals(configId, fromCursor.configurationId)
            assertEquals("true", fromCursor.value)
        }
    }

    @Test
    fun testInsert() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
        }
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        assertTrue(uri.toString().contains("/values/"))
    }

    @Test
    fun testUpdate() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "myConfigurationkey"
        }

        val configUri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )
        val configId = requireNotNull(configUri.lastPathSegment).toLong()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues(),
        )

        val updatedValue = configValue.copy(value = "false")
        val rowsUpdated = togglesProvider.update(
            TogglesProviderContract.configurationValueUri(configId),
            updatedValue.toContentValues(),
            null, null
        )

        assertEquals(1, rowsUpdated)

        togglesProvider.query(
            TogglesProviderContract.configurationValueUri(configId),
            null, null, null, null
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            val fromCursor = TogglesConfigurationValue.fromCursor(cursor)
            assertEquals("false", fromCursor.value)
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testDeleteById() {
        togglesProvider.delete(
            TogglesProviderContract.configurationValueUri(1L),
            null, null,
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testDeleteByKey() {
        togglesProvider.delete(
            TogglesProviderContract.configurationValueUri("key"),
            null, null,
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testInsertByKey() {
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri("key"),
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testUpdateByKey() {
        togglesProvider.update(
            TogglesProviderContract.configurationValueUri("key"),
            null, null, null
        )
    }
}
