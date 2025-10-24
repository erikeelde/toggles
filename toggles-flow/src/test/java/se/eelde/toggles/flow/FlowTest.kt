package se.eelde.toggles.flow

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.database.FakeTogglesDatabase
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.provider.RobolectricTogglesProvider
import se.eelde.toggles.provider.TogglesProvider

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class FlowTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var togglesProvider: TogglesProvider
    private lateinit var database: TogglesDatabase

    @Before
    fun setUp() {
        database = FakeTogglesDatabase.create(context)
        val standardTestDispatcher = StandardTestDispatcher()
        togglesProvider = RobolectricTogglesProvider.create(
            context = context,
            database = database,
            toggles = FakeToggles(),
            ioDispatcher = standardTestDispatcher,
        )
    }

    @Test
    fun test() = runTest {
        val toggles = TogglesImpl(context)
        toggles.toggle("test item", "my value").first().apply {
            @OptIn(ExperimentalCoroutinesApi::class)
            advanceUntilIdle()
            assert(this == "my value")
        }

        database.togglesConfigurationValueDao()
            .updateConfigurationValue(1, 1, "the test configuration value")

        toggles.toggle("test item", "my value").first().apply {
            @OptIn(ExperimentalCoroutinesApi::class)
            advanceUntilIdle()
            assert(this == "the test configuration value")
        }
    }
}
