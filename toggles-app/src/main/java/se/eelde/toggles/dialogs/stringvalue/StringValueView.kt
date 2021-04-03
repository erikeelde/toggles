package se.eelde.toggles.dialogs.stringvalue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.izettle.wrench.dialogs.stringvalue.FragmentStringValueViewModel
import kotlinx.coroutines.launch

@Composable
fun StringValueView(navController: NavController, viewModel: FragmentStringValueViewModel) {

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
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = it.stringValue ?: "",
                    onValueChange = { viewModel.setStringValue(it) },
                )
                Row {

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
