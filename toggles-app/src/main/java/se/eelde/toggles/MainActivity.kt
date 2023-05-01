package se.eelde.toggles

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.applications.applicationNavigations
import se.eelde.toggles.composetheme.TogglesTheme
import se.eelde.toggles.composetheme.rememberAppState
import se.eelde.toggles.configurationlist.configurationsNavigations
import se.eelde.toggles.dialogs.booleanvalue.BooleanValueView
import se.eelde.toggles.dialogs.booleanvalue.FragmentBooleanValueViewModel
import se.eelde.toggles.dialogs.enumvalue.EnumValueView
import se.eelde.toggles.dialogs.enumvalue.FragmentEnumValueViewModel
import se.eelde.toggles.dialogs.integervalue.FragmentIntegerValueViewModel
import se.eelde.toggles.dialogs.integervalue.IntegerValueView
import se.eelde.toggles.dialogs.stringvalue.FragmentStringValueViewModel
import se.eelde.toggles.dialogs.stringvalue.StringValueView
import se.eelde.toggles.help.HelpView
import se.eelde.toggles.oss.OssView

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun Navigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "applications",
        modifier = modifier
    ) {
        applicationNavigations(
            navigateToConfigurations = { applicationId ->
                navController.navigate("configurations/$applicationId")
            },
            navigateToApplications = { navController.navigate("applications") },
            navigateToOss = { navController.navigate("oss") },
            navigateToHelp = { navController.navigate("help") },
        )
        configurationsNavigations(navController, back = { navController.popBackStack() })
        composable(
            "configuration/{configurationId}/{scopeId}/boolean",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            val viewModel: FragmentBooleanValueViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon =
                        {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
            ) { paddingValues ->
                BooleanValueView(
                    uiState = state,
                    popBackStack = { navController.popBackStack() },
                    revert = {
                        viewModel.revertClick()
                        navController.popBackStack()
                    },
                    save = {
                        viewModel.saveClick()
                        navController.popBackStack()
                    },
                    setBooleanValue = { viewModel.checkedChanged(it) },
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
        composable(
            "configuration/{configurationId}/{scopeId}/integer",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            val viewModel: FragmentIntegerValueViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon =
                        {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
            ) { paddingValues ->
                IntegerValueView(
                    uiState = state,
                    popBackStack = { navController.popBackStack() },
                    revert = { viewModel.revertClick() },
                    save = { viewModel.saveClick() },
                    setIntegerValue = { viewModel.setIntegerValue(it) },
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
        composable(
            "configuration/{configurationId}/{scopeId}/string",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            val viewModel: FragmentStringValueViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon =
                        {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
            ) { paddingValues ->
                StringValueView(
                    state = state,
                    popBackStack = { navController.popBackStack() },
                    revert = { viewModel.revertClick() },
                    save = { viewModel.saveClick() },
                    setStringValue = { viewModel.setStringValue(it) },
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
        composable(
            "configuration/{configurationId}/{scopeId}/enum",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            val viewModel: FragmentEnumValueViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon =
                        {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
            ) { paddingValues ->
                EnumValueView(
                    state = state,
                    popBackStack = { navController.popBackStack() },
                    revert = { viewModel.revertClick() },
                    setEnumValue = { viewModel.saveClick(it) },
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
        composable(
            "oss",
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon =
                        {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
            ) { paddingValues ->
                OssView(modifier = Modifier.padding(paddingValues))
            }
        }
        composable(
            "help",
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon =
                        {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
            ) { paddingValues ->
                HelpView(modifier = Modifier.padding(paddingValues))
            }
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TogglesTheme {
                val navController: NavHostController = rememberNavController()
                val appState = rememberAppState(navController)
                Log.e("appState", "current destination: ${appState.currentDestination}")

                Navigation(navController = navController)
            }
        }
    }
}
