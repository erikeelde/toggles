package se.eelde.toggles.flow

import android.app.Application
import android.content.pm.ProviderInfo
import android.os.Build
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import se.eelde.toggles.database.TogglesConfigurationValue as DbConfigurationValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.FakeTogglesDatabase
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.provider.RobolectricTogglesProvider
import se.eelde.toggles.provider.TogglesProvider

/**
 * Verifies that the flow-based observation registers observers on both the legacy
 * toggleUri (currentConfiguration) and the new configurationUri (configuration) endpoints,
 * and that changes are picked up.
 *
 * The Toggles app updates values via Room DAO and then calls
 * contentResolver.notifyChange(toggleUri(configId)), so the client-side observer must
 * listen on that URI tree too.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class ToggleUriNotificationTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var serverProvider: TogglesProvider
    private lateinit var database: TogglesDatabase

    @Before
    fun setUp() {
        database = FakeTogglesDatabase.create(context)
        serverProvider = RobolectricTogglesProvider.create(
            context = context,
            database = database,
            toggles = FakeToggles(),
            ioDispatcher = testDispatcher,
        )

        // Register the provider with the package manager so that
        // TogglesProvider.providerAvailable resolves to true and observers are registered.
        val providerInfo = ProviderInfo().apply {
            authority = TogglesProviderContract.configurationUri().authority
            packageName = context.packageName
            name = "se.eelde.toggles.provider.TogglesProvider"
        }
        shadowOf(context.packageManager).addOrUpdateProvider(providerInfo)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observer is registered on legacy toggleUri`() = runTest(testDispatcher) {
        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)
        val shadowContentResolver = shadowOf(context.contentResolver)

        toggles.toggle("test_key", "default").test {
            advanceUntilIdle()
            awaitItem()

            // Verify observer is registered on the legacy toggleUri
            val toggleUriObservers = shadowContentResolver
                .getContentObservers(TogglesProviderContract.toggleUri())
            assertTrue(
                "Expected a ContentObserver registered on toggleUri (currentConfiguration)",
                toggleUriObservers.isNotEmpty()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observer is registered on new configurationUri`() = runTest(testDispatcher) {
        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)
        val shadowContentResolver = shadowOf(context.contentResolver)

        toggles.toggle("test_key", "default").test {
            advanceUntilIdle()
            awaitItem()

            // Verify observer is registered on the new configurationUri
            val configUriObservers = shadowContentResolver
                .getContentObservers(TogglesProviderContract.configurationUri())
            assertTrue(
                "Expected a ContentObserver registered on configurationUri",
                configUriObservers.isNotEmpty()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observer is registered on scopeUri`() = runTest(testDispatcher) {
        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)
        val shadowContentResolver = shadowOf(context.contentResolver)

        toggles.toggle("test_key", "default").test {
            advanceUntilIdle()
            awaitItem()

            val scopeUriObservers = shadowContentResolver
                .getContentObservers(TogglesProviderContract.scopeUri())
            assertTrue(
                "Expected a ContentObserver registered on scopeUri",
                scopeUriObservers.isNotEmpty()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `hasOverride emits true after non-default scope value is inserted`() = runTest(testDispatcher) {
        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)

        // Bootstrap: create config (id=1), default scope (id=1), development scope (id=2,
        // higher timestamp = selected), and a value for the default scope only.
        toggles.toggle("reactive-key", "false").test {
            advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        toggles.hasOverride("reactive-key").test {
            advanceUntilIdle()
            assertFalse(awaitItem()) // initial: false — no value for the selected (non-default) scope

            // Insert a value for the non-default (development) scope, then notify observers.
            database.togglesConfigurationValueDao()
                .insertSync(DbConfigurationValue(id = 0L, configurationId = 1L, value = "true", scope = 2L))
            context.contentResolver.notifyChange(TogglesProviderContract.configurationUri(), null)
            shadowOf(Looper.getMainLooper()).idle()
            advanceUntilIdle()

            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `flow emits updated value after database change and notification`() = runTest(testDispatcher) {
        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)

        toggles.toggle("test_key", "initial_default").test {
            advanceUntilIdle()
            assertEquals("initial_default", awaitItem())

            // Simulate what the Toggles app does:
            // 1. Update value directly in database (like Room DAO)
            database.togglesConfigurationValueDao()
                .updateConfigurationValue(1, 1, "updated_value")

            // 2. Notify on the legacy toggleUri (this is what the Toggles app sends)
            context.contentResolver.notifyChange(
                TogglesProviderContract.toggleUri(1L),
                null
            )

            // Process the ContentObserver callback on the main looper,
            // then advance coroutines so getToggleState completes
            shadowOf(Looper.getMainLooper()).idle()
            advanceUntilIdle()

            assertEquals("updated_value", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
