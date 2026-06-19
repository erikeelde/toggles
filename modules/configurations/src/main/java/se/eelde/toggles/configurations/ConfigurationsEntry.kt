package se.eelde.toggles.configurations

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Cyclone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import se.eelde.toggles.routes.Configurations

@Suppress("LongMethod", "LongParameterList")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
fun EntryProviderScope<NavKey>.configurationsNavigations(
    navigateToBooleanConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToIntegerConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToStringConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToEnumConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToScopeView: (Long) -> Unit,
    back: () -> Unit,
) {
    entry<Configurations>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { configurations ->
        val viewModel: ConfigurationViewModel =
            hiltViewModel<ConfigurationViewModel, ConfigurationViewModel.Factory>(
                creationCallback = { factory -> factory.create(applicationId = configurations.applicationId) }
            )

        val uiState = viewModel.state.collectAsStateWithLifecycle()

        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {}

        val listDetailScope = LocalListDetailSceneScope.current
        val showNavigationIcon = listDetailScope == null ||
            listDetailScope.scaffoldTransitionScope.scaffoldStateTransition.targetState.secondary == PaneAdaptedValue.Hidden

        val scope = rememberCoroutineScope()
        val searchBarState = rememberSearchBarState()
        val initialQuery by viewModel.getQuery().collectAsStateWithLifecycle()
        val textFieldState = rememberTextFieldState(initialQuery)

        LaunchedEffect(textFieldState) {
            snapshotFlow { textFieldState.text.toString() }
                .collect { viewModel.setQuery(it) }
        }

        val inputField = @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
                placeholder = { Text("Search configurations") },
                leadingIcon = {
                    if (searchBarState.currentValue == SearchBarValue.Expanded) {
                        IconButton(onClick = { scope.launch { searchBarState.animateToCollapsed() } }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Collapse search"
                            )
                        }
                    } else {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (textFieldState.text.isNotEmpty()) {
                        IconButton(onClick = { textFieldState.clearText() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
            )
        }

        Scaffold(
            topBar = {
                AppBarWithSearch(
                    state = searchBarState,
                    inputField = inputField,
                    navigationIcon = if (showNavigationIcon) {
                        {
                            IconButton(onClick = { back() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    } else null,
                    actions = {
                        var showMenu by rememberSaveable { mutableStateOf(false) }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Start application") },
                                onClick = { viewModel.restartApplication(requireNotNull(uiState.value.application)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Cyclone,
                                        contentDescription = null
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Appinfo") },
                                onClick = {
                                    launcher.launch(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts(
                                                "package",
                                                requireNotNull(uiState.value.application).packageName,
                                                null
                                            )
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Scopes") },
                                onClick = { navigateToScopeView(requireNotNull(uiState.value.application).id) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.List,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = { viewModel.deleteApplication(requireNotNull(uiState.value.application)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            ConfigurationListView(
                navigateToBooleanConfiguration = navigateToBooleanConfiguration,
                navigateToIntegerConfiguration = navigateToIntegerConfiguration,
                navigateToStringConfiguration = navigateToStringConfiguration,
                navigateToEnumConfiguration = navigateToEnumConfiguration,
                uiState = uiState,
                modifier = Modifier.padding(paddingValues),
            )
        }

        // Expanded full-screen search surface: shows the live-filtered results while typing.
        // uiState.configurations is already filtered by the query in the ViewModel.
        ExpandedFullScreenSearchBar(
            state = searchBarState,
            inputField = inputField,
        ) {
            ConfigurationListView(
                navigateToBooleanConfiguration = navigateToBooleanConfiguration,
                navigateToIntegerConfiguration = navigateToIntegerConfiguration,
                navigateToStringConfiguration = navigateToStringConfiguration,
                navigateToEnumConfiguration = navigateToEnumConfiguration,
                uiState = uiState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
