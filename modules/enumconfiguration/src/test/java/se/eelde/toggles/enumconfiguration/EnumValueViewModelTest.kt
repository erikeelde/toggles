package se.eelde.toggles.enumconfiguration

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import se.eelde.toggles.flow.Toggles

private class FakeToggles(private val boolValue: Boolean) : Toggles {
    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> = flowOf(boolValue)
    override fun toggle(key: String, defaultValue: String): Flow<String> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: Int): Flow<Int> = flowOf(defaultValue)
    override fun <T : Enum<T>> toggle(key: String, type: Class<T>, defaultValue: T): Flow<T> =
        flowOf(defaultValue)
}

class EnumValueViewModelTest {
    @Test
    fun `expressive list flag true flows from FakeToggles`() = runTest {
        val result = mutableListOf<Boolean>()
        FakeToggles(boolValue = true)
            .toggle("expressive_enum_list", false)
            .collect { result.add(it) }
        assertTrue(result.first())
    }

    @Test
    fun `expressive list flag false flows from FakeToggles`() = runTest {
        val result = mutableListOf<Boolean>()
        FakeToggles(boolValue = false)
            .toggle("expressive_enum_list", false)
            .collect { result.add(it) }
        assertFalse(result.first())
    }
}
