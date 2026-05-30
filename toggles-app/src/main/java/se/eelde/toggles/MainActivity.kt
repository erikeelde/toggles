package se.eelde.toggles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
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

private enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: NavKey,
) {
    APPLICATIONS("Applications", Icons.Filled.Apps, Applications),
    OSS("Licenses", Icons.Filled.Info, Oss),
    HELP("Help", Icons.AutoMirrored.Filled.HelpOutline, Help),
}

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
private fun TogglesApp() {
    val backStack = rememberNavBackStack(Applications)
    var selected by remember { mutableStateOf(TopLevelDestination.APPLICATIONS) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = selected == destination,
                    onClick = {
                        selected = destination
                        backStack.add(destination.route)
                    },
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = { Text(destination.label) },
                )
            }
        }
    ) {
        Navigation(backStack = backStack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun Navigation(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<BooleanConfiguration> { booleanConfiguration ->
                BooleanValueView(
                    booleanConfiguration = booleanConfiguration,
                    back = { backStack.removeLastOrNull() }
                )
            }

            entry<Scope> { scope ->
                ScopeValueView(
                    viewModel = hiltViewModel<ScopeViewModel, ScopeViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(scope)
                        }
                    )
                ) { backStack.removeLastOrNull() }
            }
            entry<IntegerConfiguration> { integerConfiguration ->
                IntegerValueView(
                    viewModel = hiltViewModel<IntegerValueViewModel, IntegerValueViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(integerConfiguration)
                        }
                    ),
                ) { backStack.removeLastOrNull() }
            }
            entry<StringConfiguration> { stringConfiguration ->
                StringValueView(stringConfiguration) { backStack.removeLastOrNull() }
            }
            entry<EnumConfiguration> { enumConfiguration ->
                EnumValueView(
                    viewModel = hiltViewModel<EnumValueViewModel, EnumValueViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(enumConfiguration)
                        }
                    )
                ) { backStack.removeLastOrNull() }
            }
            entry<Oss> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("") },
                            navigationIcon =
                            {
                                IconButton(onClick = { backStack.removeLastOrNull() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            entry<Help> {
                HelpView { backStack.removeLastOrNull() }
            }

            applicationNavigations(
                navigateToConfigurations = { applicationId ->
                    backStack.add(Configurations(applicationId))
                },
            )

            configurationsNavigations(
                navigateToBooleanConfiguration = { scopeId: Long, configurationId: Long ->
                    backStack.add(BooleanConfiguration(configurationId, scopeId))
                },
                navigateToIntegerConfiguration = { scopeId: Long, configurationId: Long ->
                    backStack.add(IntegerConfiguration(configurationId, scopeId))
                },
                navigateToStringConfiguration = { scopeId: Long, configurationId: Long ->
                    backStack.add(
                        StringConfiguration(
                            configurationId = configurationId,
                            scopeId = scopeId
                        )
                    )
                },
                navigateToEnumConfiguration = { scopeId: Long, configurationId: Long ->
                    backStack.add(EnumConfiguration(configurationId, scopeId))
                },
                navigateToScopeView = { applicationId: Long ->
                    backStack.add(Scope(applicationId))
                }
            ) { backStack.removeLastOrNull() }
        }
    )
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TogglesTheme {
                TogglesApp()
            }
        }
    }
}
