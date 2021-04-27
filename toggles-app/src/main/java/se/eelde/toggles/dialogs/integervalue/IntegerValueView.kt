package se.eelde.toggles.dialogs.integervalue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.izettle.wrench.dialogs.integervalue.FragmentIntegerValueViewModel
import kotlinx.coroutines.launch

@Composable
fun IntegerValueView(navController: NavController, viewModel: FragmentIntegerValueViewModel) {

    val uiState = viewModel.state.collectAsState()

    val scope = rememberCoroutineScope()

    uiState.value.let {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.h6,
                    text = it.title ?: "TODO()"
                )
                OutlinedTextField(
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = if (it.integerValue != null) it.integerValue.toString() else "",
                    onValueChange = { viewModel.setIntegerValue(it.toInt()) },
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(modifier = Modifier.padding(8.dp), onClick = {
                        scope.launch {
                            viewModel.revertClick()
                            navController.popBackStack()
                        }
                    }) {
                        Text("Revert")
                    }

                    Button(modifier = Modifier.padding(8.dp), onClick = {
                        scope.launch {
                            viewModel.saveClick()
                            navController.popBackStack()
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
