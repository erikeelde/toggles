package se.eelde.toggles.flow

import android.app.Application
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Build
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleScope
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
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

    // region Narrowed-notification filter (onChange skips re-fetches it can prove are irrelevant)

    /**
     * Reads the default scope's id, touching the provider first if this is the first call in
     * the test (which lazily creates the calling application's default + development scopes).
     */
    private fun defaultScopeId(): Long {
        val scopes = mutableListOf<ToggleScope>()
        context.contentResolver.query(TogglesProviderContract.scopeUri(), null, null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        scopes.add(ToggleScope.fromCursor(cursor))
                    } while (cursor.moveToNext())
                }
            }
        return scopes.first { it.name == ColumnNames.ToggleScope.DEFAULT_SCOPE }.id
    }

    /**
     * Creates a configuration and a matching default-scope value for it directly against the
     * real provider, so a flow that subscribes afterwards finds everything already in place and
     * does not need to auto-create anything (and therefore does not itself generate any
     * notifications as a side effect of its first read).
     */
    private fun createConfigurationWithDefaultValue(key: String, type: String, defaultValue: String): Long {
        val scopeId = defaultScopeId()

        val configId = requireNotNull(
            context.contentResolver.insert(
                TogglesProviderContract.configurationUri(),
                TogglesConfiguration.Builder()
                    .setId(0)
                    .setType(type)
                    .setKey(key)
                    .build()
                    .toContentValues()
            )?.lastPathSegment?.toLongOrNull()
        )

        context.contentResolver.insert(
            TogglesProviderContract.configurationValueUri(configId),
            TogglesConfigurationValue.Builder()
                .setId(0)
                .setConfigurationId(configId)
                .setValue(defaultValue)
                .setScope(scopeId)
                .build()
                .toContentValues()
        )

        return configId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `change to a different configuration does not re-fetch`() = runTest(testDispatcher) {
        val watchedConfigId =
            createConfigurationWithDefaultValue("watched_key", Toggle.TYPE.STRING, "watched_default")

        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)

        toggles.toggle("watched_key", "watched_default").test {
            advanceUntilIdle()
            assertEquals("watched_default", awaitItem())

            // A second, unrelated configuration. Its own configurationUri/toggleUri
            // notifications carry an id that differs from watchedConfigId, so they must not
            // wake this flow. (The values-notification trap shape is covered separately below,
            // by the test for rule 5.)
            val otherConfigId = requireNotNull(
                context.contentResolver.insert(
                    TogglesProviderContract.configurationUri(),
                    TogglesConfiguration.Builder()
                        .setId(0)
                        .setType(Toggle.TYPE.STRING)
                        .setKey("unrelated_key")
                        .build()
                        .toContentValues()
                )?.lastPathSegment?.toLongOrNull()
            )
            check(otherConfigId != watchedConfigId) {
                "test setup bug: the unrelated configuration must not share the watched id"
            }

            shadowOf(Looper.getMainLooper()).idle()
            advanceUntilIdle()

            // This is the optimisation under test: without the filter, this notification would
            // have caused an unconditional re-fetch (queryScopes + queryConfiguration +
            // queryConfigurationValues) and a new emission here.
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `change to this configuration does re-fetch and emits the new value`() = runTest(testDispatcher) {
        val configId = createConfigurationWithDefaultValue("watched_key_2", Toggle.TYPE.STRING, "initial")
        val scopeId = defaultScopeId()

        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)

        toggles.toggle("watched_key_2", "initial").test {
            advanceUntilIdle()
            assertEquals("initial", awaitItem())

            database.togglesConfigurationValueDao().updateConfigurationValue(configId, scopeId, "changed")
            context.contentResolver.notifyChange(TogglesProviderContract.configurationUri(configId), null)

            shadowOf(Looper.getMainLooper()).idle()
            advanceUntilIdle()

            assertEquals("changed", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `scope change does re-fetch even though no value changed`() = runTest(testDispatcher) {
        createConfigurationWithDefaultValue("watched_key_3", Toggle.TYPE.STRING, "same_value")

        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)

        toggles.toggle("watched_key_3", "same_value").test {
            advanceUntilIdle()
            assertEquals("same_value", awaitItem())

            // A scope switch changes what every toggle resolves to, so it must always re-fetch -
            // even though scopeUri() carries no configuration id at all to compare.
            context.contentResolver.notifyChange(TogglesProviderContract.scopeUri(), null)

            shadowOf(Looper.getMainLooper()).idle()
            advanceUntilIdle()

            // The resolved value is unchanged, but a second (equal-valued) emission proves a
            // re-fetch happened rather than being skipped.
            assertEquals("same_value", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `notification before configuration exists does re-fetch`() = runTest(testDispatcher) {
        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher, addDefaultAutomatically = false)

        toggles.toggle("late_created_key", "fallback_default").test {
            advanceUntilIdle()
            // No configuration exists yet (auto-create is disabled), so this flow has not
            // resolved a configuration id for its key.
            assertEquals("fallback_default", awaitItem())

            // A notification whose configuration id cannot be matched against anything - this
            // flow has not resolved any id yet, because its configuration does not exist. The
            // filter must not skip it: the real configuration's own creation notification would
            // look exactly like this from the observer's point of view, and skipping it would
            // mean the auto-created toggle is missed until this app restarts.
            context.contentResolver.notifyChange(TogglesProviderContract.configurationUri(999_999L), null)

            shadowOf(Looper.getMainLooper()).idle()
            advanceUntilIdle()

            // Still the fallback (nothing was actually created), but the emission proves a
            // re-fetch happened rather than being skipped.
            assertEquals("fallback_default", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `values notification for this configuration does re-fetch`() = runTest(testDispatcher) {
        val configId = createConfigurationWithDefaultValue("watched_key_5", Toggle.TYPE.STRING, "initial")
        val scopeId = defaultScopeId()

        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)

        toggles.toggle("watched_key_5", "initial").test {
            advanceUntilIdle()
            assertEquals("initial", awaitItem())

            database.togglesConfigurationValueDao()
                .updateConfigurationValue(configId, scopeId, "changed_via_value_notify")

            // Exactly the shape TogglesProvider.insert sends for a configuration value insert:
            // /configuration/{configId}/values/{valueId} - the configuration id is segment 1,
            // not the last segment ("999" here is an arbitrary value id, deliberately chosen to
            // differ from configId). Sent alone, with no accompanying toggleUri notification, so
            // this test actually exercises the trap: a naive lastPathSegment-based filter would
            // read the *value* id (999) here, compare it against configId, and wrongly skip.
            context.contentResolver.notifyChange(
                Uri.withAppendedPath(TogglesProviderContract.configurationValueUri(configId), "999"),
                null
            )

            shadowOf(Looper.getMainLooper()).idle()
            advanceUntilIdle()

            assertEquals("changed_via_value_notify", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `an unrelated toggle change costs zero additional emissions`() = runTest(testDispatcher) {
        createConfigurationWithDefaultValue("measured_key", Toggle.TYPE.STRING, "measured_default")

        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)

        val emissions = mutableListOf<String>()
        val collectJob = launch {
            toggles.toggle("measured_key", "measured_default").collect { emissions.add(it) }
        }

        advanceUntilIdle()
        assertEquals(listOf("measured_default"), emissions)

        // A single unrelated configuration change - one configuration insert plus its default
        // value insert, exactly like a fresh toggle being created in some other app.
        createConfigurationWithDefaultValue("unrelated_measured_key", Toggle.TYPE.STRING, "other_default")

        shadowOf(Looper.getMainLooper()).idle()
        advanceUntilIdle()

        // Measurement: before this change, TogglesProvider.onChange ignored the notified uri and
        // unconditionally called getToggleState (queryScopes + queryConfiguration +
        // queryConfigurationValues) for every observer of every key - so this single unrelated
        // change cost 1 emission (3 provider queries) here. With the filter, it costs 0.
        assertEquals(1, emissions.size)

        collectJob.cancel()
    }

    // endregion
}
