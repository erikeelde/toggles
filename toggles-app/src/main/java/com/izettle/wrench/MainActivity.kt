package com.izettle.wrench

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.applicationlist.ApplicationListView
import se.eelde.toggles.compose_theme.TogglesTheme
import se.eelde.toggles.configurationlist.ConfigurationListView
import se.eelde.toggles.dialogs.booleanvalue.BooleanValueView
import se.eelde.toggles.dialogs.enumvalue.EnumValueView
import se.eelde.toggles.dialogs.integervalue.IntegerValueView
import se.eelde.toggles.dialogs.stringvalue.StringValueView

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "applications") {
        composable("applications") { ApplicationListView(navController) }
        composable(
            "configurations/{applicationId}",
            arguments = listOf(navArgument("applicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            ConfigurationListView(
                navController,
                backStackEntry.arguments?.getLong("applicationId")!!
            )
        }
        composable(
            "configuration/{configurationId}/{scopeId}/boolean",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            BooleanValueView(
                navController,
                backStackEntry.arguments?.getLong("configurationId")!!,
                backStackEntry.arguments?.getLong("scopeId")!!
            )
        }
        composable(
            "configuration/{configurationId}/{scopeId}/integer",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            IntegerValueView(
                navController,
                backStackEntry.arguments?.getLong("configurationId")!!,
                backStackEntry.arguments?.getLong("scopeId")!!
            )
        }
        composable(
            "configuration/{configurationId}/{scopeId}/string",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            StringValueView(
                navController,
                backStackEntry.arguments?.getLong("configurationId")!!,
                backStackEntry.arguments?.getLong("scopeId")!!
            )
        }
        composable(
            "configuration/{configurationId}/{scopeId}/enum",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            EnumValueView(
                navController,
                backStackEntry.arguments?.getLong("configurationId")!!,
                backStackEntry.arguments?.getLong("scopeId")!!
            )
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TogglesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background
                ) {
                    val navController: NavHostController = rememberNavController()

                    Navigation(navController)

                }
            }
        }
    }
}
