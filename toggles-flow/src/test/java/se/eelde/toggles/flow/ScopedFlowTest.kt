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
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.provider.RobolectricTogglesProvider
import se.eelde.toggles.provider.TogglesProvider
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class ScopedFlowTest {
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
    fun testScopedToggleAccess() = runTest {
        // Create a custom scope for user1
        val user1Scope = TogglesScope(
            id = 0,
            applicationId = 1,
            name = "user1",
            timeStamp = Date()
        )
        user1Scope.id = database.togglesScopeDao().insert(user1Scope)

        // Create a custom scope for user2
        val user2Scope = TogglesScope(
            id = 0,
            applicationId = 1,
            name = "user2",
            timeStamp = Date()
        )
        user2Scope.id = database.togglesScopeDao().insert(user2Scope)

        // Set up a toggle with different values for each scope
        val configId = database.togglesConfigurationDao().insert(
            se.eelde.toggles.database.TogglesConfiguration(
                id = 0,
                applicationId = 1,
                key = "feature_flag",
                type = "boolean"
            )
        )

        // Set value for user1 scope to "true"
        database.togglesConfigurationValueDao().insert(
            se.eelde.toggles.database.TogglesConfigurationValue(
                id = 0,
                configurationId = configId,
                value = "true",
                scope = user1Scope.id
            )
        )

        // Set value for user2 scope to "false"
        database.togglesConfigurationValueDao().insert(
            se.eelde.toggles.database.TogglesConfigurationValue(
                id = 0,
                configurationId = configId,
                value = "false",
                scope = user2Scope.id
            )
        )

        // Test that user1 scope gets "true"
        val togglesUser1 = TogglesImpl(context, scope = "user1")
        togglesUser1.toggle("feature_flag", false).first().apply {
            @OptIn(ExperimentalCoroutinesApi::class)
            advanceUntilIdle()
            assert(this == true) { "Expected true for user1 scope, got $this" }
        }

        // Test that user2 scope gets "false"
        val togglesUser2 = TogglesImpl(context, scope = "user2")
        togglesUser2.toggle("feature_flag", false).first().apply {
            @OptIn(ExperimentalCoroutinesApi::class)
            advanceUntilIdle()
            assert(this == false) { "Expected false for user2 scope, got $this" }
        }
    }

    @Test
    fun testDefaultScopeWhenScopeNotSpecified() = runTest {
        // Without specifying scope, should use the selected scope
        val toggles = TogglesImpl(context)
        toggles.toggle("test_flag", true).first().apply {
            @OptIn(ExperimentalCoroutinesApi::class)
            advanceUntilIdle()
            assert(this == true) { "Expected default value when toggle not set" }
        }
    }

    @Test
    fun testFallbackToDefaultScopeWhenCustomScopeNotFound() = runTest {
        // Create a toggle value in the default scope
        val configId = database.togglesConfigurationDao().insert(
            se.eelde.toggles.database.TogglesConfiguration(
                id = 0,
                applicationId = 1,
                key = "fallback_flag",
                type = "string"
            )
        )

        val defaultScope = database.togglesScopeDao().getDefaultScope(1)
        database.togglesConfigurationValueDao().insert(
            se.eelde.toggles.database.TogglesConfigurationValue(
                id = 0,
                configurationId = configId,
                value = "default_value",
                scope = defaultScope!!.id
            )
        )

        // Request with non-existent scope should fall back to default
        val toggles = TogglesImpl(context, scope = "nonexistent_scope")
        toggles.toggle("fallback_flag", "fallback").first().apply {
            @OptIn(ExperimentalCoroutinesApi::class)
            advanceUntilIdle()
            assert(this == "default_value") { "Expected default scope value, got $this" }
        }
    }
}
