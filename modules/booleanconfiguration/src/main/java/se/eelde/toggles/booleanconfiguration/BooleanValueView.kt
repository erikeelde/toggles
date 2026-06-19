package se.eelde.toggles.booleanconfiguration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import se.eelde.toggles.composetheme.ToggleEditorDialog
import se.eelde.toggles.composetheme.rememberShowNavigationIconInExtraPane
import se.eelde.toggles.routes.BooleanConfiguration

@Composable
fun BooleanValueView(
    booleanConfiguration: BooleanConfiguration,
    asDialog: Boolean,
    viewModel: BooleanValueViewModel =
        hiltViewModel<BooleanValueViewModel, BooleanValueViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(booleanConfiguration)
            }
        ),
    back: () -> Unit,
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    BooleanValueView(
        viewState = viewState,
        save = { scope.launch { viewModel.saveClick() } },
        revert = { scope.launch { viewModel.revertClick() } },
        checkedChanged = { viewModel.checkedChanged(it) },
        popBackStack = back,
        asDialog = asDialog,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongParameterList", "LongMethod", "DEPRECATION")
fun BooleanValueView(
    viewState: ViewState,
    save: () -> Unit,
    revert: () -> Unit,
    checkedChanged: (Boolean) -> Unit,
    popBackStack: () -> Unit,
    asDialog: Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val showNavigationIcon = rememberShowNavigationIconInExtraPane()

    val body: @Composable ColumnScope.() -> Unit = {
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        ButtonGroup(modifier = Modifier.padding(8.dp)) {
            ToggleButton(
                checked = viewState.checked == false,
                onCheckedChange = { checkedChanged(false) },
            ) { Text("Off") }
            ToggleButton(
                checked = viewState.checked == true,
                onCheckedChange = { checkedChanged(true) },
            ) { Text("On") }
        }

        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        ButtonGroup(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Button(onClick = {
                scope.launch {
                    revert()
                    popBackStack()
                }
            }) { Text("Revert") }
            Button(onClick = {
                scope.launch {
                    save()
                    popBackStack()
                }
            }) { Text("Save") }
        }
    }

    if (asDialog) {
        ToggleEditorDialog(
            title = viewState.title.orEmpty(),
            modifier = modifier,
            content = body,
        )
    } else {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text(viewState.title.orEmpty()) },
                    navigationIcon = {
                        if (showNavigationIcon) {
                            IconButton(onClick = { popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                )
            },
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Column { body() }
            }
        }
    }
}
