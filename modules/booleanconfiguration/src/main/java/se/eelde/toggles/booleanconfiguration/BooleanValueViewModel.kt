package se.eelde.toggles.booleanconfiguration

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.WrenchConfigurationDao
import se.eelde.toggles.database.WrenchConfigurationValue
import se.eelde.toggles.database.WrenchConfigurationValueDao
import se.eelde.toggles.provider.notifyUpdate
import java.util.Date
import javax.inject.Inject

data class ViewState(
    val title: String? = null,
    val checked: Boolean? = null,
    val saving: Boolean = false,
    val reverting: Boolean = false
)

private sealed class PartialViewState {
    object Empty : PartialViewState()
    data class NewConfiguration(val title: String?) : PartialViewState()
    data class NewConfigurationValue(val checked: Boolean) : PartialViewState()
    object Saving : PartialViewState()
    object Reverting : PartialViewState()
}

@HiltViewModel
class FragmentBooleanValueViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val configurationDao: WrenchConfigurationDao,
    private val configurationValueDao: WrenchConfigurationValueDao
) : ViewModel() {

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    val state: StateFlow<ViewState>
        get() = _state

    private val configurationId: Long = savedStateHandle.get<Long>("configurationId")!!
    private val scopeId: Long = savedStateHandle.get<Long>("scopeId")!!

    private var selectedConfigurationValue: WrenchConfigurationValue? = null

    init {
        viewModelScope.launch {
            configurationDao.getConfiguration(configurationId).collect {
                _state.value = reduce(state.value, PartialViewState.NewConfiguration(it.key))
            }
        }
        viewModelScope.launch {
            configurationValueDao.getConfigurationValue(configurationId, scopeId).collect {
                if (it != null) {
                    selectedConfigurationValue = it
                    // viewEffects.value = Event(ViewEffect.CheckedChanged(it.value!!.toBoolean()))
                    _state.value = reduce(
                        state.value,
                        PartialViewState.NewConfigurationValue(it.value!!.toBoolean())
                    )
                }
            }
        }
    }

    fun checkedChanged(checked: Boolean) {
        _state.value = reduce(state.value, PartialViewState.NewConfigurationValue(checked))
    }

    private fun reduce(previousState: ViewState, partialViewState: PartialViewState): ViewState {
        return when (partialViewState) {
            is PartialViewState.NewConfiguration -> {
                previousState.copy(title = partialViewState.title)
            }

            is PartialViewState.Empty -> {
                previousState
            }

            is PartialViewState.Saving -> {
                previousState.copy(saving = true)
            }

            is PartialViewState.Reverting -> {
                previousState.copy(reverting = true)
            }

            is PartialViewState.NewConfigurationValue ->
                previousState.copy(checked = partialViewState.checked)
        }
    }

    suspend fun saveClick() {
        _state.value = reduce(state.value, PartialViewState.Saving)
        state.value.checked?.let { checkedState ->
            updateConfigurationValue(checkedState.toString()).join()
        } ?: run {
            deleteConfigurationValue()
        }
    }

    suspend fun revertClick() {
        _state.value = reduce(state.value, PartialViewState.Reverting)
        deleteConfigurationValue().join()
    }

    private suspend fun updateConfigurationValue(value: String): Job = coroutineScope {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedConfigurationValue != null) {
                configurationValueDao.updateConfigurationValue(configurationId, scopeId, value)
            } else {
                val wrenchConfigurationValue =
                    WrenchConfigurationValue(0, configurationId, value, scopeId)
                wrenchConfigurationValue.id = configurationValueDao.insert(wrenchConfigurationValue)
            }
            configurationDao.touch(configurationId, Date())

            application.contentResolver.notifyUpdate(
                TogglesProviderContract.toggleUri(
                    configurationId
                )
            )
        }
    }

    private suspend fun deleteConfigurationValue(): Job = coroutineScope {
        viewModelScope.launch(Dispatchers.IO) {
            selectedConfigurationValue?.let {
                configurationValueDao.delete(it)

                application.contentResolver.notifyUpdate(
                    TogglesProviderContract.toggleUri(
                        configurationId
                    )
                )
            }
        }
    }
}
