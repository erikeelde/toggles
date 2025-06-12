package se.eelde.toggles.applications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import kotlinx.coroutines.launch
import se.eelde.toggles.routes.Applications

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderBuilder<*>.applicationNavigations(
    navigateToConfigurations: (Long) -> Unit,
    navigateToApplications: () -> Unit,
    navigateToOss: () -> Unit,
    navigateToHelp: () -> Unit,
) {
    entry<Applications> {
        val viewModel = hiltViewModel<ApplicationViewModel>()
        val viewState = viewModel.state.collectAsState()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                TogglesDrawer(
                    drawerState,
                    navigateToApplications = { navigateToApplications() },
                    navigateToOss = { navigateToOss() },
                    navigateToHelp = { navigateToHelp() },
                )
            }
        ) {
            Scaffold(
                topBar = {
                    val scope = rememberCoroutineScope()

                    TopAppBar(
                        title = { Text("Applications") },
                        navigationIcon =
                        {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Filled.Menu, contentDescription = null)
                            }
                        }
                    )
                },
            ) { paddingValues ->
                ApplicationsView(
                    viewState = viewState.value,
                    modifier = Modifier.padding(paddingValues),
                    navigateToConfigurations = navigateToConfigurations
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
@Suppress("LongMethod")
fun TogglesDrawer(
    drawerState: DrawerState,
    navigateToApplications: () -> Unit,
    navigateToOss: () -> Unit,
    navigateToHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ModalNavigationDrawer(kotlin.Function0,androidx.compose.ui.Modifier,androidx.compose.material3.DrawerState,kotlin.Boolean,androidx.compose.ui.graphics.Color,kotlin.Function0)
    val scope = rememberCoroutineScope()
    val selectedItem = remember { mutableStateOf(0) }
    ModalDrawerSheet(modifier = modifier) {
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier.background(colorResource(id = se.eelde.toggles.composetheme.R.color.toggles_blue)),
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Application icon"
            )
        }
        Spacer(Modifier.height(12.dp))
        NavigationDrawerItem(
            icon = {
                Icon(
                    painterResource(id = se.eelde.toggles.composetheme.R.drawable.ic_settings_white_24dp),
                    contentDescription = null
                )
            },
            label = { Text(stringResource(id = R.string.applications)) },
            selected = 0 == selectedItem.value,
            onClick = {
                scope.launch { drawerState.close() }
                navigateToApplications()
                selectedItem.value = 0
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = {
                Icon(
                    painterResource(id = se.eelde.toggles.composetheme.R.drawable.ic_oss),
                    contentDescription = null
                )
            },
            label = { Text(stringResource(id = R.string.oss)) },
            selected = 1 == selectedItem.value,
            onClick = {
                scope.launch { drawerState.close() }
                navigateToOss()
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
                navigateToHelp()
                selectedItem.value = 2
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
