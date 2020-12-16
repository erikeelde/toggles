package se.eelde.toggles

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.izettle.wrench.MainActivity
import com.izettle.wrench.provider.WrenchProvider
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.provider.TogglesProvider

@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class CheckForProviders {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var rule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun checkWrenchProviderInstalled() {
        val application = ApplicationProvider.getApplicationContext<Context>()

        val providerInfo = application.packageManager.resolveContentProvider("com.izettle.wrench.configprovider", 0)
        assertNotNull(providerInfo)
        assertEquals(providerInfo!!.authority, "com.izettle.wrench.configprovider")
        assertEquals(providerInfo.name, WrenchProvider::class.java.canonicalName)
    }

    @Test
    fun checkTogglesProviderInstalled() {
        val application = ApplicationProvider.getApplicationContext<Context>()

        val providerInfo = application.packageManager.resolveContentProvider("se.eelde.toggles.configprovider", 0)
        assertNotNull(providerInfo)
        assertEquals(providerInfo!!.authority, "se.eelde.toggles.configprovider")
        assertEquals(providerInfo.name, TogglesProvider::class.java.canonicalName)
    }
}
