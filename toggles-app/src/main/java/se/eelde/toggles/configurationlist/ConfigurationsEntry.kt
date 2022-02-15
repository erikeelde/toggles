package se.eelde.toggles.configurationlist

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import se.eelde.toggles.navigation.FeatureEntry

class ConfigurationsEntry : FeatureEntry {
    override val featureRoute: String = "configurations/{applicationId}"

    override val arguments: List<NamedNavArgument>
        get() = listOf(navArgument("applicationId") { type = NavType.LongType })

    @Composable
    override fun NavGraphBuilder.Composable(
        navController: NavHostController,
        backStackEntry: NavBackStackEntry
    ) {
        ConfigurationListView(navController)
    }
}
