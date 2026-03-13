package se.eelde.toggles.flow

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleState

@Suppress("LibraryEntitiesShouldNotBePublic")
public class TogglesImpl @JvmOverloads constructor(
    context: Context,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    addDefaultAutomatically: Boolean = true,
    updateDefaultAutomatically: Boolean = false,
    onMissingToggle: ((key: String, defaultValue: String, toggleState: ToggleState) -> Unit)? = null,
    onDefaultMismatch: (
        (key: String, storedDefault: String, requestedDefault: String, toggleState: ToggleState) -> Unit
    )? = null,
) : Toggles {
    private val provider = TogglesProvider(context, ioDispatcher)
    private val resolver = TogglesResolver(
        provider,
        addDefaultAutomatically,
        updateDefaultAutomatically,
        onMissingToggle,
        onDefaultMismatch
    )

    override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> =
        resolveFlow(key, Toggle.TYPE.BOOLEAN, defaultValue.toString())
            .map { it.toBoolean() }

    override fun toggle(key: String, defaultValue: Int): Flow<Int> =
        resolveFlow(key, Toggle.TYPE.INTEGER, defaultValue.toString())
            .map { it.toInt() }

    override fun toggle(key: String, defaultValue: String): Flow<String> =
        resolveFlow(key, Toggle.TYPE.STRING, defaultValue)

    override fun <T : Enum<T>> toggle(
        key: String,
        type: Class<T>,
        defaultValue: T
    ): Flow<T> =
        resolveFlow(key, Toggle.TYPE.ENUM, defaultValue.toString()) { configurationId ->
            provider.insertPredefinedValues(
                configurationId,
                type.enumConstants!!.map { it.toString() }
            )
        }.map { java.lang.Enum.valueOf(type, it) }

    private fun resolveFlow(
        key: String,
        type: String,
        defaultValue: String,
        onFirstCreate: ((configurationId: Long) -> Unit)? = null,
    ): Flow<String> =
        provider.observeToggleState(key)
            .map { toggleState ->
                resolver.resolve(toggleState, key, type, defaultValue, onFirstCreate)
            }
}
