package se.eelde.toggles

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import se.eelde.toggles.flow.Toggles

private class FakeToggles(private val boolValues: Map<String, Boolean> = emptyMap()) : Toggles {
    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> =
        flowOf(boolValues[key] ?: defaultValue)
    override fun toggle(key: String, defaultValue: String): Flow<String> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: Int): Flow<Int> = flowOf(defaultValue)
    override fun <T : Enum<T>> toggle(key: String, type: Class<T>, defaultValue: T): Flow<T> =
        flowOf(defaultValue)
}

class MainViewModelTest {
    @Test
    fun `editorAsDialog emits true when toggle is on`() = runTest {
        val values = MainViewModel(
            FakeToggles(mapOf(MainViewModel.EDITOR_PRESENTATION_DIALOG to true))
        ).editorAsDialog.toList()
        assertEquals(listOf(true), values)
    }

    @Test
    fun `editorAsDialog emits false when toggle is off`() = runTest {
        val values = MainViewModel(
            FakeToggles(mapOf(MainViewModel.EDITOR_PRESENTATION_DIALOG to false))
        ).editorAsDialog.toList()
        assertEquals(listOf(false), values)
    }

    @Test
    fun `agentApiEnabled defaults to false when the toggle has never been set`() = runTest {
        val values = MainViewModel(FakeToggles()).agentApiEnabled.toList()
        assertEquals(listOf(false), values)
    }

    @Test
    fun `agentApiEnabled emits true when the beta_agent_api toggle is on`() = runTest {
        val values = MainViewModel(
            FakeToggles(mapOf(MainViewModel.BETA_AGENT_API to true))
        ).agentApiEnabled.toList()
        assertEquals(listOf(true), values)
    }
}
