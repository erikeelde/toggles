package se.eelde.toggles.agent

import kotlinx.serialization.Serializable

/**
 * Machine-readable failure codes. `adb shell content` swallows binder exceptions and prints a
 * misleading "No result found.", so the agent API never throws across the binder — every failure
 * comes back as one of these in a JSON envelope.
 */
enum class AgentErrorCode(val wireValue: String) {
    NOT_AUTHORIZED("not_authorized"),
    AGENT_CONTROL_DISABLED("agent_control_disabled"),
    UNKNOWN_PACKAGE("unknown_package"),
    UNKNOWN_ID("unknown_id"),
    UNKNOWN_ENDPOINT("unknown_endpoint"),
    INVALID_ARGUMENT("invalid_argument"),
    INTERNAL_ERROR("internal_error"),
}

@Serializable
data class AgentErrorBody(val code: String, val message: String)

@Serializable
data class AgentErrorEnvelope(val error: AgentErrorBody)

object AgentError {

    fun json(code: AgentErrorCode, message: String): String =
        agentJson.encodeToString(AgentErrorEnvelope(AgentErrorBody(code.wireValue, message)))
}
