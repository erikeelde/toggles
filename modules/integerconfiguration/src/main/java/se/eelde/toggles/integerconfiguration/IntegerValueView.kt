package se.eelde.toggles.integerconfiguration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Preview
@Composable
fun IntegerValueViewPreview() {
    IntegerValueView(
        uiState = ViewState(title = "Integer value", integerValue = 5, saving = false, reverting = false),
        popBackStack = {},
        revert = {},
        save = {},
        setIntegerValue = {},
    )
}

@Composable
@Suppress("LongParameterList")
fun IntegerValueView(
    uiState: ViewState,
    popBackStack: () -> Unit,
    revert: suspend () -> Unit,
    save: suspend () -> Unit,
    setIntegerValue: (value: Int) -> Unit,
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
            OutlinedTextField(
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth(),
                value = if (uiState.integerValue != null) uiState.integerValue.toString() else "",
                onValueChange = { setIntegerValue(it.toInt()) },
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
