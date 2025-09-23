package se.eelde.toggles.enumconfiguration

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.coroutines.IoDispatcher
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesPredefinedConfigurationValue
import se.eelde.toggles.database.dao.application.TogglesConfigurationDao
import se.eelde.toggles.database.dao.application.TogglesConfigurationValueDao
import se.eelde.toggles.database.dao.application.TogglesPredefinedConfigurationValueDao
import se.eelde.toggles.provider.notifyInsert
import se.eelde.toggles.provider.notifyUpdate
import se.eelde.toggles.routes.EnumConfiguration
import java.util.Date

data class ViewState(
    val title: String? = null,
    val selectedConfigurationValue: TogglesConfigurationValue? = null,
    val configurationValues: List<TogglesPredefinedConfigurationValue> = listOf(),
    val saving: Boolean = false,
    val reverting: Boolean = false
)

internal sealed class PartialViewState {
    data object Empty : PartialViewState()
    data class NewConfiguration(val title: String?) : PartialViewState()
    data class ConfigurationValues(val configurationValues: List<TogglesPredefinedConfigurationValue>) :
        PartialViewState()

    data class SelectedConfigurationValue(val selectedConfigurationValue: TogglesConfigurationValue) :
        PartialViewState()

    data object Saving : PartialViewState()
    data object Reverting : PartialViewState()
}

@HiltViewModel(assistedFactory = EnumValueViewModel.Factory::class)
class EnumValueViewModel @AssistedInject internal constructor(
    private val application: Application,
    private val configurationDao: TogglesConfigurationDao,
    private val configurationValueDao: TogglesConfigurationValueDao,
    private val predefinedConfigurationValueDao: TogglesPredefinedConfigurationValueDao,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @Assisted enumConfiguration: EnumConfiguration,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            enumConfiguration: EnumConfiguration
        ): EnumValueViewModel
    }

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    val state: StateFlow<ViewState>
        get() = _state

    private val configurationId: Long = enumConfiguration.configurationId
    private val scopeId: Long = enumConfiguration.scopeId

    private var selectedConfigurationValue: TogglesConfigurationValue? = null

    init {
        viewModelScope.launch {
            predefinedConfigurationValueDao.getByConfigurationId(configurationId).collect {
                _state.value = reduce(_state.value, PartialViewState.ConfigurationValues(it))
            }
        }

        viewModelScope.launch {
            configurationDao.getConfiguration(configurationId).collect {
                _state.value = reduce(_state.value, PartialViewState.NewConfiguration(it.key))
            }
        }
        viewModelScope.launch {
            configurationValueDao.getConfigurationValue(configurationId, scopeId).collect {
                if (it != null) {
                    selectedConfigurationValue = it
                    _state.value =
                        reduce(_state.value, PartialViewState.SelectedConfigurationValue(it))
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

            is PartialViewState.ConfigurationValues -> {
                previousState.copy(configurationValues = partialViewState.configurationValues)
            }

            is PartialViewState.SelectedConfigurationValue -> {
                previousState.copy(selectedConfigurationValue = partialViewState.selectedConfigurationValue)
            }
        }
    }

    suspend fun saveClick(value: String) {
        _state.value = reduce(_state.value, PartialViewState.Saving)
        updateConfigurationValue(value).join()
        application.contentResolver.notifyUpdate(
            TogglesProviderContract.toggleUri(
                configurationId
            )
        )
    }

    suspend fun revertClick() {
        _state.value = reduce(_state.value, PartialViewState.Reverting)
        deleteConfigurationValue().join()
        application.contentResolver.notifyInsert(
            TogglesProviderContract.toggleUri(
                configurationId
            )
        )
    }

    private suspend fun updateConfigurationValue(value: String): Job = coroutineScope {
        viewModelScope.launch(ioDispatcher) {
            if (selectedConfigurationValue != null) {
                configurationValueDao.updateConfigurationValue(configurationId, scopeId, value)
            } else {
                val togglesConfigurationValue =
                    TogglesConfigurationValue(0, configurationId, value, scopeId)
                togglesConfigurationValue.id =
                    configurationValueDao.insert(togglesConfigurationValue)
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
        viewModelScope.launch(ioDispatcher) {
            selectedConfigurationValue?.let {
                configurationValueDao.delete(it)

                application.contentResolver.notifyUpdate(
                    TogglesProviderContract.toggleUri(
                        it.configurationId
                    )
                )
            }
        }
    }
}
