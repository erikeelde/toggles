package se.eelde.toggles.flow

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.core.TogglesProviderContract

/**
 * A consuming app must not crash when the Toggles provider is registered (so observers are set up)
 * but its query throws — e.g. the provider's process died, it is mid-upgrade, or, under
 * instrumentation tests, its Hilt component is torn down between tests. In that case the client
 * should fall back to the supplied default rather than propagate the exception and crash.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class ProviderThrowsTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val testDispatcher = StandardTestDispatcher()

    /** Stands in for a Toggles provider that errors on every query. */
    class ThrowingContentProvider : ContentProvider() {
        override fun onCreate(): Boolean = true
        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?,
        ): Cursor = error("provider unavailable")

        override fun getType(uri: Uri): String? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?,
        ): Int = 0

        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    }

    @Before
    fun setUp() {
        val authority = TogglesProviderContract.configurationUri().authority

        // Route resolver queries for the toggles authority to a provider that throws.
        Robolectric.buildContentProvider(ThrowingContentProvider::class.java).create(authority)

        // Register the provider with the package manager so TogglesProvider.providerAvailable
        // resolves to true and the observation path actually issues queries.
        val providerInfo = ProviderInfo().apply {
            this.authority = authority
            packageName = context.packageName
            name = ThrowingContentProvider::class.java.name
        }
        shadowOf(context.packageManager).addOrUpdateProvider(providerInfo)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `falls back to default when provider query throws`() = runTest(testDispatcher) {
        val toggles = TogglesImpl(context, ioDispatcher = testDispatcher)

        toggles.toggle("test_key", "default_value").test {
            advanceUntilIdle()
            assertEquals("default_value", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
