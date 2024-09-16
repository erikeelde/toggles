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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.applications.applicationNavigations
import se.eelde.toggles.booleanconfiguration.BooleanValueView
import se.eelde.toggles.composetheme.TogglesTheme
import se.eelde.toggles.configurations.configurationsNavigations
import se.eelde.toggles.dialogs.scope.ScopeValueView
import se.eelde.toggles.enumconfiguration.EnumValueView
import se.eelde.toggles.help.HelpView
import se.eelde.toggles.integerconfiguration.IntegerValueView
import se.eelde.toggles.oss.OssView
import se.eelde.toggles.routes.Applications
import se.eelde.toggles.routes.BooleanConfiguration
import se.eelde.toggles.routes.Configurations
import se.eelde.toggles.routes.Help
import se.eelde.toggles.routes.Oss
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
                navController.navigate("configuration/$configurationId/$scopeId/integer")
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
                navController.navigate("configuration/$configurationId/$scopeId/enum")
            },
            navigateToScopeView = { applicationId: Long ->
                navController.navigate("scopes/$applicationId")
            }
        ) { navController.popBackStack() }
        composable<BooleanConfiguration> { backStackEntry ->
            val booleanConfiguration: BooleanConfiguration = backStackEntry.toRoute()

            BooleanValueView(booleanConfiguration) { navController.popBackStack() }
        }
        composable(
            "scopes/{applicationId}",
            arguments = listOf(navArgument("applicationId") { type = NavType.LongType })
        ) {
            ScopeValueView { navController.popBackStack() }
        }
        composable(
            "configuration/{configurationId}/{scopeId}/integer",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            IntegerValueView { navController.popBackStack() }
        }
        composable<StringConfiguration> { backStackEntry ->
            val stringConfiguration: StringConfiguration = backStackEntry.toRoute()

            StringValueView(stringConfiguration) { navController.popBackStack() }
        }
        composable(
            "configuration/{configurationId}/{scopeId}/enum",
            arguments = listOf(
                navArgument("configurationId") { type = NavType.LongType },
                navArgument("scopeId") { type = NavType.LongType }
            )
        ) {
            EnumValueView { navController.popBackStack() }
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
