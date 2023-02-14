package se.eelde.toggles.flow

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Suppress("LibraryEntitiesShouldNotBePublic")
class TogglesImpl(@Suppress("UNUSED_PARAMETER") context: Context) : Toggles {

    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> = flowOf(defaultValue)

    override fun toggle(key: String, defaultValue: Int): Flow<Int> = flowOf(defaultValue)

    override fun toggle(key: String, defaultValue: String): Flow<String> = flowOf(defaultValue)

    override fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T> = flowOf(defaultValue)
}
