package se.eelde.toggles.example.scoped

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import se.eelde.toggles.flow.TogglesImpl
import javax.inject.Inject

@HiltViewModel
class ScopedTogglesViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val adminToggles = TogglesImpl(application, scope = "admin")
    private val guestToggles = TogglesImpl(application, scope = "guest")
    private val defaultToggles = TogglesImpl(application)

    internal val viewState: MutableState<ScopedViewState> = mutableStateOf(
        ScopedViewState(
            adminFeatureEnabled = false,
            guestFeatureEnabled = false,
            defaultFeatureEnabled = false,
            adminApiEndpoint = "Loading...",
            guestApiEndpoint = "Loading...",
            defaultApiEndpoint = "Loading..."
        )
    )

    init {
        // Monitor admin scope
        viewModelScope.launch {
            adminToggles.toggle("advanced_mode", false).collect { enabled ->
                viewState.value = viewState.value.copy(adminFeatureEnabled = enabled)
            }
        }

        viewModelScope.launch {
            adminToggles.toggle("api_endpoint", "https://api.admin.example.com").collect { endpoint ->
                viewState.value = viewState.value.copy(adminApiEndpoint = endpoint)
            }
        }

        // Monitor guest scope
        viewModelScope.launch {
            guestToggles.toggle("advanced_mode", false).collect { enabled ->
                viewState.value = viewState.value.copy(guestFeatureEnabled = enabled)
            }
        }

        viewModelScope.launch {
            guestToggles.toggle("api_endpoint", "https://api.guest.example.com").collect { endpoint ->
                viewState.value = viewState.value.copy(guestApiEndpoint = endpoint)
            }
        }

        // Monitor default scope
        viewModelScope.launch {
            defaultToggles.toggle("advanced_mode", false).collect { enabled ->
                viewState.value = viewState.value.copy(defaultFeatureEnabled = enabled)
            }
        }

        viewModelScope.launch {
            defaultToggles.toggle("api_endpoint", "https://api.default.example.com").collect { endpoint ->
                viewState.value = viewState.value.copy(defaultApiEndpoint = endpoint)
            }
        }
    }
}

data class ScopedViewState(
    val adminFeatureEnabled: Boolean,
    val guestFeatureEnabled: Boolean,
    val defaultFeatureEnabled: Boolean,
    val adminApiEndpoint: String,
    val guestApiEndpoint: String,
    val defaultApiEndpoint: String
)
