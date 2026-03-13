package se.eelde.toggles.prefs

import se.eelde.toggles.core.ToggleState

internal class TogglesResolver(
    private val provider: TogglesProvider,
    private val addDefaultAutomatically: Boolean = true,
    private val updateDefaultAutomatically: Boolean = false,
    private val onMissingToggle: ((key: String, defaultValue: String, toggleState: ToggleState) -> Unit)? = null,
    private val onDefaultMismatch: (
        (key: String, storedDefault: String, requestedDefault: String, toggleState: ToggleState) -> Unit
    )? = null,
) {
    @Suppress("ReturnCount")
    fun resolve(
        toggleState: ToggleState,
        key: String,
        type: String,
        defaultValue: String,
        onFirstCreate: ((configurationId: Long) -> Unit)? = null,
    ): String {
        if (toggleState.scopes.isEmpty()) {
            return defaultValue
        }

        val defaultScope = provider.getDefaultScope(toggleState.scopes)
            ?: error("Default scope not found")

        if (toggleState.configuration == null) {
            return handleMissingConfiguration(
                key,
                type,
                defaultValue,
                defaultScope.id,
                toggleState,
                onFirstCreate
            )
        }

        val selectedScope = provider.getSelectedScope(toggleState.scopes)
            ?: error("Selected scope not found")

        val defaultConfigValue = provider.getConfigurationValueForScope(
            defaultScope.id,
            toggleState.configurationValues
        )

        handleDefaultValue(key, defaultValue, defaultConfigValue, toggleState)

        val selectedConfigValue = provider.getConfigurationValueForScope(
            selectedScope.id,
            toggleState.configurationValues
        )

        if (selectedConfigValue != null) {
            return selectedConfigValue.value ?: defaultValue
        }

        return defaultConfigValue?.value ?: defaultValue
    }

    @Suppress("LongParameterList")
    private fun handleMissingConfiguration(
        key: String,
        type: String,
        defaultValue: String,
        defaultScopeId: Long,
        toggleState: ToggleState,
        onFirstCreate: ((configurationId: Long) -> Unit)?,
    ): String {
        if (addDefaultAutomatically) {
            val configId = provider.insertConfiguration(key, type)
                ?: return defaultValue
            provider.insertConfigurationValue(configId, defaultValue, defaultScopeId)
            onFirstCreate?.invoke(configId)
        } else {
            onMissingToggle?.invoke(key, defaultValue, toggleState)
        }
        return defaultValue
    }

    private fun handleDefaultValue(
        key: String,
        defaultValue: String,
        defaultConfigValue: se.eelde.toggles.core.TogglesConfigurationValue?,
        toggleState: ToggleState,
    ) {
        val configuration = toggleState.configuration ?: return

        if (defaultConfigValue == null) {
            val defaultScope = provider.getDefaultScope(toggleState.scopes) ?: return
            if (addDefaultAutomatically) {
                provider.insertConfigurationValue(
                    configuration.id,
                    defaultValue,
                    defaultScope.id
                )
            }
        } else if (defaultConfigValue.value != defaultValue) {
            if (updateDefaultAutomatically) {
                provider.updateConfigurationValue(
                    configuration.id,
                    defaultConfigValue,
                    defaultValue
                )
            } else {
                onDefaultMismatch?.invoke(
                    key,
                    defaultConfigValue.value ?: "",
                    defaultValue,
                    toggleState
                )
            }
        }
    }
}
