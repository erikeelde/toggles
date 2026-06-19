package se.eelde.toggles.provider

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
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

/**
 * Guards the double-checked first-touch initialisation in getCallingApplication(): when many callers
 * hit a never-seen calling app concurrently, exactly one application row and exactly one pair of
 * scopes (default + development) must be created — no duplicates.
 *
 * Deliberately performs NO warm-up query in setUp(), so the concurrent queries below ARE the
 * first interaction with the provider.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = ToggleTestApplication_Application::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderConcurrentInitTest {
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
    }

    @Test
    fun concurrentFirstTouchCreatesScopesExactlyOnce() {
        val threadCount = 16
        val startGate = CountDownLatch(1)
        val ready = CountDownLatch(threadCount)
        val threads = (0 until threadCount).map {
            Thread {
                ready.countDown()
                startGate.await()
                togglesProvider
                    .query(TogglesProviderContract.scopeUri(), null, null, null, null)
                    .close()
            }
        }

        threads.forEach { it.start() }
        ready.await()
        startGate.countDown() // release all callers at once to maximise the first-touch race
        threads.forEach { it.join() }

        togglesProvider.query(TogglesProviderContract.scopeUri(), null, null, null, null)
            .use { cursor ->
                assertEquals(
                    "Concurrent first touch must create exactly the default + development scopes",
                    2,
                    cursor.count
                )
            }
    }
}
