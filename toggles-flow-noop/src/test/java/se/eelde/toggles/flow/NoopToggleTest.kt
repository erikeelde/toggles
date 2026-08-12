package se.eelde.toggles.flow

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class NoopToggleTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `hasOverride always returns false`() = runTest {
        val toggles = TogglesImpl(context)
        assertFalse(toggles.hasOverride("any-key").first())
    }

    @Test
    fun `hasOverride never invokes comparator`() = runTest {
        var invoked = false
        val capturingComparator = ScopeComparator { _ ->
            invoked = true
            false
        }
        TogglesImpl(context).hasOverride("any-key", capturingComparator).first()
        assertFalse("comparator should never be called in noop implementation", invoked)
    }
}
