package se.eelde.toggles.agent

import kotlinx.serialization.Serializable

@Serializable
data class AgentApplicationSummary(
    val packageName: String,
    val applicationLabel: String,
    val agentControlEnabled: Boolean,
)

@Serializable
data class AgentApplicationList(
    val applications: List<AgentApplicationSummary>,
)

@Serializable
data class AgentScope(
    val id: Long,
    val name: String,
    val selected: Boolean,
    val default: Boolean,
)

@Serializable
data class AgentScopeValue(
    val id: Long,
    val scopeId: Long,
    val scopeName: String?,
    val value: String?,
)

@Serializable
data class AgentConfiguration(
    val id: Long,
    val key: String?,
    val type: String,
    /**
     * The value the application resolves right now.
     *
     * Null means the application will observe no value from Toggles. That happens when there is no
     * value row in the selected scope or the default scope — including when rows exist only in
     * some other scope, which resolution never consults — or when the winning scope's row holds a
     * null value. It does NOT mean "no value exists anywhere for this configuration"; check
     * [values] before concluding that.
     */
    val effectiveValue: String?,
    val values: List<AgentScopeValue>,
    val predefinedValues: List<String?>,
)

@Serializable
data class AgentApplicationDetail(
    val packageName: String,
    val applicationLabel: String,
    val agentControlEnabled: Boolean,
    val scopes: List<AgentScope>,
    val configurations: List<AgentConfiguration>,
)
