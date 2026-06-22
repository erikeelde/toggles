package se.eelde.toggles.prefs

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class NoopTogglesPreferencesTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `hasOverride always returns false`() {
        val prefs = TogglesPreferencesImpl(context)
        assertFalse(prefs.hasOverride("any-key"))
    }

    @Test
    fun `hasOverride never invokes comparator`() {
        var invoked = false
        val capturingComparator = ScopeComparator { _ ->
            invoked = true
            false
        }
        TogglesPreferencesImpl(context).hasOverride("any-key", capturingComparator)
        assertFalse("comparator should never be called in noop implementation", invoked)
    }
}
