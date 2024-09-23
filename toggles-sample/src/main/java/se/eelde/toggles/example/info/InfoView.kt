package se.eelde.toggles.example.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InfoView(viewModel: TogglesInfoViewModel, modifier: Modifier = Modifier) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle(InfoViewState())

    InfoView(viewState = viewState, modifier)
}

@Composable
private fun InfoView(viewState: InfoViewState, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.verticalScroll(scrollState)) {
        ListItem(headlineContent = {
            Text(text = "Configurations")
        })

        viewState.configurations.forEach {
            ListItem(headlineContent = {
                Text(text = it.key)
            }, supportingContent = {
                Text(text = it.type)
            })

            it.values.forEach {
                ListItem(modifier = Modifier.padding(start = 16.dp), headlineContent = {
                    Text(text = "${it.id}")
                }, supportingContent = {
                    Text(text = it.toString())
                })
            }
        }
    }
}
