package se.eelde.toggles.provider

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.provider.di.ToggleTestApplication_Application
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

/**
 * Guards against lock-contention regressions in the provider's read path.
 *
 * Both tests deterministically prove that an unrelated thread holding one of the provider's global
 * monitors does not block an otherwise-independent read query. When the read path is serialised on
 * those monitors, N concurrent callers queue behind each other instead of running in parallel.
 *
 * The assertions encode the required behaviour: a held monitor must NOT block an independent read.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = ToggleTestApplication_Application::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderLockContentionTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var togglesProvider: TogglesProvider

    @Suppress("unused")
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

        // Warm up: ensure the calling application + default/development scopes already exist, so the
        // timed query below is a pure read and not the one-time initialisation path.
        togglesProvider.query(TogglesProviderContract.scopeUri(), null, null, null, null).close()
    }

    /**
     * Every query/insert/update/delete enters [TogglesProvider.query] -> getCallingApplication(),
     * which is `synchronized(this)`. Holding the provider instance monitor from another thread must
     * not stall an independent read.
     */
    @Test
    fun instanceMonitorDoesNotSerialiseReads() {
        val elapsed = timeQueryWhileMonitorHeld(monitor = togglesProvider)

        assertTrue(
            "Independent read serialised on the provider instance monitor " +
                "(blocked ${elapsed}ms while another thread held it). " +
                "getCallingApplication()'s synchronized(this) is on the hot path.",
            elapsed < HOLD_MILLIS / 2
        )
    }

    /**
     * Scope reads go through getDefaultScope()/getSelectedScope(), which are `@Synchronized` on the
     * companion object — a single static monitor shared with assertValidApiVersion(). Holding it
     * from another thread must not stall an independent read.
     */
    @Test
    fun companionMonitorDoesNotSerialiseReads() {
        val elapsed = timeQueryWhileMonitorHeld(monitor = TogglesProvider.Companion)

        assertTrue(
            "Independent read serialised on the companion-object monitor " +
                "(blocked ${elapsed}ms while another thread held it). " +
                "getDefaultScope()/getSelectedScope()/assertValidApiVersion() share one static lock.",
            elapsed < HOLD_MILLIS / 2
        )
    }

    /**
     * Holds [monitor] on a background thread for [HOLD_MILLIS], then times a scope query on the
     * current thread. If the query contends on [monitor], the elapsed time is ~[HOLD_MILLIS];
     * if it runs independently, it is small.
     */
    private fun timeQueryWhileMonitorHeld(monitor: Any): Long {
        val acquired = CountDownLatch(1)
        val holder = Thread {
            synchronized(monitor) {
                acquired.countDown()
                Thread.sleep(HOLD_MILLIS)
            }
        }
        holder.start()
        acquired.await()

        val elapsed = measureTimeMillis {
            togglesProvider.query(TogglesProviderContract.scopeUri(), null, null, null, null).close()
        }

        holder.join()
        return elapsed
    }

    private companion object {
        const val HOLD_MILLIS = 1000L
    }
}
