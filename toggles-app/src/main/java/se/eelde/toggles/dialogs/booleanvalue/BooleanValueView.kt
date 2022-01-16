package se.eelde.toggles.dialogs.booleanvalue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.izettle.wrench.dialogs.booleanvalue.FragmentBooleanValueViewModel
import kotlinx.coroutines.launch

@Composable
fun BooleanValueView(navController: NavController,
                     configurationId: Long,
                     scopeId: Long,
                     viewModel: FragmentBooleanValueViewModel = hiltViewModel()) {

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

                Switch(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(End),
                    checked = uiState.value.checked ?: false,
                    onCheckedChange = { viewModel.checkedChanged(it) })
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
                            it.checked?.let {
                                viewModel.saveClick(it)
                            }
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
