package se.eelde.toggles.flow

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
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
    val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var togglesProvider: TogglesProvider
    private lateinit var database: TogglesDatabase

    @Before
    fun setUp() {
        database = FakeTogglesDatabase.create(context)
        togglesProvider = RobolectricTogglesProvider.create(
            context,
            database,
            FakeToggles(),
        )
    }

    @Test
    fun test() = runTest {
        val toggles = TogglesImpl(context)
        toggles.toggle("test item", "my value").first().apply {
            assert(this == "my value")
        }

        database.togglesConfigurationValueDao()
            .updateConfigurationValue(1, 1, "mah digga")

        toggles.toggle("test item", "my value").first().apply {
            assert(this == "mah digga")
        }
    }
}
