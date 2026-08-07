package se.eelde.toggles.provider.resolution

/**
 * The ordered list of scopes a toggle value is looked up in: the application's currently selected
 * scope first, then its default scope.
 */
data class ScopeChain(val selectedScopeId: Long, val defaultScopeId: Long) {
    val orderedScopeIds: List<Long>
        get() = if (selectedScopeId == defaultScopeId) {
            listOf(defaultScopeId)
        } else {
            listOf(selectedScopeId, defaultScopeId)
        }
}

/**
 * Shared by TogglesProvider (modules/provider/implementation) and the agent API. Both surfaces
 * must resolve values identically, otherwise the agent would report a value the app does not read.
 */
object ScopeResolution {

    /**
     * The value an application will actually observe, given every value it has per scope.
     *
     * Contract for [valuesByScopeId]: it must contain an entry for every scope that has a value
     * row, including rows whose value is null. Key presence, not value nullability, is what marks
     * a scope as resolved — this mirrors the SQL join TogglesProvider queries against, where an
     * inner join on a null-valued row still counts as a matching row (no fallback to the next
     * scope). A scope with no row at all must be absent from the map entirely so the chain can
     * fall through to the next scope.
     *
     * A `null` return is therefore ambiguous by design: either the winning scope's row holds a
     * null value, or no scope in the chain has a row at all.
     */
    fun effectiveValue(valuesByScopeId: Map<Long, String?>, chain: ScopeChain): String? =
        chain.orderedScopeIds
            .firstOrNull { scopeId -> valuesByScopeId.containsKey(scopeId) }
            ?.let { scopeId -> valuesByScopeId[scopeId] }
}
