package se.eelde.toggles.flow

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.provider.RobolectricTogglesProvider
import se.eelde.toggles.provider.TogglesProvider

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class FlowTest {
    val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var togglesProvider: TogglesProvider

    @Before
    fun setUp() {
        togglesProvider = RobolectricTogglesProvider.create(context)
    }

    @Test
    fun test() = runTest {
        val toggles = TogglesImpl(context)
        toggles.toggle("test item", "my value").test {
            val toggleValue = awaitItem()
            assert(toggleValue == "my value")
        }
    }
}
