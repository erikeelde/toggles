package se.eelde.toggles

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import se.eelde.toggles.flow.Toggles

private class FakeToggles(private val boolValue: Boolean) : Toggles {
    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> = flowOf(boolValue)
    override fun toggle(key: String, defaultValue: String): Flow<String> = flowOf(defaultValue)
    override fun toggle(key: String, defaultValue: Int): Flow<Int> = flowOf(defaultValue)
    override fun <T : Enum<T>> toggle(key: String, type: Class<T>, defaultValue: T): Flow<T> =
        flowOf(defaultValue)
}

class MainViewModelTest {
    @Test
    fun `editorAsDialog emits true when toggle is on`() = runTest {
        val values = MainViewModel(FakeToggles(boolValue = true)).editorAsDialog.toList()
        assertEquals(listOf(true), values)
    }

    @Test
    fun `editorAsDialog emits false when toggle is off`() = runTest {
        val values = MainViewModel(FakeToggles(boolValue = false)).editorAsDialog.toList()
        assertEquals(listOf(false), values)
    }
}
