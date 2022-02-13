package se.eelde.toggles.dialogs.stringvalue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import se.eelde.toggles.composetheme.TogglesTheme

@Preview
@Composable
fun StringValueViewPreview() {
    TogglesTheme {
        StringValueView(
            title = "The title",
            stringValue = "This is value",
            setStringValue = {},
            save = {},
            revert = {}
        ) {

        }
    }
}

@Composable
fun StringValueView(
    navController: NavController,
    viewModel: FragmentStringValueViewModel = hiltViewModel()
) {
    val uiState = viewModel.state.collectAsState()

    uiState.value.let { viewState ->
        StringValueView(
            title = viewState.title,
            stringValue = viewState.stringValue,
            setStringValue = { viewModel.setStringValue(it) },
            save = { viewModel.saveClick() },
            revert = { viewModel.revertClick() },
            navigateBack = { navController.popBackStack() }
        )
    }
}

@Composable
fun StringValueView(
    title: String?,
    stringValue: String?,
    setStringValue: (String) -> Unit,
    save: suspend () -> Unit,
    revert: suspend () -> Unit,
    navigateBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.padding(16.dp)) {
        Column {
            Text(
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.headlineMedium,
                text = title ?: "TODO()"
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = stringValue ?: "",
                onValueChange = { setStringValue(it) },
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {

                Button(modifier = Modifier.padding(8.dp), onClick = {
                    scope.launch {
                        revert()
                        navigateBack()
                    }
                }) {
                    Text("Revert")
                }

                Button(modifier = Modifier.padding(8.dp), onClick = {
                    scope.launch {
                        save()
                        navigateBack()
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
}
