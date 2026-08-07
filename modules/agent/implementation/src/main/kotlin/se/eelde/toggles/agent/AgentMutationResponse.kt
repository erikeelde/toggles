package se.eelde.toggles.agent

import kotlinx.serialization.Serializable

/**
 * Every mutation echoes human-readable names alongside the ids it acted on. Requests are id-based
 * — `{configurationId: 47, scopeId: 3}` — so without this echo an agent transcript reads as
 * "set 47/3" and nobody reviewing the session can tell what happened.
 */
@Serializable
data class AgentMutationResponse(
    val method: String,
    val summary: String,
    val applicationPackage: String?,
    val configurationId: Long?,
    val configurationKey: String?,
    val scopeId: Long?,
    val scopeName: String?,
    val value: String?,
    /**
     * Null except from createConfiguration when it just provisioned a new application row (see
     * [AgentApplicationProvisioner.ApplicationResolution]). True/false there reports whether
     * PackageManager could confirm [applicationPackage] is actually installed on this device —
     * false is the signal that a typo in the package name would surface as, since Toggles has no
     * other way to distinguish a genuine not-yet-run package from a misspelled one. Also folded
     * into [summary] as prose so a plain-text transcript carries the same information.
     */
    val packageVerified: Boolean?,
)
