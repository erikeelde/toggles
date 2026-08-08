package se.eelde.toggles.agent

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentErrorTest {

    private val json = Json { ignoreUnknownKeys = false }

    @Test
    fun `error json carries code and message`() {
        val envelope = json.decodeFromString<AgentErrorEnvelope>(
            AgentError.json(AgentErrorCode.UNKNOWN_PACKAGE, "no such package: com.example.app")
        )

        assertEquals("unknown_package", envelope.error.code)
        assertEquals("no such package: com.example.app", envelope.error.message)
    }

    @Test
    fun `error json escapes quotes in the message`() {
        val envelope = json.decodeFromString<AgentErrorEnvelope>(
            AgentError.json(AgentErrorCode.INVALID_ARGUMENT, """bad "value" given""")
        )

        assertEquals("""bad "value" given""", envelope.error.message)
    }

    @Test
    fun `error json survives a message containing newlines and braces`() {
        val nasty = "line one\nline two {\"not\": \"json\"}"
        val envelope = json.decodeFromString<AgentErrorEnvelope>(
            AgentError.json(AgentErrorCode.INTERNAL_ERROR, nasty)
        )

        assertEquals(nasty, envelope.error.message)
    }

    @Test
    fun `every error code has a distinct non blank wire value`() {
        val wireValues = AgentErrorCode.entries.map { it.wireValue }

        assertEquals(wireValues.size, wireValues.toSet().size)
        assertTrue(wireValues.all { it.isNotBlank() })
    }
}
