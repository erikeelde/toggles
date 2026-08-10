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

    // A prior version of this test proved that a null-valued row in the selected scope still won
    // over the default scope, because effectiveValue resolves by key presence rather than value
    // nullability. That state is no longer representable: `valuesByScopeId` is now
    // `Map<Long, String>` (see MIGRATION_11_12, which makes `configurationValue.value` NOT NULL
    // at the database level), so a present entry can no longer hold null. The presence-over-null
    // resolution rule this test exercised is unreachable code now, not a behaviour to keep
    // covering.

    @Test
    fun `effective value is null when no scope holds a value`() {
        val chain = ScopeChain(selectedScopeId = 7, defaultScopeId = 1)

        assertNull(ScopeResolution.effectiveValue(emptyMap(), chain))
    }
}
