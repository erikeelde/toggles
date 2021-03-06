package se.eelde.toggles.dialogs.enumvalue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.izettle.wrench.dialogs.enumvalue.FragmentEnumValueViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EnumValueView(navController: NavController, viewModel: FragmentEnumValueViewModel) {

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
                LazyColumn {
                    uiState.value.configurationValues.forEach {
                        item {
                            ListItem(modifier = Modifier.clickable {
                                scope.launch {
                                    viewModel.saveClick(it.value.toString())
                                    navController.popBackStack()
                                }
                            }) {
                                Text(text = it.value.toString())
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(modifier = Modifier.padding(8.dp), onClick = {
                        scope.launch {
                            viewModel.revertClick()
                            navController.popBackStack()
                        }
                    }) {
                        Text("Revert")
                    }
                }
            }
        }
    }
}
