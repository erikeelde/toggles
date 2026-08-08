package se.eelde.toggles.agent

import android.os.Bundle
import se.eelde.toggles.database.dao.agent.AgentDao
import se.eelde.toggles.database.dao.agent.AgentMutationDao
import se.eelde.toggles.provider.resolution.ScopeResolution

/**
 * Implements `deleteConfigurationValue`: removes one scope's override row so resolution falls back
 * to the next scope in the chain. This is the agent-API equivalent of the Toggles UI's Revert
 * button (`BooleanValueView.kt` -> `BooleanValueViewModel.revertClick()` ->
 * `configurationValueDao.delete(...)`) — deleting the row, not overwriting it with the default's
 * current value, so a later change to the default scope still propagates.
 *
 * Split out of [AgentCallHandler] for the same TooManyFunctions reason as
 * [AgentApplicationProvisioner]: that class is already at detekt's function-count limit.
 */
internal class AgentConfigurationValueDeleter(
    private val agentDao: AgentDao,
    private val agentMutationDao: AgentMutationDao,
    private val changeNotifier: AgentChangeNotifier,
    private val controlNotifier: AgentControlNotifier,
) {

    /** What a configuration resolves to, and which scope's row produced it (if any). */
    private data class EffectiveValueResolution(val value: String?, val resolvedFromScopeName: String?)

    // Each early return is a distinct validation gate, same rationale as
    // AgentCallHandler.setConfigurationValue.
    @Suppress("ReturnCount")
    fun handle(extras: Bundle?): String {
        val configurationId = extras.longExtra(AgentCallContract.KEY_CONFIGURATION_ID)
            ?: return missingArgumentError(AgentCallContract.KEY_CONFIGURATION_ID)
        val scopeId = extras.longExtra(AgentCallContract.KEY_SCOPE_ID)
            ?: return missingArgumentError(AgentCallContract.KEY_SCOPE_ID)

        val configuration = agentMutationDao.getConfiguration(configurationId)
            ?: return unknownIdError("no configuration with id $configurationId")

        val application = agentDao.getApplicationById(configuration.applicationId)
            ?: return unknownIdError(
                "configuration $configurationId has no owning application " +
                    "(applicationId ${configuration.applicationId})"
            )

        if (!application.agentControlEnabled) {
            return AgentError.json(
                AgentErrorCode.AGENT_CONTROL_DISABLED,
                "agent control is disabled for ${application.packageName}. Enable it in the " +
                    "Toggles app."
            )
        }

        val scope = agentMutationDao.getScope(scopeId)
            ?: return unknownIdError("no scope with id $scopeId")

        if (scope.applicationId != configuration.applicationId) {
            return AgentError.json(
                AgentErrorCode.INVALID_ARGUMENT,
                "scope $scopeId belongs to a different application than configuration " +
                    "$configurationId; cross-application deletes are not allowed."
            )
        }

        // Deleting an absent override is idempotent: an agent retrying a delete must not see a
        // failure just because a previous attempt (or another agent) already removed it. Only
        // notify when a row was actually removed, matching BooleanValueViewModel.deleteConfigurationValue,
        // which only notifies when selectedConfigurationValue was non-null. The control notifier
        // follows the same rule: a no-op delete changed nothing, so there is nothing to disclose.
        val deletedRows = agentMutationDao.deleteConfigurationValue(configurationId, scopeId)
        if (deletedRows > 0) {
            changeNotifier.notifyConfigurationChanged(configurationId)
            controlNotifier.notifyFirstMutation(application.packageName, application.applicationLabel)
        }

        val resolution = resolveEffectiveValue(application.id, configurationId)
        val summary = if (deletedRows > 0) {
            "removed the \"${scope.name}\" override for ${configuration.key}; it now resolves to " +
                describeResolution(resolution)
        } else {
            "no \"${scope.name}\" override existed for ${configuration.key}; nothing to remove. " +
                "It currently resolves to " + describeResolution(resolution)
        }

        return agentJson.encodeToString(
            AgentMutationResponse(
                method = AgentCallContract.METHOD_DELETE_CONFIGURATION_VALUE,
                summary = summary,
                applicationPackage = application.packageName,
                configurationId = configurationId,
                configurationKey = configuration.key,
                scopeId = scopeId,
                scopeName = scope.name,
                value = resolution.value,
                packageVerified = null
            )
        )
    }

    private val unresolved = EffectiveValueResolution(value = null, resolvedFromScopeName = null)

    private fun resolveEffectiveValue(applicationId: Long, configurationId: Long): EffectiveValueResolution {
        val scopes = agentDao.getScopes(applicationId)
        val selectedScope = scopes.maxByOrNull { it.timeStamp }
        val chain = scopeChainFor(scopes, selectedScope)

        val valuesByScopeId = agentDao.getConfigurationValues(applicationId)
            .filter { it.configurationId == configurationId }
            .associate { it.scope to it.value }

        val resolvedScopeId = chain?.orderedScopeIds?.firstOrNull { valuesByScopeId.containsKey(it) }

        return if (chain == null || resolvedScopeId == null) {
            unresolved
        } else {
            EffectiveValueResolution(
                value = ScopeResolution.effectiveValue(valuesByScopeId, chain),
                resolvedFromScopeName = scopes.firstOrNull { it.id == resolvedScopeId }?.name
            )
        }
    }

    private fun describeResolution(resolution: EffectiveValueResolution): String =
        if (resolution.resolvedFromScopeName != null) {
            "\"${resolution.value}\" from \"${resolution.resolvedFromScopeName}\""
        } else {
            "nothing (no scope has a value for it)"
        }
}
