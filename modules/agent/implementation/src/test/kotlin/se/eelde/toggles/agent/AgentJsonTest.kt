package se.eelde.toggles.agent

import kotlinx.serialization.Serializable
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentJsonTest {

    @Serializable
    private data class NullableFixture(val name: String, val optional: String?)

    @Test
    fun `a null field is emitted rather than omitted`() {
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
    fun `a non null field round trips`() {
        val encoded = agentJson.encodeToString(NullableFixture("x", "present"))
        val decoded = agentJson.decodeFromString<NullableFixture>(encoded)

        assertTrue(decoded.optional == "present")
    }
}
