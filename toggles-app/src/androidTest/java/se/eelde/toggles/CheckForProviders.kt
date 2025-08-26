package se.eelde.toggles

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.eelde.toggles.provider.TogglesProvider

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CheckForProviders {

    private lateinit var context: Context

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()

        scenario = launchActivity()
    }

    @Test
    fun checkTogglesProviderInstalled() {
        val providerInfo =
            context.packageManager.resolveContentProvider(
                "se.eelde.toggles.configprovider",
                0 // PackageManager.ComponentInfoFlags.of(0)
            )
        assertNotNull(providerInfo)
        assertEquals(providerInfo!!.authority, "se.eelde.toggles.configprovider")
        assertEquals(providerInfo.name, TogglesProvider::class.java.canonicalName)
    }
}
