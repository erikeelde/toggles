package se.eelde.toggles.dialogs.scope

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import se.eelde.toggles.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScopeValueView(navController: NavController, viewModel: ScopeFragmentViewModel) {
    val uiState = viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    uiState.value.let {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    text = stringResource(id = R.string.select_scope)
                )
                LazyColumn {
                    uiState.value.scopes.forEach {
                        0
                        item {
                            ListItem(
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        viewModel.selectScope(it)
                                        navController.popBackStack()
                                    }
                                },
                                headlineText = { Text(text = it.name) }
                            )
                        }
                    }
                }
                Row {
                    Button(modifier = Modifier.padding(8.dp), onClick = {
                        TODO()
                    }) {
                        Text("Delete")
                    }
                    Button(modifier = Modifier.padding(8.dp), onClick = {
                        TODO()
                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
