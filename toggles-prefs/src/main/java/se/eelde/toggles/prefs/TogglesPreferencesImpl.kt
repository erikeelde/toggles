package se.eelde.toggles.prefs

import android.content.Context
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleState

@Suppress("LibraryEntitiesShouldNotBePublic")
public class TogglesPreferencesImpl @JvmOverloads constructor(
    context: Context,
    addDefaultAutomatically: Boolean = true,
    updateDefaultAutomatically: Boolean = false,
    onMissingToggle: ((key: String, defaultValue: String, toggleState: ToggleState) -> Unit)? = null,
    onDefaultMismatch:
    ((key: String, storedDefault: String, requestedDefault: String, toggleState: ToggleState) -> Unit)? = null,
) : TogglesPreferences {
    private val provider = TogglesProvider(context)
    private val resolver = TogglesResolver(
        provider,
        addDefaultAutomatically,
        updateDefaultAutomatically,
        onMissingToggle,
        onDefaultMismatch
    )

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val toggleState = provider.getToggleState(key)
        val result = resolver.resolve(toggleState, key, Toggle.TYPE.BOOLEAN, defaultValue.toString())
        return result.toBoolean()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        val toggleState = provider.getToggleState(key)
        val result = resolver.resolve(toggleState, key, Toggle.TYPE.INTEGER, defaultValue.toString())
        return result.toInt()
    }

    override fun getString(key: String, defaultValue: String): String {
        val toggleState = provider.getToggleState(key)
        return resolver.resolve(toggleState, key, Toggle.TYPE.STRING, defaultValue)
    }

    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defaultValue: T): T {
        val toggleState = provider.getToggleState(key)
        val result = resolver.resolve(
            toggleState,
            key,
            Toggle.TYPE.ENUM,
            defaultValue.toString()
        ) { configurationId ->
            provider.insertPredefinedValues(
                configurationId,
                requireNotNull(type.enumConstants).map { it.toString() }
            )
        }
        return java.lang.Enum.valueOf(type, result)
    }

    override fun hasOverride(key: String, comparator: ScopeComparator): Boolean =
        comparator.hasOverride(provider.getToggleState(key))
}
