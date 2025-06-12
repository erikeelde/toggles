package se.eelde.toggles.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.composetheme.TogglesTheme
import se.eelde.toggles.example.flow.FlowView
import se.eelde.toggles.example.info.InfoView
import se.eelde.toggles.example.routes.Flow
import se.eelde.toggles.example.routes.Info
import se.eelde.toggles.example.routes.Oss
import se.eelde.toggles.oss.OssView

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TogglesTheme {
                val navController: NavHostController = rememberNavController()

                Navigation(navController = navController)
            }
        }
    }
}

@Composable
fun Navigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val backStack = remember { mutableStateListOf<Any>(Flow) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Info> {
                Scaffold(bottomBar = {
                    BottomNavigationBar(
                        prefsClick = { backStack.add(Info) },
                        flowClick = { backStack.add(Flow) },
                        ossClick = { backStack.add(Oss) },
                        rootDestination = RootDestination.Info
                    )
                }) { paddingValues ->
                    InfoView(
                        hiltViewModel(),
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )
                }
            }
            entry<Flow> {
                Scaffold(bottomBar = {
                    BottomNavigationBar(
                        prefsClick = { backStack.add(Info) },
                        flowClick = { backStack.add(Flow) },
                        ossClick = { backStack.add(Oss) },
                        rootDestination = RootDestination.Flow
                    )
                }) { paddingValues ->
                    FlowView(
                        hiltViewModel(),
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )
                }
            }
            entry<Oss> {
                Scaffold(bottomBar = {
                    BottomNavigationBar(
                        prefsClick = { backStack.add(Info) },
                        flowClick = { backStack.add(Flow) },
                        ossClick = { backStack.add(Oss) },
                        rootDestination = RootDestination.Oss
                    )
                }) { paddingValues ->
                    OssView(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    )
}

enum class RootDestination {
    Info, Flow, Oss
}

@Composable
fun BottomNavigationBar(
    prefsClick: () -> Unit,
    flowClick: () -> Unit,
    ossClick: () -> Unit,
    rootDestination: RootDestination,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = rootDestination == RootDestination.Info,
            onClick = { prefsClick() },
            icon = {
                Icon(
                    imageVector = Icons.Filled.SettingsEthernet,
                    contentDescription = null
                )
            },
            label = { Text(text = stringResource(id = R.string.nav_menu_toggles_prefs)) }
        )

        NavigationBarItem(
            selected = rootDestination == RootDestination.Flow,
            onClick = { flowClick() },
            icon = {
                Icon(
                    imageVector = Icons.Filled.SettingsBackupRestore,
                    contentDescription = null
                )
            },
            label = { Text(text = stringResource(id = R.string.nav_menu_toggles_flow)) }
        )

        NavigationBarItem(
            selected = rootDestination == RootDestination.Oss,
            onClick = { ossClick() },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Source,
                    contentDescription = null
                )
            },
            label = { Text(text = stringResource(id = R.string.oss)) }
        )
    }
}
