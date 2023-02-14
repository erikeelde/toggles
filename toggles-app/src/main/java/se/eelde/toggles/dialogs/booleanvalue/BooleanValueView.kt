package se.eelde.toggles.dialogs.booleanvalue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
internal fun BooleanValueView(
    uiState: ViewState,
    popBackStack: () -> Unit,
    setBooleanValue: (Boolean) -> Unit,
    revert: suspend () -> Unit,
    save: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.padding(16.dp)) {
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
