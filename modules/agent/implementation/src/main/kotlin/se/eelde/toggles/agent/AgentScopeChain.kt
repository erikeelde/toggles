package se.eelde.toggles.agent

import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.provider.resolution.ScopeChain

/**
 * The [ScopeChain] a configuration's effective value resolves against, given an application's full
 * scope list and its currently selected scope (the most recently touched one, i.e.
 * `scopes.maxByOrNull { it.timeStamp }`).
 *
 * Shared by [AgentReadHandler] (every configuration's `effectiveValue`) and
 * [AgentConfigurationValueDeleter] (the post-delete `effectiveValue` reported in
 * deleteConfigurationValue's summary) so the two surfaces can never resolve a chain differently.
 */
internal fun scopeChainFor(scopes: List<TogglesScope>, selectedScope: TogglesScope?): ScopeChain? {
    val defaultScope = scopes.firstOrNull { it.name == TogglesScope.SCOPE_DEFAULT }
    return if (selectedScope != null && defaultScope != null) {
        ScopeChain(selectedScopeId = selectedScope.id, defaultScopeId = defaultScope.id)
    } else {
        null
    }
}
