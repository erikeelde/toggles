package se.eelde.toggles.applications

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ExampleSharedTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun useAppContext() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        assertTrue(
            "Package name missmatch ${context.packageName}",
            context.packageName.startsWith("se.eelde.toggles.example")
        )
    }
}
