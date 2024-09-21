package se.eelde.toggles.booleanconfiguration

import android.annotation.SuppressLint
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
import se.eelde.toggles.routes.BooleanConfiguration

@SuppressLint("ComposeViewModelInjection")
@Composable
fun BooleanValueView(
    booleanConfiguration: BooleanConfiguration,
    back: () -> Unit,
) {
    val viewModel: BooleanValueViewModel =
        hiltViewModel<BooleanValueViewModel, BooleanValueViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(booleanConfiguration)
            }
        )

    val viewState by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    BooleanValueView(
        viewState = viewState,
        save = { scope.launch { viewModel.saveClick() } },
        revert = { scope.launch { viewModel.revertClick() } },
        checkedChanged = { viewModel.checkedChanged(it) },
        popBackStack = back,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongParameterList", "LongMethod")
fun BooleanValueView(
    viewState: ViewState,
    save: () -> Unit,
    revert: () -> Unit,
    checkedChanged: (Boolean) -> Unit,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boolean configuration") },
                navigationIcon =
                {
                    IconButton(onClick = { popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        val scope = rememberCoroutineScope()
        Surface(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    text = viewState.title ?: ""
                )

                Switch(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(End),
                    checked = viewState.checked ?: false,
                    onCheckedChange = {
                        checkedChanged(it)
                    }
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
