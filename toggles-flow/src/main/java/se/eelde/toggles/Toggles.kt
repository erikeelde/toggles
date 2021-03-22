package se.eelde.toggles

import android.content.Context
import kotlinx.coroutines.flow.Flow

interface Toggles {
    fun toggle(key: String, defaultValue: Boolean): Flow<Boolean>
    fun toggle(key: String, defaultValue: String): Flow<String>
    fun toggle(key: String, defaultValue: Int): Flow<Int>
    fun <T : Enum<T>> toggle(
        context: Context,
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T>
}
