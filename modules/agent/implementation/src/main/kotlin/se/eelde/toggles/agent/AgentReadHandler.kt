package se.eelde.toggles.agent

import android.net.Uri
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfiguration
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesPredefinedConfigurationValue
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.dao.agent.AgentDao
import se.eelde.toggles.provider.resolution.ScopeChain
import se.eelde.toggles.provider.resolution.ScopeResolution

/**
 * Serves every agent read endpoint as a JSON string.
 *
 * Never throws: `adb shell content` hides binder exceptions behind "No result found.", so failures
 * are returned as [AgentErrorEnvelope] payloads instead.
 */
class AgentReadHandler(
    private val agentDao: AgentDao,
    private val uriMatcher: AgentUriMatcher,
    private val appVersionName: String,
) {

    // /describe must stay reachable even when apiEnabled is false: it is how an agent tells
    // "Toggles is installed but the API is off" apart from "Toggles is not installed", and it is
    // what carries the enabled flag itself. Every other match — including UNKNOWN, so a disabled
    // API does not leak which paths are real endpoints — is gated on apiEnabled before the normal
    // per-endpoint dispatch runs.
    @Suppress("TooGenericExceptionCaught") // must never throw across the binder; see class kdoc
    fun handle(uri: Uri, apiEnabled: Boolean): String = try {
        val match = uriMatcher.match(uri)
        if (match != AgentUriMatch.DESCRIBE && !apiEnabled) {
            agentApiDisabledError()
        } else {
            when (match) {
                AgentUriMatch.DESCRIBE -> AgentDescription.json(appVersionName, apiEnabled)
                AgentUriMatch.APPLICATIONS -> agentJson.encodeToString(applications())
                AgentUriMatch.APPLICATION -> application(uri)
                AgentUriMatch.UNKNOWN -> AgentError.json(
                    AgentErrorCode.UNKNOWN_ENDPOINT,
                    "no such endpoint: ${uri.path}. Read /describe for the available endpoints."
                )
            }
        }
    } catch (throwable: Throwable) {
        AgentError.json(
            AgentErrorCode.INTERNAL_ERROR,
            throwable.message ?: throwable::class.java.name
        )
    }

    private fun applications() = AgentApplicationList(
        applications = agentDao.getApplications().map { application ->
            AgentApplicationSummary(
                packageName = application.packageName,
                applicationLabel = application.applicationLabel,
                agentControlEnabled = application.agentControlEnabled
            )
        }
    )

    private fun application(uri: Uri): String {
        val packageName = uri.lastPathSegment
        return if (packageName == null) {
            AgentError.json(AgentErrorCode.INVALID_ARGUMENT, "missing package name in $uri")
        } else {
            resolveApplication(packageName)
        }
    }

    private fun resolveApplication(packageName: String): String {
        val application = agentDao.getApplicationByPackageName(packageName)
        return when {
            application == null -> AgentError.json(
                AgentErrorCode.UNKNOWN_PACKAGE,
                "Toggles has no record of $packageName. Read /apps for the known packages."
            )

            !application.agentControlEnabled -> AgentError.json(
                AgentErrorCode.AGENT_CONTROL_DISABLED,
                "agent control is disabled for $packageName. Enable it in the Toggles app."
            )

            else -> agentJson.encodeToString(detail(application))
        }
    }

    private fun detail(application: TogglesApplication): AgentApplicationDetail {
        val scopes = agentDao.getScopes(application.id)
        val selectedScope = scopes.maxByOrNull { it.timeStamp }
        val chain = scopeChainFor(scopes, selectedScope)

        val valuesByConfigurationId =
            agentDao.getConfigurationValues(application.id).groupBy { it.configurationId }
        val predefinedByConfigurationId =
            agentDao.getPredefinedConfigurationValues(application.id).groupBy { it.configurationId }

        return AgentApplicationDetail(
            packageName = application.packageName,
            applicationLabel = application.applicationLabel,
            agentControlEnabled = application.agentControlEnabled,
            scopes = scopes.map { scope -> scope.toAgentScope(selectedScope) },
            configurations = agentDao.getConfigurations(application.id).map { configuration ->
                configuration.toAgentConfiguration(
                    values = valuesByConfigurationId[configuration.id].orEmpty(),
                    predefined = predefinedByConfigurationId[configuration.id].orEmpty(),
                    scopes = scopes,
                    chain = chain
                )
            }
        )
    }

    private fun TogglesConfiguration.toAgentConfiguration(
        values: List<TogglesConfigurationValue>,
        predefined: List<TogglesPredefinedConfigurationValue>,
        scopes: List<TogglesScope>,
        chain: ScopeChain?,
    ) = AgentConfiguration(
        id = id,
        key = key,
        type = type,
        effectiveValue = resolveEffectiveValue(values, chain),
        values = values.map { value -> value.toAgentScopeValue(scopes) },
        predefinedValues = predefined.map { it.value }
    )

    private fun resolveEffectiveValue(
        values: List<TogglesConfigurationValue>,
        chain: ScopeChain?,
    ): String? = chain?.let {
        // Key presence means "this scope has a value row", including null-valued rows, which
        // matches the INNER JOIN the provider resolves values with. Do not filter nulls out.
        ScopeResolution.effectiveValue(values.associate { value -> value.scope to value.value }, it)
    }

    private fun TogglesConfigurationValue.toAgentScopeValue(scopes: List<TogglesScope>) =
        AgentScopeValue(
            id = id,
            scopeId = scope,
            scopeName = scopes.firstOrNull { candidate -> candidate.id == scope }?.name,
            value = value
        )

    private fun TogglesScope.toAgentScope(selectedScope: TogglesScope?) = AgentScope(
        id = id,
        name = name,
        selected = id == selectedScope?.id,
        default = name == TogglesScope.SCOPE_DEFAULT
    )
}
