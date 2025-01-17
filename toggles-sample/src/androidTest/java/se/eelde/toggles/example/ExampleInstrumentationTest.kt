package se.eelde.toggles.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentationTest {
    @Test
    fun useAppContext() {
        val appContext = ApplicationProvider.getApplicationContext<Application>()
        assertEquals("se.eelde.toggles.example.debug", appContext.packageName)
    }
}
