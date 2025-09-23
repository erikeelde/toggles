package se.eelde.toggles.dialogs.scope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.eelde.toggles.coroutines.IoDispatcher
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.dao.application.TogglesScopeDao
import se.eelde.toggles.routes.Scope
import java.util.Date

internal data class ViewState(
    val title: String? = null,
    val scopes: List<TogglesScope> = listOf(),
    val selectedScope: TogglesScope? = null,
    val saving: Boolean = false,
    val reverting: Boolean = false
)

private sealed class PartialViewState {
    object Empty : PartialViewState()
    data class NewConfiguration(val title: String?) : PartialViewState()
    class Scopes(val scopes: List<TogglesScope>) : PartialViewState()
    object Saving : PartialViewState()
    object Reverting : PartialViewState()
}

@HiltViewModel(assistedFactory = ScopeViewModel.Factory::class)
class ScopeViewModel @AssistedInject internal constructor(
    private val scopeDao: TogglesScopeDao,
    @Assisted scope: Scope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            scope: Scope
        ): ScopeViewModel
    }

    private val applicationId: Long = scope.applicationId

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    internal val state: StateFlow<ViewState>
        get() = _state

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
                    selectedScope = partialViewState.scopes.maxByOrNull { it.timeStamp }!!,
                    scopes = partialViewState.scopes
                )
            }
        }
    }

    internal fun selectScope(togglesScope: TogglesScope) {
        viewModelScope.launch {
            togglesScope.timeStamp = Date()
            scopeDao.update(togglesScope)
        }
    }

    internal fun createScope(scopeName: String) {
        viewModelScope.launch {
            val togglesScope = TogglesScope.newScope()
            togglesScope.name = scopeName
            togglesScope.applicationId = applicationId
            withContext(ioDispatcher) {
                togglesScope.id = scopeDao.insert(togglesScope)
            }
        }
    }

    internal fun removeScope(scope: TogglesScope) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                scopeDao.delete(scope)
            }
        }
    }
}
