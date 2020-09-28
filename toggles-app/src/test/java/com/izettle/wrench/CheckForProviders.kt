package com.izettle.wrench

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.izettle.wrench.provider.WrenchProvider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.eelde.toggles.TogglesApplication
import se.eelde.toggles.provider.TogglesProvider

@RunWith(AndroidJUnit4::class)
class CheckForProviders {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun checkWrenchProviderInstalled() {
        val application = ApplicationProvider.getApplicationContext<TogglesApplication>()

        val providerInfo = application.packageManager.resolveContentProvider("com.izettle.wrench.configprovider", 0)
        assertNotNull(providerInfo)
        assertEquals(providerInfo!!.authority, "com.izettle.wrench.configprovider")
        assertEquals(providerInfo.name, WrenchProvider::class.java.canonicalName)
    }

    @Test
    fun checkTogglesProviderInstalled() {
        val application = ApplicationProvider.getApplicationContext<TogglesApplication>()

        val providerInfo = application.packageManager.resolveContentProvider("se.eelde.toggles.configprovider", 0)
        assertNotNull(providerInfo)
        assertEquals(providerInfo!!.authority, "se.eelde.toggles.configprovider")
        assertEquals(providerInfo.name, TogglesProvider::class.java.canonicalName)
    }
}