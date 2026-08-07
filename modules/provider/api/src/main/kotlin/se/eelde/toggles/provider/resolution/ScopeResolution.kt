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
     */
    fun effectiveValue(valuesByScopeId: Map<Long, String?>, chain: ScopeChain): String? =
        chain.orderedScopeIds.firstNotNullOfOrNull { valuesByScopeId[it] }
}
