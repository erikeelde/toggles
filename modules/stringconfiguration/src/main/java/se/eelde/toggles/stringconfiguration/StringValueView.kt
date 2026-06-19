package se.eelde.toggles.stringconfiguration

import android.annotation.SuppressLint
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import se.eelde.toggles.composetheme.ToggleEditorDialog
import se.eelde.toggles.composetheme.rememberShowNavigationIconInExtraPane
import se.eelde.toggles.composetheme.TogglesTheme
import se.eelde.toggles.routes.StringConfiguration

@Preview
@Composable
internal fun StringValueViewPreview() {
    TogglesTheme {
        StringValueView(
            viewState = ViewState(title = "The title", stringValue = "This is value"),
            setStringValue = {},
            save = {},
            revert = {},
            popBackStack = {},
            asDialog = false,
        )
    }
}

@SuppressLint("ComposeViewModelInjection")
@Composable
fun StringValueView(
    stringConfiguration: StringConfiguration,
    asDialog: Boolean,
    back: () -> Unit,
) {
    val viewModel: StringValueViewModel =
        hiltViewModel<StringValueViewModel, StringValueViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(stringConfiguration)
            }
        )

    val viewState by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    StringValueView(
        viewState = viewState,
        setStringValue = { viewModel.setStringValue(it) },
        save = { scope.launch { viewModel.saveClick() } },
        revert = { viewModel.revertClick() },
        popBackStack = back,
        asDialog = asDialog,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList", "LongMethod", "DEPRECATION")
internal fun StringValueView(
    viewState: ViewState,
    setStringValue: (String) -> Unit,
    save: suspend () -> Unit,
    revert: suspend () -> Unit,
    popBackStack: () -> Unit,
    asDialog: Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val showNavigationIcon = rememberShowNavigationIconInExtraPane()

    val body: @Composable ColumnScope.() -> Unit = {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            value = viewState.stringValue.orEmpty(),
            onValueChange = { setStringValue(it) },
        )
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        ButtonGroup(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Button(modifier = Modifier.padding(8.dp), onClick = {
                scope.launch {
                    revert()
                    popBackStack()
                }
            }) {
                Text("Revert")
            }

            Button(modifier = Modifier.padding(8.dp), onClick = {
                scope.launch {
                    save()
                    popBackStack()
                }
            }) {
                Text("Save")
            }
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
            Surface(modifier = Modifier.padding(paddingValues)) {
                Column { body() }
            }
        }
    }
}
