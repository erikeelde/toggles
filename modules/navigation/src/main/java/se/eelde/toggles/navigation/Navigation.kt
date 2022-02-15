package se.eelde.toggles.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

typealias Destinations = MutableMap<Class<out FeatureEntry>, @JvmSuppressWildcards FeatureEntry>

val featureDestinations: Destinations = mutableMapOf()

// https://medium.com/google-developer-experts/modular-navigation-with-jetpack-compose-fda9f6b2bef7
// https://proandroiddev.com/navigating-through-multi-module-jetpack-compose-applications-6c9a31fa12b6
interface FeatureEntry {

    val featureRoute: String

    val arguments: List<NamedNavArgument>
        get() = emptyList()

    fun NavGraphBuilder.composable(
        navController: NavHostController,
    ) {
        composable(featureRoute, arguments) { backStackEntry ->
            Composable(navController, backStackEntry)
        }
    }

    @Composable
    fun NavGraphBuilder.Composable(
        navController: NavHostController,
        backStackEntry: NavBackStackEntry
    )
}
