package se.eelde.toggles.enumconfiguration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun EnumValueView(
    state: ViewState,
    setEnumValue: suspend (String) -> Unit,
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
            LazyColumn {
                state.configurationValues.forEach { wrenchPredefinedConfigurationValue ->
                    item {
                        ListItem(
                            modifier = Modifier.clickable {
                                scope.launch {
                                    setEnumValue(wrenchPredefinedConfigurationValue.value.toString())
                                    popBackStack()
                                }
                            },
                            headlineContent = { Text(text = wrenchPredefinedConfigurationValue.value.toString()) }
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(modifier = Modifier.padding(8.dp), onClick = {
                    scope.launch {
                        revert()
                        popBackStack()
                    }
                }) {
                    Text("Revert")
                }
            }
        }
    }
}
