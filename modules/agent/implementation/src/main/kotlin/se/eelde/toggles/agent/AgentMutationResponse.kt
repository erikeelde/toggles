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
)
