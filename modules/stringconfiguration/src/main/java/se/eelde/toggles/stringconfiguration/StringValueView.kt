package se.eelde.toggles.stringconfiguration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import se.eelde.toggles.composetheme.TogglesTheme

@Preview
@Composable
fun StringValueViewPreview() {
    TogglesTheme {
        StringValueView(
            state = ViewState(title = "The title", stringValue = "This is value"),
            setStringValue = {},
            save = {},
            revert = {},
            popBackStack = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringValueView(
    modifier: Modifier = Modifier,
    viewModel: FragmentStringValueViewModel = hiltViewModel(),
    back: () -> Unit,
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
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
        StringValueView(
            state = viewState,
            popBackStack = { back() },
            revert = { viewModel.revertClick() },
            save = { viewModel.saveClick() },
            setStringValue = { viewModel.setStringValue(it) },
            modifier = modifier.padding(paddingValues),
        )
    }
}

@Composable
@Suppress("LongParameterList")
internal fun StringValueView(
    state: ViewState,
    setStringValue: (String) -> Unit,
    save: suspend () -> Unit,
    revert: suspend () -> Unit,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Surface(modifier = modifier) {
        Column {
            Text(
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.headlineMedium,
                text = state.title ?: ""
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = state.stringValue ?: "",
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
