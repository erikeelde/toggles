package se.eelde.toggles.dialogs.scope

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.izettle.wrench.dialogs.scope.ScopeFragmentViewModel
import kotlinx.coroutines.launch
import se.eelde.toggles.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScopeValueView(navController: NavController, viewModel: ScopeFragmentViewModel) {

    val uiState = viewModel.state.collectAsState()

    val scope = rememberCoroutineScope()

    uiState.value.let {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column {

                Text(
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.h6,
                    text = stringResource(id = R.string.select_scope)
                )
                LazyColumn {
                    uiState.value.scopes.forEach {0
                        item {
                            ListItem(modifier = Modifier.clickable {
                                scope.launch {
                                    viewModel.selectScope(it)
                                    navController.popBackStack()
                                }
                            }) {
                                Text(text = it.name)
                            }
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
