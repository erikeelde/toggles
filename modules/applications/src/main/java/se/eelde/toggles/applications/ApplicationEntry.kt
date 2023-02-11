package se.eelde.toggles.applications

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import se.eelde.toggles.composetheme.AppBarState

fun NavGraphBuilder.applicationNavigations(
    onComposing: (AppBarState) -> Unit,
    navigateToConfigurations: (Long) -> Unit
) {
    composable("applications") {
        val viewModel = hiltViewModel<ApplicationViewModel>()
        val viewState = viewModel.state.collectAsState()

        LaunchedEffect(key1 = true) {
            onComposing(
                AppBarState(title = "Applications")
            )
        }

        ApplicationsView(
            viewState = viewState.value,
            navigateToConfigurations = navigateToConfigurations
        )
    }
}
