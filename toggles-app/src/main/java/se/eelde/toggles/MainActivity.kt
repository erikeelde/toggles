package se.eelde.toggles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.applications.applicationNavigations
import se.eelde.toggles.booleanconfiguration.BooleanValueView
import se.eelde.toggles.composetheme.TogglesTheme
import se.eelde.toggles.configurations.configurationsNavigations
import se.eelde.toggles.dialogs.scope.ScopeValueView
import se.eelde.toggles.dialogs.scope.ScopeViewModel
import se.eelde.toggles.enumconfiguration.EnumValueView
import se.eelde.toggles.enumconfiguration.EnumValueViewModel
import se.eelde.toggles.help.HelpView
import se.eelde.toggles.integerconfiguration.IntegerValueView
import se.eelde.toggles.integerconfiguration.IntegerValueViewModel
import se.eelde.toggles.oss.OssView
import se.eelde.toggles.routes.Applications
import se.eelde.toggles.routes.BooleanConfiguration
import se.eelde.toggles.routes.Configurations
import se.eelde.toggles.routes.EnumConfiguration
import se.eelde.toggles.routes.Help
import se.eelde.toggles.routes.IntegerConfiguration
import se.eelde.toggles.routes.Oss
import se.eelde.toggles.routes.Scope
import se.eelde.toggles.routes.StringConfiguration
import se.eelde.toggles.stringconfiguration.StringValueView

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun Navigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Applications,
        modifier = modifier
    ) {
        applicationNavigations(
            navigateToConfigurations = { applicationId ->
                navController.navigate(Configurations(applicationId))
            },
            navigateToApplications = { navController.navigate(Applications) },
            navigateToOss = { navController.navigate(Oss) },
            navigateToHelp = { navController.navigate(Help) },
        )
        configurationsNavigations(
            navigateToBooleanConfiguration = { scopeId: Long, configurationId: Long ->
                navController.navigate(BooleanConfiguration(configurationId, scopeId))
            },
            navigateToIntegerConfiguration = { scopeId: Long, configurationId: Long ->
                navController.navigate(IntegerConfiguration(configurationId, scopeId))
            },
            navigateToStringConfiguration = { scopeId: Long, configurationId: Long ->
                navController.navigate(
                    StringConfiguration(
                        configurationId = configurationId,
                        scopeId = scopeId
                    )
                )
            },
            navigateToEnumConfiguration = { scopeId: Long, configurationId: Long ->
                navController.navigate(EnumConfiguration(configurationId, scopeId))
            },
            navigateToScopeView = { applicationId: Long ->
                navController.navigate(Scope(applicationId))
            }
        ) { navController.popBackStack() }
        composable<BooleanConfiguration> { backStackEntry ->
            val booleanConfiguration: BooleanConfiguration = backStackEntry.toRoute()

            BooleanValueView(booleanConfiguration) { navController.popBackStack() }
        }
        composable<Scope> { backStackEntry ->
            val scope: Scope = backStackEntry.toRoute()

            ScopeValueView(
                viewModel = hiltViewModel<ScopeViewModel, ScopeViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(scope)
                    }
                )
            ) { navController.popBackStack() }
        }
        composable<IntegerConfiguration> { backStackEntry ->
            val integerConfiguration: IntegerConfiguration = backStackEntry.toRoute()
            IntegerValueView(
                viewModel = hiltViewModel<IntegerValueViewModel, IntegerValueViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(integerConfiguration)
                    }
                ),
            ) { navController.popBackStack() }
        }
        composable<StringConfiguration> { backStackEntry ->
            val stringConfiguration: StringConfiguration = backStackEntry.toRoute()

            StringValueView(stringConfiguration) { navController.popBackStack() }
        }
        composable<EnumConfiguration> { backStackEntry ->
            val enumConfiguration: EnumConfiguration = backStackEntry.toRoute()

            EnumValueView(
                viewModel = hiltViewModel<EnumValueViewModel, EnumValueViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(enumConfiguration)
                    }
                )
            ) { navController.popBackStack() }
        }
        composable<Oss> {
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
        composable<Help> {
            HelpView { navController.popBackStack() }
        }
    }
}

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
