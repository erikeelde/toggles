package se.eelde.toggles.agent

import android.os.Bundle
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.dao.agent.AgentDao
import se.eelde.toggles.database.dao.agent.AgentMutationDao

/**
 * Implements `deleteScope`.
 *
 * A constraint the schema does not enforce, required here:
 *
 * [AgentReadHandler]'s scope-chain resolution (see [scopeChainFor]) returns null when an
 * application has no scope named [TogglesScope.SCOPE_DEFAULT], which silently nulls out every
 * `effectiveValue` for that application. The default scope can therefore never be deleted here.
 *
 * `configurationValue.scope` has a foreign key to `scope(id) ON DELETE CASCADE` (added in
 * `MIGRATION_9_10`), so deleting a scope row cascades to its value rows automatically — no
 * explicit cleanup is needed here.
 *
 * Split out of [AgentCallHandler] for the same TooManyFunctions reason as
 * [AgentApplicationProvisioner] and [AgentConfigurationValueDeleter].
 */
internal class AgentScopeDeleter(
    private val agentDao: AgentDao,
    private val agentMutationDao: AgentMutationDao,
    private val changeNotifier: AgentChangeNotifier,
    private val controlNotifier: AgentControlNotifier,
) {

    // Each early return is a distinct validation gate, same rationale as
    // AgentCallHandler.setConfigurationValue.
    @Suppress("ReturnCount")
    fun handle(extras: Bundle?): String {
        val packageName = extras.stringExtra(AgentCallContract.KEY_PACKAGE)
            ?: return missingArgumentError(AgentCallContract.KEY_PACKAGE)
        val scopeId = extras.longExtra(AgentCallContract.KEY_SCOPE_ID)
            ?: return missingArgumentError(AgentCallContract.KEY_SCOPE_ID)

        val application = agentDao.getApplicationByPackageName(packageName)
            ?: return AgentError.json(
                AgentErrorCode.UNKNOWN_PACKAGE,
                "Toggles has no record of $packageName. Read /apps for the known packages."
            )

        if (!application.agentControlEnabled) {
            return AgentError.json(
                AgentErrorCode.AGENT_CONTROL_DISABLED,
                "agent control is disabled for $packageName. Enable it in the Toggles app."
            )
        }

        val scope = agentMutationDao.getScope(scopeId)
            ?: return unknownIdError("no scope with id $scopeId")

        if (scope.applicationId != application.id) {
            return AgentError.json(
                AgentErrorCode.INVALID_ARGUMENT,
                "scope $scopeId belongs to a different application than $packageName; " +
                    "cross-application scope deletion is not allowed."
            )
        }

        if (scope.name == TogglesScope.SCOPE_DEFAULT) {
            return AgentError.json(
                AgentErrorCode.INVALID_ARGUMENT,
                "\"${scope.name}\" is $packageName's default scope; deleting it would leave " +
                    "every configuration in the application with no resolvable value. Delete a " +
                    "non-default scope instead."
            )
        }

        val wasSelected = agentDao.getScopes(application.id).maxByOrNull { it.timeStamp }?.id == scope.id

        agentMutationDao.deleteScope(scopeId)
        changeNotifier.notifyScopesChanged()
        controlNotifier.notifyFirstMutation(application.packageName, application.applicationLabel)

        return agentJson.encodeToString(
            AgentMutationResponse(
                method = AgentCallContract.METHOD_DELETE_SCOPE,
                summary = summary(packageName, scope, application.id, wasSelected),
                applicationPackage = packageName,
                configurationId = null,
                configurationKey = null,
                scopeId = scopeId,
                scopeName = scope.name,
                value = null,
                packageVerified = null
            )
        )
    }

    private fun summary(
        packageName: String,
        deletedScope: TogglesScope,
        applicationId: Long,
        wasSelected: Boolean,
    ): String {
        // The default scope can never be deleted (see this class's kdoc), so at least one scope
        // always remains — a new selection always exists when the deleted scope was selected.
        val newSelection = if (wasSelected) {
            agentDao.getScopes(applicationId).maxByOrNull { it.timeStamp }
        } else {
            null
        }

        return "deleted scope \"${deletedScope.name}\" from $packageName" +
            if (newSelection != null) {
                "; it was the selected scope, selection moved to \"${newSelection.name}\""
            } else {
                ""
            }
    }
}
