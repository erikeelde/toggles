package se.eelde.toggles.applications

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ./gradlew :modules:applications:pixel2api30DebugAndroidTest -Pshared-tests-are-android-tests=false
 * ./gradlew :modules:applications:pixel2api30DebugAndroidTest -Pshared-tests-are-android-tests=true
 */
@RunWith(AndroidJUnit4::class)
class ExampleSharedTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun useAppContext() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        assertEquals("se.eelde.toggles.applications.test", context.packageName)
    }
}
