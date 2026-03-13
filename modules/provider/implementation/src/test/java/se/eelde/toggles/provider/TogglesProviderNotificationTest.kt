package se.eelde.toggles.provider

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleScope
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import javax.inject.Inject

@Suppress("DEPRECATION")
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderNotificationTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var togglesProvider: TogglesProvider
    private lateinit var shadowContentResolver: ShadowContentResolver

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

        shadowContentResolver = shadowOf(context.contentResolver)
    }

    // region Scope notifications

    @Test
    fun `first query triggers scope creation notification`() {
        // The first query triggers getCallingApplication which creates default + development scopes
        togglesProvider.query(
            TogglesProviderContract.scopeUri(),
            null,
            null,
            null,
            null
        ).use { }

        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected scope URI notification after scope creation",
            notifiedUris.any { it.uri.toString().contains("/scope") }
        )
    }

    // endregion

    // region Configuration insert notifications

    @Test
    fun `insert configuration notifies on configuration URI`() {
        // First call triggers scope notification - clear it
        triggerInitialSetup()
        shadowContentResolver.getNotifiedUris().clear()

        val config = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "notifyConfigKey"
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            config.toContentValues()
        )

        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected configuration URI notification after insert, got: ${notifiedUris.map { it.uri }}",
            notifiedUris.any { it.uri.toString().contains("/configuration/") }
        )
    }

    // endregion

    // region Configuration value insert notifications

    @Test
    fun `insert configuration value notifies on configuration value URI`() {
        val configId = insertConfiguration("notifyValueKey")
        shadowContentResolver.getNotifiedUris().clear()

        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = getDefaultScopeId()
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues()
        )

        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected configuration value URI notification after insert, got: ${notifiedUris.map { it.uri }}",
            notifiedUris.any { it.uri.toString().contains("/values/") }
        )
    }

    // endregion

    // region Configuration value update notifications

    @Test
    fun `update configuration value notifies on configuration value URI`() {
        val configId = insertConfiguration("notifyUpdateKey")
        val defaultScopeId = getDefaultScopeId()
        val configValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = defaultScopeId
        }
        togglesProvider.insert(
            TogglesProviderContract.configurationValueUri(configId),
            configValue.toContentValues()
        )
        shadowContentResolver.getNotifiedUris().clear()

        val updatedValue = configValue.copy(value = "false")
        togglesProvider.update(
            TogglesProviderContract.configurationValueUri(configId),
            updatedValue.toContentValues(),
            null,
            null
        )

        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected configuration value URI notification after update, got: ${notifiedUris.map { it.uri }}",
            notifiedUris.any { it.uri.toString().contains("/values") }
        )
    }

    @Test
    fun `update with zero rows affected does not notify`() {
        val configId = insertConfiguration("noNotifyUpdateKey")
        shadowContentResolver.getNotifiedUris().clear()

        val nonExistentValue = TogglesConfigurationValue {
            configurationId = configId
            value = "true"
            scope = 999L
        }
        togglesProvider.update(
            TogglesProviderContract.configurationValueUri(configId),
            nonExistentValue.toContentValues(),
            null,
            null
        )

        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected no notifications when update affects zero rows, got: ${notifiedUris.map { it.uri }}",
            notifiedUris.none { it.uri.toString().contains("/values") }
        )
    }

    // endregion

    // region Legacy toggle insert notifications

    @Test
    fun `insert legacy toggle notifies on toggle URI`() {
        triggerInitialSetup()
        shadowContentResolver.getNotifiedUris().clear()

        val toggle = Toggle {
            id = 0L
            type = Toggle.TYPE.BOOLEAN
            key = "legacyNotifyKey"
            value = "true"
        }
        togglesProvider.insert(
            TogglesProviderContract.toggleUri(),
            toggle.toContentValues()
        )

        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected toggle URI notification after legacy insert, got: ${notifiedUris.map { it.uri }}",
            notifiedUris.any { it.uri.toString().contains("/currentConfiguration/") }
        )
    }

    // endregion

    // region Legacy toggle update notifications

    @Test
    fun `update legacy toggle notifies on toggle URI`() {
        val toggle = Toggle {
            id = 0L
            type = Toggle.TYPE.BOOLEAN
            key = "legacyUpdateNotifyKey"
            value = "true"
        }
        togglesProvider.insert(
            TogglesProviderContract.toggleUri(),
            toggle.toContentValues()
        )

        val queriedToggle = togglesProvider.query(
            TogglesProviderContract.toggleUri("legacyUpdateNotifyKey"),
            null,
            null,
            null,
            null
        ).use { cursor ->
            cursor.moveToFirst()
            Toggle.fromCursor(cursor)
        }

        shadowContentResolver.getNotifiedUris().clear()

        val updateToggle = Toggle {
            id = queriedToggle.id
            type = queriedToggle.type
            key = queriedToggle.key
            value = "false"
        }
        togglesProvider.update(
            TogglesProviderContract.toggleUri(queriedToggle.id),
            updateToggle.toContentValues(),
            null,
            null
        )

        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected toggle URI notification after legacy update, got: ${notifiedUris.map { it.uri }}",
            notifiedUris.any { it.uri.toString().contains("/currentConfiguration/") }
        )
    }

    // endregion

    // region Delete notifications

    @Test
    fun `delete configuration by id notifies`() {
        val configId = insertConfiguration("deleteNotifyKey")
        shadowContentResolver.getNotifiedUris().clear()

        togglesProvider.delete(
            TogglesProviderContract.configurationUri(configId),
            null,
            null
        )

        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected configuration URI notification after delete, got: ${notifiedUris.map { it.uri }}",
            notifiedUris.any { it.uri.toString().contains("/configuration/") }
        )
    }

    @Test
    fun `delete configuration by key notifies`() {
        insertConfiguration("deleteByKeyNotifyKey")
        shadowContentResolver.getNotifiedUris().clear()

        togglesProvider.delete(
            TogglesProviderContract.configurationUri("deleteByKeyNotifyKey"),
            null,
            null
        )

        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected configuration URI notification after delete by key, got: ${notifiedUris.map { it.uri }}",
            notifiedUris.any { it.uri.toString().contains("/configuration/") }
        )
    }

    @Test
    fun `delete non-existent configuration does not notify`() {
        triggerInitialSetup()
        shadowContentResolver.getNotifiedUris().clear()

        val deletedRows = togglesProvider.delete(
            TogglesProviderContract.configurationUri(999L),
            null,
            null
        )

        assertTrue("Expected zero deleted rows", deletedRows == 0)
        val notifiedUris = shadowContentResolver.getNotifiedUris()
        assertTrue(
            "Expected no notifications when delete affects zero rows, got: ${notifiedUris.map { it.uri }}",
            notifiedUris.none { it.uri.toString().contains("/configuration/") }
        )
    }

    // endregion

    // region Cursor notification URI

    @Test
    fun `query cursor has notification URI set`() {
        triggerInitialSetup()

        val cursor = togglesProvider.query(
            TogglesProviderContract.scopeUri(),
            null,
            null,
            null,
            null
        )
        cursor.use {
            assertTrue(
                "Expected cursor notification URI to be set",
                it.notificationUri != null
            )
            assertTrue(
                "Expected cursor notification URI to match query URI",
                it.notificationUri.toString().contains("/scope")
            )
        }
    }

    // endregion

    // region Helper methods

    private fun triggerInitialSetup() {
        togglesProvider.query(
            TogglesProviderContract.scopeUri(),
            null,
            null,
            null,
            null
        ).use { }
    }

    private fun insertConfiguration(key: String): Long {
        val config = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            this.key = key
        }
        val uri = togglesProvider.insert(
            TogglesProviderContract.configurationUri(),
            config.toContentValues()
        )
        return requireNotNull(uri.lastPathSegment).toLong()
    }

    private fun getDefaultScopeId(): Long {
        togglesProvider.query(
            TogglesProviderContract.scopeUri(),
            null,
            null,
            null,
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val scope = ToggleScope.fromCursor(cursor)
                if (scope.name == "toggles_default") {
                    return scope.id
                }
            }
        }
        error("Default scope not found")
    }

    // endregion
}
