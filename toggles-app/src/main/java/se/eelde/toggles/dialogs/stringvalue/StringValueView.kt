package se.eelde.toggles.dialogs.stringvalue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
