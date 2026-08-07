package se.eelde.toggles.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AgentValueValidatorTest {

    @Test
    fun `boolean accepts true and false`() {
        assertNull(AgentValueValidator.rejectionReason("boolean", "true", emptyList()))
        assertNull(AgentValueValidator.rejectionReason("boolean", "false", emptyList()))
    }

    @Test
    fun `boolean rejects anything else`() {
        assertNotNull(AgentValueValidator.rejectionReason("boolean", "yes", emptyList()))
        assertNotNull(AgentValueValidator.rejectionReason("boolean", "1", emptyList()))
        assertNotNull(AgentValueValidator.rejectionReason("boolean", "TRUE", emptyList()))
        assertNotNull(AgentValueValidator.rejectionReason("boolean", "", emptyList()))
    }

    @Test
    fun `the boolean rejection explains the silent false behaviour`() {
        val reason = requireNotNull(AgentValueValidator.rejectionReason("boolean", "yes", emptyList()))

        assertEquals(true, reason.contains("false"))
    }

    @Test
    fun `integer accepts decimal integers including negatives`() {
        assertNull(AgentValueValidator.rejectionReason("integer", "0", emptyList()))
        assertNull(AgentValueValidator.rejectionReason("integer", "-42", emptyList()))
        assertNull(AgentValueValidator.rejectionReason("integer", "2147483647", emptyList()))
    }

    @Test
    fun `integer rejects non numeric and overflowing values`() {
        assertNotNull(AgentValueValidator.rejectionReason("integer", "abc", emptyList()))
        assertNotNull(AgentValueValidator.rejectionReason("integer", "1.5", emptyList()))
        assertNotNull(AgentValueValidator.rejectionReason("integer", "99999999999999999999", emptyList()))
        assertNotNull(AgentValueValidator.rejectionReason("integer", "", emptyList()))
    }

    @Test
    fun `string accepts anything`() {
        assertNull(AgentValueValidator.rejectionReason("string", "", emptyList()))
        assertNull(AgentValueValidator.rejectionReason("string", "anything at all", emptyList()))
        assertNull(AgentValueValidator.rejectionReason("string", "{\"json\": true}", emptyList()))
    }

    @Test
    fun `enum accepts a predefined value`() {
        assertNull(AgentValueValidator.rejectionReason("enum", "b", listOf("a", "b")))
    }

    @Test
    fun `enum rejects a value that is not predefined`() {
        assertNotNull(AgentValueValidator.rejectionReason("enum", "c", listOf("a", "b")))
    }

    @Test
    fun `the enum rejection lists the allowed values`() {
        val reason = requireNotNull(AgentValueValidator.rejectionReason("enum", "c", listOf("a", "b")))

        assertEquals(true, reason.contains("a"))
        assertEquals(true, reason.contains("b"))
    }

    @Test
    fun `enum with no predefined values rejects everything`() {
        assertNotNull(AgentValueValidator.rejectionReason("enum", "a", emptyList()))
    }

    @Test
    fun `enum tolerates null entries in the predefined list`() {
        assertNull(AgentValueValidator.rejectionReason("enum", "a", listOf("a", null)))
        assertNotNull(AgentValueValidator.rejectionReason("enum", "z", listOf("a", null)))
    }

    @Test
    fun `an unknown type is rejected rather than silently accepted`() {
        assertNotNull(AgentValueValidator.rejectionReason("timestamp", "123", emptyList()))
    }
}
