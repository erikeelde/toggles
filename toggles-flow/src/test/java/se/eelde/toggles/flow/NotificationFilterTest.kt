package se.eelde.toggles.flow

import android.net.Uri
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.core.TogglesProviderContract

/**
 * Direct unit tests for [configurationIdFromNotificationUri] and [shouldRefetchOnChange], the
 * pure functions backing the conservative change filter in
 * [TogglesProvider.observeToggleState]'s shared `ContentObserver`.
 *
 * [ToggleUriNotificationTest] covers the same rules end-to-end against a real Robolectric
 * provider; these tests isolate the URI-parsing/filtering logic itself so each rule (and the
 * multi-shape URI trap in particular) has a single, deterministic assertion.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class NotificationFilterTest {

    // region configurationIdFromNotificationUri

    @Test
    fun `configuration id is the last segment for configurationUri`() {
        assertEquals(42L, configurationIdFromNotificationUri(TogglesProviderContract.configurationUri(42L)))
    }

    @Test
    fun `configuration id is the last segment for the legacy toggleUri`() {
        assertEquals(7L, configurationIdFromNotificationUri(TogglesProviderContract.toggleUri(7L)))
    }

    @Test
    fun `configuration id is segment 1, not the last segment, for a values-insert notification`() {
        // The trap: TogglesProvider.insert notifies Uri.withAppendedPath(configurationValueUri(id), valueId),
        // i.e. /configuration/{configId}/values/{valueId}. The last segment is the *value* id.
        val valuesInsertNotificationUri = Uri.withAppendedPath(
            TogglesProviderContract.configurationValueUri(5L),
            "999"
        )
        assertEquals(5L, configurationIdFromNotificationUri(valuesInsertNotificationUri))
    }

    @Test
    fun `scopeUri carries no configuration id`() {
        assertNull(configurationIdFromNotificationUri(TogglesProviderContract.scopeUri()))
    }

    // endregion

    // region shouldRefetchOnChange

    @Test
    fun `null uri always refetches`() {
        assertTrue(shouldRefetchOnChange(uri = null, resolvedConfigurationId = 1L))
    }

    @Test
    fun `a uri with no configuration id always refetches`() {
        assertTrue(
            shouldRefetchOnChange(TogglesProviderContract.scopeUri(), resolvedConfigurationId = 1L)
        )
    }

    @Test
    fun `an unresolved configuration id always refetches`() {
        assertTrue(
            shouldRefetchOnChange(
                TogglesProviderContract.configurationUri(999L),
                resolvedConfigurationId = null
            )
        )
    }

    @Test
    fun `a matching configuration id refetches`() {
        assertTrue(
            shouldRefetchOnChange(TogglesProviderContract.configurationUri(1L), resolvedConfigurationId = 1L)
        )
    }

    @Test
    fun `a different configuration id skips`() {
        assertFalse(
            shouldRefetchOnChange(TogglesProviderContract.configurationUri(2L), resolvedConfigurationId = 1L)
        )
    }

    @Test
    fun `a different configuration id via a values-insert notification skips`() {
        val valuesInsertNotificationUri = Uri.withAppendedPath(
            TogglesProviderContract.configurationValueUri(2L),
            "999"
        )
        assertFalse(shouldRefetchOnChange(valuesInsertNotificationUri, resolvedConfigurationId = 1L))
    }

    @Test
    fun `a matching configuration id via a values-insert notification refetches`() {
        // This is the trap: the last segment (999) is a value id, not the configuration id (5).
        // A naive lastPathSegment-based filter would compare 999 against 5, see a mismatch, and
        // wrongly skip a notification that is actually about the resolved configuration.
        val valuesInsertNotificationUri = Uri.withAppendedPath(
            TogglesProviderContract.configurationValueUri(5L),
            "999"
        )
        assertTrue(shouldRefetchOnChange(valuesInsertNotificationUri, resolvedConfigurationId = 5L))
    }

    // endregion
}
