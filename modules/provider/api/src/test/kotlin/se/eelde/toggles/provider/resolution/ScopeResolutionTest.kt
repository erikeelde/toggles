package se.eelde.toggles.provider.resolution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScopeResolutionTest {

    @Test
    fun `chain lists selected scope before default scope`() {
        val chain = ScopeChain(selectedScopeId = 7, defaultScopeId = 1)

        assertEquals(listOf(7L, 1L), chain.orderedScopeIds)
    }

    @Test
    fun `chain lists a single id when the selected scope is the default scope`() {
        val chain = ScopeChain(selectedScopeId = 1, defaultScopeId = 1)

        assertEquals(listOf(1L), chain.orderedScopeIds)
    }

    @Test
    fun `effective value prefers the selected scope`() {
        val chain = ScopeChain(selectedScopeId = 7, defaultScopeId = 1)

        val value = ScopeResolution.effectiveValue(mapOf(7L to "dev", 1L to "prod"), chain)

        assertEquals("dev", value)
    }

    @Test
    fun `effective value falls back to the default scope`() {
        val chain = ScopeChain(selectedScopeId = 7, defaultScopeId = 1)

        val value = ScopeResolution.effectiveValue(mapOf(1L to "prod"), chain)

        assertEquals("prod", value)
    }

    @Test
    fun `effective value skips a null value in the selected scope`() {
        val chain = ScopeChain(selectedScopeId = 7, defaultScopeId = 1)

        val value = ScopeResolution.effectiveValue(mapOf(7L to null, 1L to "prod"), chain)

        assertEquals("prod", value)
    }

    @Test
    fun `effective value is null when no scope holds a value`() {
        val chain = ScopeChain(selectedScopeId = 7, defaultScopeId = 1)

        assertNull(ScopeResolution.effectiveValue(emptyMap(), chain))
    }
}
