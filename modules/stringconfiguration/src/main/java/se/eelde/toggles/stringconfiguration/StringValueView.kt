package se.eelde.toggles.stringconfiguration

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import se.eelde.toggles.composetheme.TogglesTheme
import se.eelde.toggles.routes.StringConfiguration

@Preview
@Composable
private fun StringValueViewPreview() {
    TogglesTheme {
        StringValueView(
            viewState = ViewState(title = "The title", stringValue = "This is value"),
            setStringValue = {},
            save = {},
            revert = {},
            popBackStack = {},
        )
    }
}

@SuppressLint("ComposeViewModelInjection")
@Composable
fun StringValueView(
    stringConfiguration: StringConfiguration,
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
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
internal fun StringValueView(
    viewState: ViewState,
    setStringValue: (String) -> Unit,
    save: suspend () -> Unit,
    revert: suspend () -> Unit,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("String configuration") },
                navigationIcon =
                {
                    IconButton(onClick = { popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        val scope = rememberCoroutineScope()

        Surface(modifier = modifier.padding(paddingValues)) {
            Column {
                Text(
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    text = viewState.title ?: ""
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = viewState.stringValue ?: "",
                    onValueChange = { setStringValue(it) },
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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
        }
    }
}
