package se.eelde.toggles.enumconfiguration

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
import se.eelde.toggles.database.WrenchPredefinedConfigurationValue
import se.eelde.toggles.database.WrenchPredefinedConfigurationValueDao
import se.eelde.toggles.provider.notifyInsert
import se.eelde.toggles.provider.notifyUpdate
import java.util.Date
import javax.inject.Inject

data class ViewState(
    val title: String? = null,
    val configurationValues: List<WrenchPredefinedConfigurationValue> = listOf(),
    val saving: Boolean = false,
    val reverting: Boolean = false
)

internal sealed class PartialViewState {
    object Empty : PartialViewState()
    data class NewConfiguration(val title: String?) : PartialViewState()
    class ConfigurationValues(val configurationValues: List<WrenchPredefinedConfigurationValue>) :
        PartialViewState()

    object Saving : PartialViewState()
    object Reverting : PartialViewState()
}

@HiltViewModel
class FragmentEnumValueViewModel @Inject internal constructor(
    private val savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val configurationDao: WrenchConfigurationDao,
    private val configurationValueDao: WrenchConfigurationValueDao,
    private val predefinedConfigurationValueDao: WrenchPredefinedConfigurationValueDao
) : ViewModel() {

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    val state: StateFlow<ViewState>
        get() = _state

    private val configurationId: Long = savedStateHandle.get<Long>("configurationId")!!
    private val scopeId: Long = savedStateHandle.get<Long>("scopeId")!!

    private var selectedConfigurationValue: WrenchConfigurationValue? = null

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
                        it.configurationId
                    )
                )
            }
        }
    }
}
