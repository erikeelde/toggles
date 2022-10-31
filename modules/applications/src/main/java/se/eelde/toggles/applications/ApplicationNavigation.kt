package se.eelde.toggles.applications

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.application(navigateToConfigurations: (Long) -> Unit) {
    composable(
        route = "applications"
    ) {
        val viewModel: ApplicationViewModel = hiltViewModel()
        val uiState = viewModel.state.collectAsState()
        ApplicationListView(navigateToConfigurations, uiState.value)
    }
}
