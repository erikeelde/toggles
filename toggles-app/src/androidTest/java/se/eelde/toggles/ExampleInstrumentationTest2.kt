package se.eelde.toggles

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Suppress("DEPRECATION")
class ExampleInstrumentationTest2 {

    // HiltAndroidRule must run before the activity is launched, so order it first.
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun useAppContext() = runTest {
        launchActivity<MainActivity>().use {
            // The app launches on the Applications home. (The previous empty-state
            // assertion was racy: reading the dogfood toggle on launch self-registers
            // this app, so "No applications found." is no longer reliably shown.)
            composeTestRule.onAllNodesWithText("Applications").onFirst().assertExists()
        }
    }
}
