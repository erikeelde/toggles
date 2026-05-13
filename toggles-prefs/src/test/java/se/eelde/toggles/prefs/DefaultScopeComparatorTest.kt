package se.eelde.toggles.prefs

import android.annotation.SuppressLint
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.ToggleScope
import se.eelde.toggles.core.ToggleState
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
@SuppressLint("DenyListedApi")
internal class DefaultScopeComparatorTest {

    private val defaultScope = ToggleScope {
        id = 1L
        name = ColumnNames.ToggleScope.DEFAULT_SCOPE
        timeStamp = Date(1000)
    }

    private val nonDefaultScope = ToggleScope {
        id = 2L
        name = "development"
        timeStamp = Date(2000)
    }

    private val configuration = TogglesConfiguration {
        id = 1L
        type = "BOOLEAN"
        key = "feature_x"
    }

    @Test
    fun `returns false when scopes list is empty`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = emptyList(),
            scopes = emptyList()
        )
        assertFalse(DefaultScopeComparator.hasOverride(state))
    }

    @Test
    fun `returns false when only default scope exists`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = listOf(
                TogglesConfigurationValue {
                    id = 1L
                    configurationId = 1L
                    value = "true"
                    scope = defaultScope.id
                }
            ),
            scopes = listOf(defaultScope)
        )
        assertFalse(DefaultScopeComparator.hasOverride(state))
    }

    @Test
    fun `returns false when non-default scope selected but has no value`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = listOf(
                TogglesConfigurationValue {
                    id = 1L
                    configurationId = 1L
                    value = "true"
                    scope = defaultScope.id
                }
            ),
            scopes = listOf(defaultScope, nonDefaultScope)
        )
        assertFalse(DefaultScopeComparator.hasOverride(state))
    }

    @Test
    fun `returns true when non-default scope is selected and has a value`() {
        val state = ToggleState(
            configuration = configuration,
            configurationValues = listOf(
                TogglesConfigurationValue {
                    id = 1L
                    configurationId = 1L
                    value = "true"
                    scope = defaultScope.id
                },
                TogglesConfigurationValue {
                    id = 2L
                    configurationId = 1L
                    value = "false"
                    scope = nonDefaultScope.id
                }
            ),
            scopes = listOf(defaultScope, nonDefaultScope)
        )
        assertTrue(DefaultScopeComparator.hasOverride(state))
    }
}
