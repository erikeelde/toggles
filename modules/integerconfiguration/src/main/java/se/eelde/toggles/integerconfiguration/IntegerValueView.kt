package se.eelde.toggles.integerconfiguration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Preview
@Composable
internal fun IntegerValueViewPreview() {
    IntegerValueView(
        uiState = ViewState(
            title = "Integer value",
            integerValue = 5,
            saving = false,
            reverting = false
        ),
        popBackStack = {},
        revert = {},
        save = {},
        setIntegerValue = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegerValueView(
    viewModel: IntegerValueViewModel,
    modifier: Modifier = Modifier,
    back: () -> Unit,
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Integer configuration") },
                navigationIcon =
                {
                    IconButton(onClick = { back() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        IntegerValueView(
            uiState = viewState,
            popBackStack = { back() },
            revert = { viewModel.revertClick() },
            save = { viewModel.saveClick() },
            setIntegerValue = { viewModel.setIntegerValue(it) },
            modifier = modifier.padding(paddingValues),
        )
    }
}

@Composable
@Suppress("LongParameterList")
internal fun IntegerValueView(
    uiState: ViewState,
    popBackStack: () -> Unit,
    revert: suspend () -> Unit,
    save: suspend () -> Unit,
    setIntegerValue: (value: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Surface(modifier = modifier.padding(16.dp)) {
        Column {
            Text(
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.headlineMedium,
                text = uiState.title ?: ""
            )
            OutlinedTextField(
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth(),
                value = if (uiState.integerValue != null) uiState.integerValue.toString() else "",
                onValueChange = {
                    try {
                        setIntegerValue(it.toInt())
                    } catch (_: NumberFormatException) { }
                },
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
