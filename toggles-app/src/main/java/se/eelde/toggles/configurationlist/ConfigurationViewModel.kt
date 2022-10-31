package se.eelde.toggles.configurationlist

import android.text.TextUtils
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import se.eelde.toggles.database.WrenchApplication
import se.eelde.toggles.database.WrenchApplicationDao
import se.eelde.toggles.database.WrenchConfigurationDao
import se.eelde.toggles.database.WrenchConfigurationWithValues
import se.eelde.toggles.database.WrenchScope
import se.eelde.toggles.database.WrenchScopeDao
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
    private val applicationId: Long = savedStateHandle.get<Long>("applicationId")!!

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    internal val state: StateFlow<ViewState>
        get() = _state

    internal lateinit var wrenchApplication: WrenchApplication

    private val queryString: MutableStateFlow<String> = MutableStateFlow("")

    init {
        setQuery(savedStateHandle.get<String>("query") ?: "")
        viewModelScope.launch {
            wrenchApplication = applicationDao.getApplication(applicationId)!!
        }

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
