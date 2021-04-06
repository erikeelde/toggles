package com.izettle.wrench.dialogs.scope

import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.izettle.wrench.database.WrenchScope
import com.izettle.wrench.database.WrenchScopeDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

internal data class ViewState(
    val title: String? = null,
    val scopes: List<WrenchScope> = listOf(),
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
class ScopeFragmentViewModel @Inject internal constructor(
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
            is PartialViewState.Scopes -> previousState.copy(scopes = partialViewState.scopes)
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
            val wrenchScope = WrenchScope()
            wrenchScope.name = scopeName
            wrenchScope.applicationId = applicationId
            wrenchScope.id = scopeDao.insert(wrenchScope)
        }
    }

    internal fun removeScope(scope: WrenchScope) {
        viewModelScope.launch {
            scopeDao.delete(scope)
        }
    }
}
