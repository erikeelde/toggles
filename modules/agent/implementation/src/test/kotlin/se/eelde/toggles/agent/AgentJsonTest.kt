package se.eelde.toggles.agent

import kotlinx.serialization.Serializable
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Two fixtures, two independent guarantees:
 *
 * - [NullableFixture] has NO declared default for `optional`. kotlinx-serialization always
 *   encodes a property with no declared default, regardless of `encodeDefaults`. This pins the
 *   "no defaults on payload DTOs" convention — if a later task adds `= null` to a DTO field, the
 *   [DefaultedFixture] tests below are what would catch the field silently disappearing (assuming
 *   `encodeDefaults` were also flipped off), not these.
 *
 * - [DefaultedFixture] DOES declare `optional: String? = null`. Without `encodeDefaults = true` in
 *   [agentJson], a value equal to the declared default is omitted entirely. This fixture's tests
 *   are the ones that actually depend on, and would fail if someone flips, `encodeDefaults` in
 *   AgentJson.kt.
 */
class AgentJsonTest {

    @Serializable
    private data class NullableFixture(val name: String, val optional: String?)

    @Serializable
    private data class DefaultedFixture(val name: String, val optional: String? = null)

    @Test
    fun `no-default field- a null field is emitted rather than omitted`() {
        val encoded = agentJson.encodeToString(NullableFixture("x", null))

        assertTrue(
            "expected an explicit null for the optional field, got: $encoded",
            encoded.contains("\"optional\"")
        )
        assertTrue(
            "expected the optional field to be null, got: $encoded",
            encoded.replace(" ", "").replace("\n", "").contains("\"optional\":null")
        )
    }

    @Test
    fun `no-default field- a non null field round trips`() {
        val encoded = agentJson.encodeToString(NullableFixture("x", "present"))
        val decoded = agentJson.decodeFromString<NullableFixture>(encoded)

        assertTrue(decoded.optional == "present")
    }

    @Test
    fun `encodeDefaults- a defaulted null field is still emitted explicitly`() {
        val encoded = agentJson.encodeToString(DefaultedFixture("x"))

        assertTrue(
            "expected an explicit null for the defaulted optional field even though it equals " +
                "its declared default — this only holds because encodeDefaults = true; got: $encoded",
            encoded.replace(" ", "").replace("\n", "").contains("\"optional\":null")
        )
    }

    @Test
    fun `encodeDefaults- a defaulted non null field round trips`() {
        val encoded = agentJson.encodeToString(DefaultedFixture("x", "present"))
        val decoded = agentJson.decodeFromString<DefaultedFixture>(encoded)

        assertTrue(decoded.optional == "present")
    }
}
