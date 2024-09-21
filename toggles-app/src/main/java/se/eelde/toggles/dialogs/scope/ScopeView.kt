package se.eelde.toggles.dialogs.scope

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.eelde.toggles.R
import se.eelde.toggles.database.WrenchScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScopeValueView(
    viewModel: ScopeViewModel,
    modifier: Modifier = Modifier,
    back: () -> Unit
) {
    val uiState = viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scopes") },
                navigationIcon =
                {
                    IconButton(onClick = { back() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
    ) { paddingValues ->
        ScopeValueView(
            viewState = uiState.value,
            selectScope = { scope -> viewModel.selectScope(scope) },
            deleteScope = { scope -> viewModel.removeScope(scope) },
            createScope = { viewModel.createScope(it) },
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Suppress("LongMethod")
@Composable
internal fun ScopeValueView(
    viewState: ViewState,
    selectScope: (scope: WrenchScope) -> Unit,
    deleteScope: (scope: WrenchScope) -> Unit,
    createScope: (scopeName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column {
            Text(
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(id = R.string.scope_information)
            )
            LazyColumn {
                viewState.scopes.forEach { scope ->
                    item {
                        val selected = scope.id == viewState.selectedScope?.id
                        ListItem(
                            modifier = Modifier
                                .selectable(selected = selected) {
                                    selectScope(scope)
                                },
                            leadingContent = {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Link,
                                        contentDescription = null
                                    )
                                }
                            },
                            headlineContent = { Text(text = scope.name) }
                        )
                    }
                }
            }

            val showAddScopeView = rememberSaveable { mutableStateOf(false) }
            val showDeleteScopeView = rememberSaveable { mutableStateOf(false) }

            Row {
                Button(
                    modifier = Modifier.padding(16.dp),
                    onClick = { showAddScopeView.value = true }
                ) {
                    Text("Add")
                }
                OutlinedButton(
                    modifier = Modifier.padding(16.dp),
                    enabled = viewState.scopes.size > 1,
                    onClick = { showDeleteScopeView.value = true }
                ) {
                    Text("Delete")
                }
            }

            if (showAddScopeView.value) {
                AddScopeView(
                    addScope = {
                        createScope(it)
                        showAddScopeView.value = false
                    },
                    dismiss = { showAddScopeView.value = false }
                )
            }
            if (showDeleteScopeView.value) {
                DeleteScopeView(
                    scope = viewState.selectedScope!!,
                    deleteScope = { scope: WrenchScope ->
                        deleteScope(scope)
                        showDeleteScopeView.value = false
                    },
                    dismiss = { showDeleteScopeView.value = false }
                )
            }
        }
    }
}

@Composable
fun AddScopeView(
    addScope: (String) -> Unit,
    dismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scopeName = rememberSaveable { mutableStateOf("") }
    Dialog(onDismissRequest = { dismiss() }) {
        Card(modifier = modifier) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    text = stringResource(id = R.string.scope_add_information)
                )
                TextField(
                    value = scopeName.value,
                    onValueChange = { scopeName.value = it }
                )
                Button(onClick = { addScope(scopeName.value) }) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun DeleteScopeView(
    scope: WrenchScope,
    deleteScope: (WrenchScope) -> Unit,
    dismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = { dismiss() }) {
        Card(modifier = modifier) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Are you sure you want to delete the scope: ${scope.name}")
                Button(onClick = { deleteScope(scope) }) {
                    Text("Delete")
                }
            }
        }
    }
}
