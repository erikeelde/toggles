package se.eelde.toggles

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.izettle.wrench.MainActivity
import com.izettle.wrench.provider.WrenchProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.provider.TogglesProvider

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
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

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        scenario = launchActivity()
    }

    @Test
    fun checkWrenchProviderInstalled() {
        val providerInfo = context.packageManager.resolveContentProvider("com.izettle.wrench.configprovider", 0)
        assertNotNull(providerInfo)
        assertEquals(providerInfo!!.authority, "com.izettle.wrench.configprovider")
        assertEquals(providerInfo.name, WrenchProvider::class.java.canonicalName)
    }

    @Test
    fun checkTogglesProviderInstalled() {
        val providerInfo = context.packageManager.resolveContentProvider("se.eelde.toggles.configprovider", 0)
        assertNotNull(providerInfo)
        assertEquals(providerInfo!!.authority, "se.eelde.toggles.configprovider")
        assertEquals(providerInfo.name, TogglesProvider::class.java.canonicalName)
    }
}
