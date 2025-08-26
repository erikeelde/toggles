package se.eelde.toggles

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentationTest2 {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun useAppContext() = runTest {
        launchActivity<MainActivity>().use {
            composeTestRule.onNodeWithText("No applications found.").assertExists()
        }
    }
}
