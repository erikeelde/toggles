package se.eelde.toggles.enumconfiguration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnumValueView(
    modifier: Modifier = Modifier,
    viewModel: EnumValueViewModel = hiltViewModel(),
    back: () -> Unit,
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enum configuration") },
                navigationIcon =
                {
                    IconButton(onClick = { back() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        EnumValueView(
            state = viewState,
            setEnumValue = { viewModel.saveClick(it) },
            revert = { viewModel.revertClick() },
            popBackStack = { back() },
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
internal fun EnumValueView(
    state: ViewState,
    setEnumValue: suspend (String) -> Unit,
    revert: suspend () -> Unit,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Surface(modifier = modifier.padding(16.dp)) {
        Column {
            Text(
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.headlineMedium,
                text = state.title.orEmpty()
            )
            LazyColumn {
                state.configurationValues.forEach { togglesPredefinedConfigurationValue ->
                    item {
                        val selected =
                            togglesPredefinedConfigurationValue.value == state.selectedConfigurationValue?.value
                        ListItem(
                            modifier = Modifier.selectable(
                                selected = selected
                            ) {
                                scope.launch {
                                    setEnumValue(togglesPredefinedConfigurationValue.value.toString())
                                }
                            },
                            headlineContent = { Text(text = togglesPredefinedConfigurationValue.value.toString()) },
                            leadingContent = {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Link,
                                        contentDescription = null
                                    )
                                }
                            }
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
