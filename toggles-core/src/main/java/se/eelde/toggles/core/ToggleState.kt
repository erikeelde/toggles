package se.eelde.toggles.core

@Suppress("LibraryEntitiesShouldNotBePublic")
public class ToggleState(
    public val configuration: TogglesConfiguration?,
    public val configurationValues: List<TogglesConfigurationValue>,
    public val scopes: List<ToggleScope>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToggleState

        if (configuration != other.configuration) return false
        if (configurationValues != other.configurationValues) return false
        if (scopes != other.scopes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = configuration?.hashCode() ?: 0
        result = 31 * result + configurationValues.hashCode()
        result = 31 * result + scopes.hashCode()
        return result
    }

    override fun toString(): String {
        return "ToggleState(configuration=$configuration, configurationValues=$configurationValues, scopes=$scopes)"
    }
}
