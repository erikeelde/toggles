package se.eelde.toggles.flow

import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.core.ToggleState

@Suppress("LibraryEntitiesShouldNotBePublic")
public fun interface ScopeComparator {
    public fun hasOverride(state: ToggleState): Boolean
}

@Suppress("LibraryEntitiesShouldNotBePublic")
public object DefaultScopeComparator : ScopeComparator {
    override fun hasOverride(state: ToggleState): Boolean {
        val defaultScope = state.scopes.firstOrNull { it.name == ColumnNames.ToggleScope.DEFAULT_SCOPE }
        val selectedScope = state.scopes.maxByOrNull { it.timeStamp }
        if (defaultScope == null || selectedScope == null) return false
        if (defaultScope.id == selectedScope.id) return false
        return state.configurationValues.any { it.scope == selectedScope.id }
    }
}
