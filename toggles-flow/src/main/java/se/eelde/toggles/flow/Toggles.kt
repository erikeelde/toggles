package se.eelde.toggles.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@Suppress("LibraryEntitiesShouldNotBePublic")
interface Toggles {
    fun toggle(key: String, defaultValue: Boolean): Flow<Boolean>

    fun toggle(key: String, defaultValue: String): Flow<String>

    fun toggle(key: String, defaultValue: Int): Flow<Int>

    fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T>
}
