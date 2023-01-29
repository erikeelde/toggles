package se.eelde.toggles.applications

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.applicationNavigations(navigateToConfigurations: (Long) -> Unit) {
    composable("applications") {
        val viewModel = hiltViewModel<ApplicationViewModel>()
        val viewState = viewModel.state.collectAsState()

        ApplicationsView(
            viewState = viewState.value,
            navigateToConfigurations = navigateToConfigurations
        )
    }
}
