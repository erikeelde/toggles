package se.eelde.toggles.flow

import se.eelde.toggles.core.ToggleState

@Suppress("LibraryEntitiesShouldNotBePublic")
public fun interface ScopeComparator {
    public fun hasOverride(state: ToggleState): Boolean
}

@Suppress("LibraryEntitiesShouldNotBePublic")
public object DefaultScopeComparator : ScopeComparator {
    override fun hasOverride(state: ToggleState): Boolean = false
}
