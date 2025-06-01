package se.eelde.toggles.stringconfiguration

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
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.dao.application.TogglesConfigurationDao
import se.eelde.toggles.database.dao.application.TogglesConfigurationValueDao
import se.eelde.toggles.provider.notifyUpdate
import se.eelde.toggles.routes.StringConfiguration
import java.util.Date

data class ViewState(
    val title: String? = null,
    val stringValue: String? = null,
    val saving: Boolean = false,
    val reverting: Boolean = false
)

private sealed class PartialViewState {
    object Empty : PartialViewState()
    data class NewConfiguration(val title: String?) : PartialViewState()
    data class NewConfigurationValue(val value: String) : PartialViewState()
    object Saving : PartialViewState()
    object Reverting : PartialViewState()
}

@HiltViewModel(assistedFactory = StringValueViewModel.Factory::class)
class StringValueViewModel
@AssistedInject internal constructor(
    private val application: Application,
    private val configurationDao: TogglesConfigurationDao,
    private val configurationValueDao: TogglesConfigurationValueDao,
    @Assisted stringConfiguration: StringConfiguration
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(
            stringConfiguration: StringConfiguration
        ): StringValueViewModel
    }

    private val configurationId = stringConfiguration.configurationId
    private val scopeId = stringConfiguration.scopeId

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    val state: StateFlow<ViewState>
        get() = _state

    private var selectedConfigurationValue: TogglesConfigurationValue? = null

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
                    _state.value = reduce(
                        state.value,
                        PartialViewState.NewConfigurationValue(it.value!!)
                    )
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
                previousState.copy(stringValue = partialViewState.value)
            }
        }
    }

    fun setStringValue(newValue: String) {
        _state.value = reduce(state.value, PartialViewState.NewConfigurationValue(newValue))
    }

    suspend fun saveClick() {
        _state.value = reduce(state.value, PartialViewState.Saving)
        state.value.stringValue?.let {
            updateConfigurationValue(it).join()
        } ?: run {
            deleteConfigurationValue()
        }
    }

    suspend fun revertClick() {
        _state.value = reduce(state.value, PartialViewState.Reverting)
        deleteConfigurationValue()
    }

    private suspend fun updateConfigurationValue(value: String): Job = coroutineScope {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedConfigurationValue != null) {
                configurationValueDao.updateConfigurationValue(configurationId, scopeId, value)
            } else {
                val togglesConfigurationValue =
                    TogglesConfigurationValue(0, configurationId, value, scopeId)
                togglesConfigurationValue.id = configurationValueDao.insert(togglesConfigurationValue)
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
