package se.eelde.toggles.booleanconfiguration

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooleanValueView(
    modifier: Modifier = Modifier,
    viewModel: FragmentBooleanValueViewModel = hiltViewModel(),
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
        BooleanValueView(
            uiState = viewState,
            popBackStack = { back() },
            revert = { viewModel.revertClick() },
            save = { viewModel.saveClick() },
            setBooleanValue = { viewModel.checkedChanged(it) },
            modifier = modifier.padding(paddingValues),
        )
    }
}

@Composable
@Suppress("LongParameterList")
internal fun BooleanValueView(
    uiState: ViewState,
    popBackStack: () -> Unit,
    setBooleanValue: (Boolean) -> Unit,
    revert: suspend () -> Unit,
    save: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Surface(modifier = modifier) {
        Column {
            Text(
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.headlineMedium,
                text = uiState.title ?: ""
            )

            Switch(
                modifier = Modifier
                    .padding(8.dp)
                    .align(End),
                checked = uiState.checked ?: false,
                onCheckedChange = { setBooleanValue(it) }
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
