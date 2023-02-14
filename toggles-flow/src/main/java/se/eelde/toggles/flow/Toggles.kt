package se.eelde.toggles.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@Suppress("LibraryEntitiesShouldNotBePublic")
interface Toggles {
    @ExperimentalCoroutinesApi
    fun toggle(key: String, defaultValue: Boolean): Flow<Boolean>

    @ExperimentalCoroutinesApi
    fun toggle(key: String, defaultValue: String): Flow<String>

    @ExperimentalCoroutinesApi
    fun toggle(key: String, defaultValue: Int): Flow<Int>

    @ExperimentalCoroutinesApi
    fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T>
}
