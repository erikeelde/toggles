package se.eelde.toggles.agent

import kotlinx.serialization.json.Json

/**
 * The single JSON configuration for every agent response.
 *
 * `prettyPrint` because a human reads this output in a terminal alongside the agent.
 *
 * `encodeDefaults` is on so that a property is emitted even when it equals its declared default.
 * Combined with payload DTOs that declare no defaults, this guarantees a null field is serialised
 * as an explicit `null` rather than omitted — an agent must be able to distinguish "resolved to no
 * value" from "field absent from this API version". See AgentJsonTest.
 */
internal val agentJson: Json = Json {
    prettyPrint = true
    encodeDefaults = true
}
