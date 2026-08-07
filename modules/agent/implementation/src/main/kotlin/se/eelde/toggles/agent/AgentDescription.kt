package se.eelde.toggles.agent

import kotlinx.serialization.Serializable

@Serializable
data class AgentEndpoint(
    val path: String,
    val returns: String,
    val example: String,
)

@Serializable
data class AgentModelDocumentation(
    val configurationTypes: List<String>,
    val scopeResolution: String,
    val agentControl: String,
    val callers: String,
    val valueFormats: Map<String, String>,
)

@Serializable
data class AgentDescriptionDocument(
    val agentApiVersion: Int,
    val togglesAppVersion: String,
    val authority: String,
    val endpoints: List<AgentEndpoint>,
    val model: AgentModelDocumentation,
    val errors: List<String>,
)

/**
 * The self-describing API contract. Host-side tooling hardcodes only the `/describe` command and
 * discovers everything else from here, so it can never drift from the installed APK.
 */
object AgentDescription {

    const val AGENT_API_VERSION = 1
    const val AGENT_AUTHORITY = "se.eelde.toggles.agentprovider"

    fun document(appVersionName: String): AgentDescriptionDocument = AgentDescriptionDocument(
        agentApiVersion = AGENT_API_VERSION,
        togglesAppVersion = appVersionName,
        authority = AGENT_AUTHORITY,
        endpoints = listOf(
            AgentEndpoint(
                path = "/describe",
                returns = "This document.",
                example = "adb shell content read --uri content://$AGENT_AUTHORITY/describe"
            ),
            AgentEndpoint(
                path = "/apps",
                returns = "Every application Toggles knows about, with its agentControlEnabled flag.",
                example = "adb shell content read --uri content://$AGENT_AUTHORITY/apps"
            ),
            AgentEndpoint(
                path = "/apps/{package}",
                returns = "One application's scopes, configurations, every per-scope value, " +
                    "predefined values and effective values.",
                example = "adb shell content read --uri content://$AGENT_AUTHORITY/apps/com.example.app"
            )
        ),
        model = AgentModelDocumentation(
            configurationTypes = listOf("boolean", "integer", "string", "enum"),
            scopeResolution = "Each application has a default scope and zero or more additional " +
                "scopes. The selected scope is the one with the most recent selectedTimestamp. " +
                "Resolution consults exactly two scopes: the selected scope, then the default " +
                "scope. Value rows in any other scope are ignored entirely — they are reported " +
                "under each configuration's values array but never affect effectiveValue. The " +
                "fallback to the default scope happens only when the selected scope has no value " +
                "row at all; a row whose value is null still counts as a hit and stops the " +
                "fallback. effectiveValue is the result of that resolution, and null means the " +
                "application observes no value from Toggles — check the values array before " +
                "concluding a configuration has no value anywhere.",
            agentControl = "Every /apps/{package} call requires that application's " +
                "agentControlEnabled flag to be true. It defaults to true and can be turned off " +
                "per application inside the Toggles app.",
            callers = "Only uid 2000 (shell) and uid 0 (root) may call this provider. Every other " +
                "caller receives a not_authorized error.",
            valueFormats = mapOf(
                "boolean" to "Exactly \"true\" or \"false\". The client library parses with " +
                    "Kotlin's String.toBoolean(), which returns true only for a case-insensitive " +
                    "\"true\" and silently returns false for anything else — a malformed value " +
                    "does not error, it reads as false.",
                "integer" to "A decimal integer string. The client library parses with " +
                    "String.toInt(), which throws NumberFormatException in the consuming app if " +
                    "the value is not a valid integer.",
                "string" to "Any string.",
                "enum" to "Must be one of the values listed in that configuration's " +
                    "predefinedValues array."
            )
        ),
        errors = AgentErrorCode.entries.map { it.wireValue }
    )

    fun json(appVersionName: String): String = agentJson.encodeToString(document(appVersionName))
}
