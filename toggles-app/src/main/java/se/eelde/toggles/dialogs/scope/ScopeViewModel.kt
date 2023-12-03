package se.eelde.toggles.dialogs.scope

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.eelde.toggles.database.WrenchScope
import se.eelde.toggles.database.WrenchScopeDao
import java.util.Date
import javax.inject.Inject

internal data class ViewState(
    val title: String? = null,
    val scopes: List<WrenchScope> = listOf(),
    val selectedScope: WrenchScope? = null,
    val saving: Boolean = false,
    val reverting: Boolean = false
)

private sealed class PartialViewState {
    object Empty : PartialViewState()
    data class NewConfiguration(val title: String?) : PartialViewState()
    class Scopes(val scopes: List<WrenchScope>) : PartialViewState()
    object Saving : PartialViewState()
    object Reverting : PartialViewState()
}

@HiltViewModel
class ScopeViewModel @Inject internal constructor(
    private val savedStateHandle: SavedStateHandle,
    private val scopeDao: WrenchScopeDao
) : ViewModel() {

    private val applicationId: Long = savedStateHandle.get<Long>("applicationId")!!

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    internal val state: StateFlow<ViewState>
        get() = _state

    internal var selectedScope: WrenchScope? = null

    init {
        viewModelScope.launch {
            scopeDao.getScopes(applicationId = applicationId).collect {
                _state.value = reduce(_state.value, PartialViewState.Scopes(it))
            }
        }
    }

    private fun reduce(previousState: ViewState, partialViewState: PartialViewState): ViewState {
        return when (partialViewState) {
            PartialViewState.Empty -> previousState
            is PartialViewState.NewConfiguration -> previousState
            PartialViewState.Reverting -> previousState
            PartialViewState.Saving -> previousState
            is PartialViewState.Scopes -> {
                previousState.copy(
                    selectedScope = partialViewState.scopes.sortedByDescending { it.timeStamp }.first(),
                    scopes = partialViewState.scopes
                )
            }
        }
    }

    internal fun selectScope(wrenchScope: WrenchScope) {
        viewModelScope.launch {
            wrenchScope.timeStamp = Date()
            scopeDao.update(wrenchScope)
        }
    }

    internal fun createScope(scopeName: String) {
        viewModelScope.launch {
            val wrenchScope = WrenchScope.newWrenchScope()
            wrenchScope.name = scopeName
            wrenchScope.applicationId = applicationId
            withContext(Dispatchers.IO) {
                wrenchScope.id = scopeDao.insert(wrenchScope)
            }
        }
    }

    internal fun removeScope(scope: WrenchScope) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                scopeDao.delete(scope)
            }
        }
    }
}
