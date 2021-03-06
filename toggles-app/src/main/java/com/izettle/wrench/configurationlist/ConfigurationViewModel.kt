package com.izettle.wrench.configurationlist

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchApplicationDao
import com.izettle.wrench.database.WrenchConfigurationDao
import com.izettle.wrench.database.WrenchConfigurationWithValues
import com.izettle.wrench.database.WrenchScope
import com.izettle.wrench.database.WrenchScopeDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

internal data class ViewState(
    val configurations: List<WrenchConfigurationWithValues> = listOf(),
    val defaultScope: WrenchScope? = null,
    val selectedScope: WrenchScope? = null,
)

internal sealed class PartialViewState {
    object Empty : PartialViewState()
    data class Configurations(val configurations: List<WrenchConfigurationWithValues>) :
        PartialViewState()

    data class SelectedScope(val scope: WrenchScope) : PartialViewState()
    data class DefaultScope(val scope: WrenchScope) : PartialViewState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConfigurationViewModel @Inject internal constructor(
    private val applicationDao: WrenchApplicationDao,
    configurationDao: WrenchConfigurationDao,
    scopeDao: WrenchScopeDao,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    internal val applicationId: Long = savedStateHandle.get<Long>("applicationId")!!

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    internal val state: StateFlow<ViewState>
        get() = _state

    internal val wrenchApplication: LiveData<WrenchApplication> =
        applicationDao.getApplicationLiveData(applicationId)

    private val queryString: MutableStateFlow<String> = MutableStateFlow("")

    init {
        setQuery(savedStateHandle.get<String>("query") ?: "")
        viewModelScope.launch {
            queryString.value = (savedStateHandle.get<String>("query") ?: "")
        }

        viewModelScope.launch {
            scopeDao.getSelectedScopeFlow(applicationId = applicationId).collect { scope ->
                _state.value = reduce(_state.value, PartialViewState.SelectedScope(scope))
            }
        }

        viewModelScope.launch {
            scopeDao.getDefaultScopeFlow(applicationId = applicationId).collect { scope ->
                _state.value = reduce(_state.value, PartialViewState.DefaultScope(scope))
            }
        }

        viewModelScope.launch {
            queryString.flatMapLatest { queryString ->
                if (TextUtils.isEmpty(queryString)) {
                    configurationDao.getApplicationConfigurations(applicationId)
                } else {
                    configurationDao.getApplicationConfigurations(applicationId, "%$queryString%")
                }
            }.collect { value ->
                _state.value = reduce(_state.value, PartialViewState.Configurations(value))
            }
        }
    }

    private fun reduce(viewState: ViewState, partialViewState: PartialViewState): ViewState {
        return when (partialViewState) {
            is PartialViewState.Configurations -> viewState.copy(
                configurations = partialViewState.configurations
            )
            PartialViewState.Empty -> viewState
            is PartialViewState.DefaultScope -> viewState.copy(defaultScope = partialViewState.scope)
            is PartialViewState.SelectedScope -> viewState.copy(selectedScope = partialViewState.scope)
        }
    }

    fun setQuery(query: String) {
        viewModelScope.launch {
            queryString.value = query
        }
        savedStateHandle["query"] = query
    }

    internal fun deleteApplication(wrenchApplication: WrenchApplication) {
        viewModelScope.launch {
            applicationDao.delete(wrenchApplication)
        }
    }
}
