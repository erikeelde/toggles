package se.eelde.toggles.applications

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.applicationNavigations(navigateToConfigurations: (Long) -> Unit) {
    composable("applications") {
        ApplicationListView(
            viewModel = hiltViewModel(),
            navigateToConfigurations = navigateToConfigurations
        )
    }
}
