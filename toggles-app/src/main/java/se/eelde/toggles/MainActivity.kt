package se.eelde.toggles

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import se.eelde.toggles.applications.applicationNavigations
import se.eelde.toggles.composetheme.AppBarState
import se.eelde.toggles.composetheme.AppState
import se.eelde.toggles.composetheme.TogglesTheme
import se.eelde.toggles.composetheme.rememberAppState
import se.eelde.toggles.configurationlist.configurationsNavigations
import se.eelde.toggles.dialogs.booleanvalue.BooleanValueView
import se.eelde.toggles.dialogs.enumvalue.EnumValueView
import se.eelde.toggles.dialogs.integervalue.IntegerValueView
import se.eelde.toggles.dialogs.stringvalue.StringValueView
import se.eelde.toggles.help.HelpView
import se.eelde.toggles.oss.detail.OssDetailView
import se.eelde.toggles.oss.list.OssListView

@Suppress("LongMethod")
@Composable
fun Navigation(
    navController: NavHostController,
    appState: AppState,
    paddingValues: PaddingValues,
    appBarState: AppBarState,
    onComposing: (AppBarState) -> Unit,
) {
    NavHost(
        modifier = Modifier.padding(paddingValues),
        navController = navController,
        startDestination = "applications"
    ) {
        applicationNavigations(
            onComposing = onComposing,
            navigateToConfigurations = { applicationId ->
                navController.navigate("configurations/$applicationId")
            })
        configurationsNavigations(navController)
        composable(
            "configuration/{configurationId}/{scopeId}/boolean",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            BooleanValueView(navController)
        }
        composable(
            "configuration/{configurationId}/{scopeId}/integer",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            IntegerValueView(navController)
        }
        composable(
            "configuration/{configurationId}/{scopeId}/string",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            StringValueView(navController)
        }
        composable(
            "configuration/{configurationId}/{scopeId}/enum",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            EnumValueView(navController)
        }
        composable(
            "oss",
        ) {

            LaunchedEffect(key1 = true) {
                onComposing(
                    AppBarState(title = "Oss",
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_oss),
                                    contentDescription = null
                                )
                            }
                        })
                )
            }
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
            LaunchedEffect(key1 = true) {
                onComposing(
                    AppBarState(title = "Help",
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalHospital,
                                    contentDescription = null
                                )
                            }
                        })
                )
            }
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
                val appState = rememberAppState(navController)
                Log.e("appState", "current destination: ${appState.currentDestination}")
                var appBarState by remember {
                    mutableStateOf(AppBarState())
                }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = appState.isRootDestination,
                    drawerContent = { TogglesDrawer(navController, drawerState) }
                ) {
                    Scaffold(
                        topBar = {
                            TogglesAppBar(drawerState, appState, appBarState)
                        },
                    ) { paddingValues ->
                        Navigation(
                            navController = navController,
                            appState = appState,
                            appBarState = appBarState,
                            paddingValues = paddingValues,
                            onComposing = { appBarState = it }
                        )
                    }
                }
            }
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TogglesAppBar(drawerState: DrawerState, appState: AppState, appBarState: AppBarState) {
    val scope = rememberCoroutineScope()

    TopAppBar(
        title = { Text(appBarState.title) },
        actions = { appBarState.actions?.invoke(this) },
        navigationIcon = if (appState.isRootDestination) {
            {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = null)
                }
            }
        } else {
            {
                IconButton(onClick = { appState.navigateUp() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            }
        }
    )
}

@ExperimentalMaterial3Api
@Composable
@Suppress("LongMethod")
fun TogglesDrawer(navController: NavHostController, drawerState: DrawerState) {
    // https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ModalNavigationDrawer(kotlin.Function0,androidx.compose.ui.Modifier,androidx.compose.material3.DrawerState,kotlin.Boolean,androidx.compose.ui.graphics.Color,kotlin.Function0)
    val scope = rememberCoroutineScope()
    val selectedItem = remember { mutableStateOf(0) }
    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier.background(colorResource(id = R.color.toggles_blue)),
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Application icon"
            )
        }
        Spacer(Modifier.height(12.dp))
        NavigationDrawerItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_settings_white_24dp),
                    contentDescription = null
                )
            },
            label = { Text(stringResource(id = R.string.applications)) },
            selected = 0 == selectedItem.value,
            onClick = {
                scope.launch { drawerState.close() }
                navController.navigate("applications")
                selectedItem.value = 0
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(painterResource(id = R.drawable.ic_oss), contentDescription = null) },
            label = { Text(stringResource(id = R.string.oss)) },
            selected = 1 == selectedItem.value,
            onClick = {
                scope.launch { drawerState.close() }
                navController.navigate("oss")
                selectedItem.value = 1
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_report_black_24dp),
                    contentDescription = null
                )
            },
            label = { Text(stringResource(id = R.string.help)) },
            selected = 2 == selectedItem.value,
            onClick = {
                scope.launch { drawerState.close() }
                navController.navigate("help")
                selectedItem.value = 2
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}