package se.eelde.toggles.composetheme

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),
): AppState {
    return remember(navController) {
        AppState(navController)
    }
}

data class AppBarState(
    val title: String = "",
    val actions: (@Composable RowScope.() -> Unit)? = null
)

class AppState constructor(
    private val navController: NavHostController,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val isRootDestination: Boolean
        @Composable get() = when (currentDestination?.route) {
            "applications" -> true
            "help" -> true
            "oss" -> true
            else -> false
        }
    fun navigateUp() = navController.navigateUp()
}