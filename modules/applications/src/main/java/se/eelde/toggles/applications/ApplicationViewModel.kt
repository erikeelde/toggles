package se.eelde.toggles.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.dao.application.TogglesApplicationDao
import javax.inject.Inject

internal data class ViewState(
    val applications: List<TogglesApplication> = listOf(),
)

internal sealed class PartialViewState {
    object Empty : PartialViewState()
    class Applications(val applications: List<TogglesApplication>) :
        PartialViewState()
}

@HiltViewModel
internal class ApplicationViewModel @Inject constructor(applicationDao: TogglesApplicationDao) :
    ViewModel() {

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    internal val state: StateFlow<ViewState>
        get() = _state

    init {
        viewModelScope.launch {
            applicationDao.getApplications().collect {
                _state.value = reduce(_state.value, PartialViewState.Applications(it))
            }
        }
    }

    private fun reduce(viewState: ViewState, partialViewState: PartialViewState): ViewState {
        return when (partialViewState) {
            is PartialViewState.Applications -> viewState.copy(applications = partialViewState.applications)
            PartialViewState.Empty -> viewState
        }
    }
}
