package se.eelde.toggles.integerconfiguration

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.WrenchConfigurationValue
import se.eelde.toggles.database.dao.application.TogglesConfigurationDao
import se.eelde.toggles.database.dao.application.TogglesConfigurationValueDao
import se.eelde.toggles.provider.notifyUpdate
import se.eelde.toggles.routes.IntegerConfiguration
import java.util.Date

data class ViewState(
    val title: String? = null,
    val integerValue: Int? = null,
    val saving: Boolean = false,
    val reverting: Boolean = false
)

private sealed class PartialViewState {
    object Empty : PartialViewState()
    data class NewConfiguration(val title: String?) : PartialViewState()
    data class NewConfigurationValue(val value: Int) : PartialViewState()
    object Saving : PartialViewState()
    object Reverting : PartialViewState()
}

@HiltViewModel(assistedFactory = IntegerValueViewModel.Factory::class)
class IntegerValueViewModel @AssistedInject internal constructor(
    private val application: Application,
    private val configurationDao: TogglesConfigurationDao,
    private val configurationValueDao: TogglesConfigurationValueDao,
    @Assisted integerConfiguration: IntegerConfiguration,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            integerConfiguration: IntegerConfiguration
        ): IntegerValueViewModel
    }
    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    val state: StateFlow<ViewState>
        get() = _state

    private val configurationId: Long = integerConfiguration.configurationId
    private val scopeId: Long = integerConfiguration.scopeId

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
                    try {
                        _state.value = reduce(
                            state.value,
                            PartialViewState.NewConfigurationValue(it.value!!.toInt())
                        )
                    } catch (e: NumberFormatException) {
                        // delete the value if we encounter a numberformat exception
                        deleteConfigurationValue()
                    }
                }
            }
        }
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
            is PartialViewState.NewConfigurationValue -> {
                previousState.copy(integerValue = partialViewState.value)
            }
        }
    }

    fun setIntegerValue(newValue: Int) {
        _state.value = reduce(state.value, PartialViewState.NewConfigurationValue(newValue))
    }

    suspend fun saveClick() {
        _state.value = reduce(state.value, PartialViewState.Saving)
        // updateConfigurationValue(state.value.integerValue).join()

        state.value.integerValue?.let {
            updateConfigurationValue(it).join()
        } ?: run {
            deleteConfigurationValue()
        }
    }

    suspend fun revertClick() {
        _state.value = reduce(state.value, PartialViewState.Reverting)
        deleteConfigurationValue().join()
    }

    private suspend fun updateConfigurationValue(value: Int): Job = coroutineScope {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedConfigurationValue != null) {
                configurationValueDao.updateConfigurationValue(configurationId, scopeId, value.toString())
            } else {
                val wrenchConfigurationValue =
                    WrenchConfigurationValue(0, configurationId, value.toString(), scopeId)
                wrenchConfigurationValue.id = configurationValueDao.insert(wrenchConfigurationValue)
            }
            configurationDao.touch(configurationId, Date())

            application.contentResolver.notifyUpdate(TogglesProviderContract.toggleUri(configurationId))
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
