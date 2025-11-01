package se.eelde.toggles.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeToggles : Toggles {
    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> =
        flowOf(defaultValue)

    override fun toggle(key: String, defaultValue: String): Flow<String> =
        flowOf(defaultValue)

    override fun toggle(key: String, defaultValue: Int): Flow<Int> =
        flowOf(defaultValue)

    override fun <T : Enum<T>> toggle(key: String, type: Class<T>, defaultValue: T): Flow<T> =
        flowOf(defaultValue)
}
