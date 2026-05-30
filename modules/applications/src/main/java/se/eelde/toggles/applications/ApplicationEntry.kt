package se.eelde.toggles.applications

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState // used in composable lambda
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import se.eelde.toggles.routes.Applications

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.applicationNavigations(
    navigateToConfigurations: (Long) -> Unit,
) {
    entry<Applications> {
        val viewModel = hiltViewModel<ApplicationViewModel>()
        val viewState = viewModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Applications") })
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
