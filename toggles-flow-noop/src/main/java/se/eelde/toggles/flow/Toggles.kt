package se.eelde.toggles.flow

import kotlinx.coroutines.flow.Flow

@Suppress("LibraryEntitiesShouldNotBePublic")
public interface Toggles {
    public fun toggle(key: String, defaultValue: Boolean): Flow<Boolean>
    public fun toggle(key: String, defaultValue: String): Flow<String>
    public fun toggle(key: String, defaultValue: Int): Flow<Int>
    public fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T>
    public suspend fun hasToggle(key: String): Boolean
}
