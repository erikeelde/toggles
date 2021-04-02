package se.eelde.toggles.dialogs.booleanvalue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izettle.wrench.dialogs.booleanvalue.FragmentBooleanValueViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BooleanValueView(viewModel: FragmentBooleanValueViewModel) {

    val uiState = viewModel.state.collectAsState()

    val scope = rememberCoroutineScope()

    uiState.value.let {

        Column {

            Text(it.title ?: "TODO()")

            Switch(checked = uiState.value.checked ?: false, onCheckedChange = { viewModel.checkedChanged(it) })

            Row {

                Button(modifier = Modifier.padding(8.dp), onClick = {
                    scope.launch {
                        viewModel.revertClick()
                    }
                }) {
                    Text("Revert")
                }

                Button(modifier = Modifier.padding(8.dp), onClick = {
                    scope.launch {
                        viewModel.saveClick(it.checked!!)
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
}