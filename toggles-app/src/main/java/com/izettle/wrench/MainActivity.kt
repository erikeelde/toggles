package com.izettle.wrench

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import se.eelde.toggles.DrawerView
import se.eelde.toggles.R
import se.eelde.toggles.applicationlist.ApplicationListView
import se.eelde.toggles.composetheme.TogglesTheme
import se.eelde.toggles.configurationlist.ConfigurationListView
import se.eelde.toggles.dialogs.booleanvalue.BooleanValueView
import se.eelde.toggles.dialogs.enumvalue.EnumValueView
import se.eelde.toggles.dialogs.integervalue.IntegerValueView
import se.eelde.toggles.dialogs.stringvalue.StringValueView
import se.eelde.toggles.help.HelpView
import se.eelde.toggles.oss.detail.OssDetailView
import se.eelde.toggles.oss.list.OssListView

@Suppress("LongMethod")
@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "applications") {
        composable("applications") { ApplicationListView(navController) }
        composable(
            "configurations/{applicationId}",
            arguments = listOf(navArgument("applicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            ConfigurationListView(
                navController
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
                navController
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
                navController
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
                navController
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
                navController
            )
        }
        composable(
            "oss",
        ) {
            OssListView(navController = navController)
        }
        composable(
            "oss/{dependency}/{skip}/{length}",
            arguments = listOf(
                navArgument("dependency") { type = NavType.StringType },
                navArgument("skip") { type = NavType.IntType },
                navArgument("length") { type = NavType.IntType },
            )
        ) {
            OssDetailView()
        }
        composable(
            "help",
        ) {
            HelpView()
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TogglesTheme {
                val navController: NavHostController = rememberNavController()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                NavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = { DrawerLaLa(navController, drawerState) },
                ) {
                    Navigation(navController)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TogglesAppBar(navController, drawerState)
                        },
                    ) {
                        Navigation(navController)
                    }
                }
            }
        }
    }

    @ExperimentalMaterial3Api
    @Composable
    fun DrawerLaLa(navController: NavHostController, drawerState: DrawerState) {
        val scope = rememberCoroutineScope()
        DrawerView(
            openApplications = {
                navController.navigate("applications")
                scope.launch {
                    drawerState.close()
                }
            },
            openOss = {
                navController.navigate("oss")
                scope.launch {
                    drawerState.close()
                }
            },
            openHelp = {
                navController.navigate("help")
                scope.launch {
                    drawerState.close()
                }
            }
        )
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun TogglesAppBar(navController: NavHostController, drawerState: DrawerState) {
        val currentRoute = navController.currentBackStackEntryAsState()

        val rootId = currentRoute.value?.destination?.parent?.startDestinationRoute
        val currentId = currentRoute.value?.destination?.route
        val rootItem = rootId == currentId

        val scope = rememberCoroutineScope()
        TogglesAppBar(root = rootItem) {
            if (rootItem) {
                scope.launch { drawerState.open() }
            } else {
                navController.navigateUp()
            }
        }
    }

    @Composable
    fun TogglesAppBar(root: Boolean, navigationIconClicked: () -> Unit) {
        TopAppBar(
            title = { Text(stringResource(id = R.string.app_name)) },
            navigationIcon = if (root) {
                {
                    IconButton(onClick = { navigationIconClicked() }) {
                        Icon(imageVector = Icons.Filled.Menu, contentDescription = null)
                    }
                }
            } else {
                {

                    IconButton(onClick = { navigationIconClicked() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            }
        )
    }
}
