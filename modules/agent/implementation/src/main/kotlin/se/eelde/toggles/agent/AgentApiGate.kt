package se.eelde.toggles.agent

import android.content.Context
import se.eelde.toggles.database.dao.agent.AgentDao
import se.eelde.toggles.provider.resolution.ScopeResolution

/**
 * The global kill switch for the agent API: whether `beta_agent_api` currently resolves to `true`
 * for the Toggles app's own package.
 *
 * Read synchronously, straight from the database, rather than through toggles-flow: toggles-flow
 * talks to the ContentProvider over binder, and this check runs from inside that very provider's
 * own call path (see [TogglesAgentProvider]) — it cannot make a nested binder call back into
 * itself, and it needs a plain `Boolean` where toggles-flow would hand back a `Flow`.
 *
 * Resolution mirrors [AgentReadHandler] exactly (same [AgentDao] queries, same [scopeChainFor] /
 * [ScopeResolution] used to resolve any other configuration's effective value) so the gate and the
 * value the Toggles UI displays for this same toggle can never disagree.
 *
 * A missing configuration — the state of a fresh install, before the app has ever been opened — is
 * treated as disabled, matching the toggle's default. It is registered by
 * `se.eelde.toggles.MainViewModel.agentApiEnabled` in the toggles-app module.
 */
class AgentApiGate(
    private val agentDao: AgentDao,
    private val context: Context,
) {

    // Each early return is a distinct "no answer" gate — no application row, no configuration, no
    // resolvable scope chain — same rationale AgentCallHandler's endpoints use for ReturnCount.
    @Suppress("ReturnCount")
    fun isEnabled(): Boolean {
        val application = agentDao.getApplicationByPackageName(context.packageName) ?: return false

        val configuration = agentDao.getConfigurations(application.id)
            .firstOrNull { it.key == BETA_AGENT_API_KEY } ?: return false

        val scopes = agentDao.getScopes(application.id)
        val selectedScope = scopes.maxByOrNull { it.timeStamp }
        val chain = scopeChainFor(scopes, selectedScope) ?: return false

        val valuesByScopeId = agentDao.getConfigurationValues(application.id)
            .filter { it.configurationId == configuration.id }
            .associate { it.scope to it.value }

        return ScopeResolution.effectiveValue(valuesByScopeId, chain) == "true"
    }

    companion object {
        /**
         * Must stay in sync with `MainViewModel.BETA_AGENT_API` — deliberately not a shared
         * constant across the toggles-app and modules/agent module boundary, the same way
         * `AgentCallContract`'s constants are typed out once per side rather than shared.
         */
        const val BETA_AGENT_API_KEY = "beta_agent_api"
    }
}
