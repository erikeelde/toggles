package se.eelde.toggles.prefs

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleScope
import se.eelde.toggles.core.ToggleState
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@Suppress("LargeClass")
@SuppressLint("DenyListedApi")
internal class TogglesResolverTest {

    private lateinit var provider: TogglesProvider

    private val defaultScope = ToggleScope.Builder()
        .setId(1L)
        .setName("toggles_default")
        .setTimeStamp(Date(1000L))
        .build()

    private val developmentScope = ToggleScope.Builder()
        .setId(2L)
        .setName("development")
        .setTimeStamp(Date(500L))
        .build()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        provider = TogglesProvider(context)
    }

    // region Empty scopes - returns default regardless of settings

    @Test
    fun `resolve returns default when scopes are empty`() {
        val resolver = createResolver()
        val state = ToggleState(null, emptyList(), emptyList())

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "default")

        assertEquals("default", result)
    }

    @Test
    fun `resolve returns default when scopes are empty even with configuration present`() {
        val resolver = createResolver()
        val config = buildConfiguration(id = 1L, key = "key")
        val state = ToggleState(config, emptyList(), emptyList())

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "default")

        assertEquals("default", result)
    }

    // endregion

    // region Missing configuration - addDefaultAutomatically=true (default)

    @Test
    fun `resolve returns default when configuration is missing and addDefaultAutomatically is true`() {
        val resolver = createResolver(addDefaultAutomatically = true)
        val state = ToggleState(null, emptyList(), listOf(defaultScope))

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "default")

        assertEquals("default", result)
    }

    @Test
    fun `resolve does not invoke onMissingToggle when addDefaultAutomatically is true`() {
        var missingToggleCalled = false
        val resolver = createResolver(
            addDefaultAutomatically = true,
            onMissingToggle = { _, _, _ -> missingToggleCalled = true }
        )
        val state = ToggleState(null, emptyList(), listOf(defaultScope))

        resolver.resolve(state, "key", Toggle.TYPE.STRING, "default")

        assertEquals(false, missingToggleCalled)
    }

    // endregion

    // region Missing configuration - addDefaultAutomatically=false

    @Test
    fun `resolve returns default when configuration is missing and addDefaultAutomatically is false`() {
        val resolver = createResolver(addDefaultAutomatically = false)
        val state = ToggleState(null, emptyList(), listOf(defaultScope))

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "default")

        assertEquals("default", result)
    }

    @Test
    fun `resolve invokes onMissingToggle when addDefaultAutomatically is false`() {
        var capturedKey: String? = null
        var capturedDefault: String? = null
        var capturedState: ToggleState? = null
        val resolver = createResolver(
            addDefaultAutomatically = false,
            onMissingToggle = { key, defaultValue, toggleState ->
                capturedKey = key
                capturedDefault = defaultValue
                capturedState = toggleState
            }
        )
        val state = ToggleState(null, emptyList(), listOf(defaultScope))

        resolver.resolve(state, "my_key", Toggle.TYPE.BOOLEAN, "true")

        assertEquals("my_key", capturedKey)
        assertEquals("true", capturedDefault)
        assertEquals(state, capturedState)
    }

    @Test
    fun `resolve does not invoke onMissingToggle when it is null and addDefaultAutomatically is false`() {
        val resolver = createResolver(addDefaultAutomatically = false, onMissingToggle = null)
        val state = ToggleState(null, emptyList(), listOf(defaultScope))

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "default")

        assertEquals("default", result)
    }

    // endregion

    // region Scope resolution - selected scope has override

    @Test
    fun `resolve returns selected scope value when override exists`() {
        val resolver = createResolver()
        val selectedScope = developmentScope.copy(timeStamp = Date(2000L))
        val config = buildConfiguration(id = 10L, key = "feature_flag")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "false",
            scopeId = defaultScope.id
        )
        val overrideValue = buildConfigurationValue(
            id = 2L,
            configId = 10L,
            value = "true",
            scopeId = selectedScope.id
        )
        val state = ToggleState(
            config,
            listOf(defaultValue, overrideValue),
            listOf(defaultScope, selectedScope)
        )

        val result = resolver.resolve(state, "feature_flag", Toggle.TYPE.BOOLEAN, "false")

        assertEquals("true", result)
    }

    @Test
    fun `resolve returns default scope value when no selected scope override`() {
        val resolver = createResolver()
        val config = buildConfiguration(id = 10L, key = "feature_flag")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "stored_value",
            scopeId = defaultScope.id
        )
        val state = ToggleState(
            config,
            listOf(defaultValue),
            listOf(defaultScope, developmentScope)
        )

        val result = resolver.resolve(state, "feature_flag", Toggle.TYPE.STRING, "fallback")

        assertEquals("stored_value", result)
    }

    @Test
    fun `resolve returns parameter default when no configuration values exist for any scope`() {
        val resolver = createResolver()
        val config = buildConfiguration(id = 10L, key = "feature_flag")
        val state = ToggleState(
            config,
            emptyList(),
            listOf(defaultScope, developmentScope)
        )

        val result = resolver.resolve(state, "feature_flag", Toggle.TYPE.STRING, "param_default")

        assertEquals("param_default", result)
    }

    @Test
    fun `resolve uses scope with highest timestamp as selected`() {
        val resolver = createResolver()
        val recentScope = developmentScope.copy(timeStamp = Date(9999L))
        val config = buildConfiguration(id = 10L, key = "key")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "default_val",
            scopeId = defaultScope.id
        )
        val devValue = buildConfigurationValue(
            id = 2L,
            configId = 10L,
            value = "dev_val",
            scopeId = recentScope.id
        )
        val state = ToggleState(
            config,
            listOf(defaultValue, devValue),
            listOf(defaultScope, recentScope)
        )

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "fallback")

        assertEquals("dev_val", result)
    }

    @Test
    fun `resolve returns default value when selected scope value is null`() {
        val resolver = createResolver()
        val selectedScope = developmentScope.copy(timeStamp = Date(2000L))
        val config = buildConfiguration(id = 10L, key = "key")
        val defaultConfigValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "stored",
            scopeId = defaultScope.id
        )
        val nullScopeValue = TogglesConfigurationValue.Builder()
            .setId(2L)
            .setConfigurationId(10L)
            .setScope(selectedScope.id)
            .build()
        val state = ToggleState(
            config,
            listOf(defaultConfigValue, nullScopeValue),
            listOf(defaultScope, selectedScope)
        )

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "param_default")

        assertEquals("param_default", result)
    }

    // endregion

    // region Default mismatch - updateDefaultAutomatically=false (default)

    @Test
    fun `resolve invokes onDefaultMismatch when stored default differs from requested`() {
        var capturedKey: String? = null
        var capturedStoredDefault: String? = null
        var capturedRequestedDefault: String? = null
        val resolver = createResolver(
            updateDefaultAutomatically = false,
            onDefaultMismatch = { key, storedDefault, requestedDefault, _ ->
                capturedKey = key
                capturedStoredDefault = storedDefault
                capturedRequestedDefault = requestedDefault
            }
        )
        val config = buildConfiguration(id = 10L, key = "key")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "old_default",
            scopeId = defaultScope.id
        )
        val state = ToggleState(
            config,
            listOf(defaultValue),
            listOf(defaultScope)
        )

        resolver.resolve(state, "key", Toggle.TYPE.STRING, "new_default")

        assertEquals("key", capturedKey)
        assertEquals("old_default", capturedStoredDefault)
        assertEquals("new_default", capturedRequestedDefault)
    }

    @Test
    fun `resolve does not invoke onDefaultMismatch when stored matches requested`() {
        var mismatchCalled = false
        val resolver = createResolver(
            updateDefaultAutomatically = false,
            onDefaultMismatch = { _, _, _, _ -> mismatchCalled = true }
        )
        val config = buildConfiguration(id = 10L, key = "key")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "same_value",
            scopeId = defaultScope.id
        )
        val state = ToggleState(
            config,
            listOf(defaultValue),
            listOf(defaultScope)
        )

        resolver.resolve(state, "key", Toggle.TYPE.STRING, "same_value")

        assertEquals(false, mismatchCalled)
    }

    @Test
    fun `resolve does not invoke onDefaultMismatch when updateDefaultAutomatically is true`() {
        var mismatchCalled = false
        val resolver = createResolver(
            updateDefaultAutomatically = true,
            onDefaultMismatch = { _, _, _, _ -> mismatchCalled = true }
        )
        val config = buildConfiguration(id = 10L, key = "key")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "old_default",
            scopeId = defaultScope.id
        )
        val state = ToggleState(
            config,
            listOf(defaultValue),
            listOf(defaultScope)
        )

        resolver.resolve(state, "key", Toggle.TYPE.STRING, "new_default")

        assertEquals(false, mismatchCalled)
    }

    // endregion

    // region Default mismatch with null stored value

    @Test
    fun `resolve invokes onDefaultMismatch when stored default is null`() {
        var capturedStoredDefault: String? = null
        var capturedRequestedDefault: String? = null
        val resolver = createResolver(
            updateDefaultAutomatically = false,
            onDefaultMismatch = { _, storedDefault, requestedDefault, _ ->
                capturedStoredDefault = storedDefault
                capturedRequestedDefault = requestedDefault
            }
        )
        val config = buildConfiguration(id = 10L, key = "key")
        val nullDefaultValue = TogglesConfigurationValue.Builder()
            .setId(1L)
            .setConfigurationId(10L)
            .setScope(defaultScope.id)
            .build()
        val state = ToggleState(
            config,
            listOf(nullDefaultValue),
            listOf(defaultScope)
        )

        resolver.resolve(state, "key", Toggle.TYPE.STRING, "requested_default")

        assertEquals("", capturedStoredDefault)
        assertEquals("requested_default", capturedRequestedDefault)
    }

    // endregion

    // region Missing default configuration value with existing configuration

    @Test
    fun `resolve auto-creates default value when configuration exists but default value missing`() {
        val resolver = createResolver(addDefaultAutomatically = true)
        val config = buildConfiguration(id = 10L, key = "key")
        val state = ToggleState(
            config,
            emptyList(),
            listOf(defaultScope)
        )

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "my_default")

        assertEquals("my_default", result)
    }

    @Test
    fun `resolve does not auto-create default value when addDefaultAutomatically is false`() {
        val resolver = createResolver(addDefaultAutomatically = false)
        val config = buildConfiguration(id = 10L, key = "key")
        val state = ToggleState(
            config,
            emptyList(),
            listOf(defaultScope)
        )

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "my_default")

        assertEquals("my_default", result)
    }

    // endregion

    // region Return value with various toggle types

    @Test
    fun `resolve returns boolean string value`() {
        val resolver = createResolver()
        val config = buildConfiguration(id = 10L, key = "feature_enabled")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "true",
            scopeId = defaultScope.id
        )
        val state = ToggleState(config, listOf(defaultValue), listOf(defaultScope))

        val result = resolver.resolve(state, "feature_enabled", Toggle.TYPE.BOOLEAN, "false")

        assertEquals("true", result)
    }

    @Test
    fun `resolve returns integer string value`() {
        val resolver = createResolver()
        val config = buildConfiguration(id = 10L, key = "max_retries")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "5",
            scopeId = defaultScope.id
        )
        val state = ToggleState(config, listOf(defaultValue), listOf(defaultScope))

        val result = resolver.resolve(state, "max_retries", Toggle.TYPE.INTEGER, "3")

        assertEquals("5", result)
    }

    // endregion

    // region Multiple scopes with single default scope

    @Test
    fun `resolve falls back to default scope when only default scope exists`() {
        val resolver = createResolver()
        val config = buildConfiguration(id = 10L, key = "key")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "stored",
            scopeId = defaultScope.id
        )
        val state = ToggleState(
            config,
            listOf(defaultValue),
            listOf(defaultScope)
        )

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "fallback")

        assertEquals("stored", result)
    }

    // endregion

    // region Combination: mismatch detection + scope override

    @Test
    fun `resolve detects mismatch and still returns selected scope override`() {
        var mismatchCalled = false
        val selectedScope = developmentScope.copy(timeStamp = Date(2000L))
        val resolver = createResolver(
            updateDefaultAutomatically = false,
            onDefaultMismatch = { _, _, _, _ -> mismatchCalled = true }
        )
        val config = buildConfiguration(id = 10L, key = "key")
        val defaultValue = buildConfigurationValue(
            id = 1L,
            configId = 10L,
            value = "old_default",
            scopeId = defaultScope.id
        )
        val overrideValue = buildConfigurationValue(
            id = 2L,
            configId = 10L,
            value = "override",
            scopeId = selectedScope.id
        )
        val state = ToggleState(
            config,
            listOf(defaultValue, overrideValue),
            listOf(defaultScope, selectedScope)
        )

        val result = resolver.resolve(state, "key", Toggle.TYPE.STRING, "new_default")

        assertEquals(true, mismatchCalled)
        assertEquals("override", result)
    }

    // endregion

    // region Helper methods

    private fun createResolver(
        addDefaultAutomatically: Boolean = true,
        updateDefaultAutomatically: Boolean = false,
        onMissingToggle: ((String, String, ToggleState) -> Unit)? = null,
        onDefaultMismatch: ((String, String, String, ToggleState) -> Unit)? = null,
    ): TogglesResolver = TogglesResolver(
        provider = provider,
        addDefaultAutomatically = addDefaultAutomatically,
        updateDefaultAutomatically = updateDefaultAutomatically,
        onMissingToggle = onMissingToggle,
        onDefaultMismatch = onDefaultMismatch,
    )

    private fun buildConfiguration(id: Long, key: String, type: String = Toggle.TYPE.STRING): TogglesConfiguration =
        TogglesConfiguration.Builder()
            .setId(id)
            .setKey(key)
            .setType(type)
            .build()

    private fun buildConfigurationValue(
        id: Long,
        configId: Long,
        value: String,
        scopeId: Long,
    ): TogglesConfigurationValue =
        TogglesConfigurationValue.Builder()
            .setId(id)
            .setConfigurationId(configId)
            .setValue(value)
            .setScope(scopeId)
            .build()

    // endregion
}
