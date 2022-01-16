package se.eelde.toggles.dialogs.integervalue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.izettle.wrench.dialogs.integervalue.FragmentIntegerValueViewModel
import kotlinx.coroutines.launch

@Composable
fun IntegerValueView(
    navController: NavController,
    configurationId: Long,
    scopeId: Long,
    viewModel: FragmentIntegerValueViewModel = hiltViewModel()
) {

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
