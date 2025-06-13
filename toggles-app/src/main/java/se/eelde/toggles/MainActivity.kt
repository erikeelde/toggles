package se.eelde.toggles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
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
    modifier: Modifier = Modifier,
) {
    val backStack = remember { mutableStateListOf<Any>(Applications) }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<BooleanConfiguration> { booleanConfiguration ->
                BooleanValueView(booleanConfiguration) { backStack.removeLastOrNull() }
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
                navigateToApplications = { backStack.add(Applications) },
                navigateToOss = { backStack.add(Oss) },
                navigateToHelp = { backStack.add(Help) },
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
                Navigation()
            }
        }
    }
}
