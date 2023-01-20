package se.eelde.toggles.configurationlist

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

fun NavGraphBuilder.configurationsNavigations(navController: NavController) {
    composable(
        "configurations/{applicationId}",
        arguments = listOf(navArgument("applicationId") { type = NavType.LongType })
    ) {
        ConfigurationListView(
            navController = navController,
            viewModel = hiltViewModel(),
        )
    }
}
