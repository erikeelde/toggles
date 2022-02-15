package se.eelde.toggles.applications

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import se.eelde.toggles.navigation.FeatureEntry


class ApplicationEntry : FeatureEntry {
    override val featureRoute: String = "applications"

    @Composable
    override fun NavGraphBuilder.Composable(
        navController: NavHostController,
        backStackEntry: NavBackStackEntry
    ) {
        ApplicationListView(navController = navController)
    }
}
