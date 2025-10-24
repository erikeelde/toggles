package se.eelde.toggles.configurations

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Cyclone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import se.eelde.toggles.routes.Configurations

@Suppress("LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.configurationsNavigations(
    navigateToBooleanConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToIntegerConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToStringConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToEnumConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToScopeView: (Long) -> Unit,
    back: () -> Unit,
) {
    entry<Configurations> { configurations ->
        val viewModel: ConfigurationViewModel =
            hiltViewModel<ConfigurationViewModel, ConfigurationViewModel.Factory>(
                creationCallback = { factory -> factory.create(applicationId = configurations.applicationId) }
            )

        val uiState = viewModel.state.collectAsStateWithLifecycle()

        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {}

        val query = viewModel.getQuery().collectAsState().value
        var searching = query.isNotEmpty()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        SearchBar(
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = query,
                                    onQueryChange = {
                                        viewModel.setQuery(it)
                                    },
                                    onSearch = {},
                                    expanded = false,
                                    onExpandedChange = { },
                                    placeholder = { Text("Search") },
                                    trailingIcon = {
                                        if (searching) {
                                            IconButton(onClick = {
                                                viewModel.setQuery("")
                                                searching = false
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    },
                                )
                            },
                            expanded = false,
                            onExpandedChange = { },
                            content = { },
                        )
                    },
                    navigationIcon =
                    {
                        IconButton(onClick = { back() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        var showMenu by rememberSaveable { mutableStateOf(false) }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Start application") },
                                onClick = { viewModel.restartApplication(uiState.value.application!!) },
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
                                                uiState.value.application!!.packageName,
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
                                onClick = { navigateToScopeView(uiState.value.application!!.id) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.List,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = { viewModel.deleteApplication(uiState.value.application!!) },
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
    }
}
