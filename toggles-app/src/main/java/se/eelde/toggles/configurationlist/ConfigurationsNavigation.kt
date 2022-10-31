package se.eelde.toggles.configurationlist

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

fun NavGraphBuilder.configurations(
    navigateToStringConfiguration: ConfigurationNavigation,
    navigateToIntegerConfiguration: ConfigurationNavigation,
    navigateToBooleanConfiguration: ConfigurationNavigation,
    navigateToEnumConfiguration: ConfigurationNavigation,
) {
    composable(route = "configurations/{applicationId}",
        arguments = listOf(navArgument("applicationId") { type = NavType.LongType }
        )
    ) {
        val viewModel: ConfigurationViewModel = hiltViewModel()
        val uiState = viewModel.state.collectAsState()
        ConfigurationListView(
            navigateToStringConfiguration = navigateToStringConfiguration,
            navigateToIntegerConfiguration = navigateToIntegerConfiguration,
            navigateToBooleanConfiguration = navigateToBooleanConfiguration,
            navigateToEnumConfiguration = navigateToEnumConfiguration,
            uiState = uiState.value
        )
    }
}
