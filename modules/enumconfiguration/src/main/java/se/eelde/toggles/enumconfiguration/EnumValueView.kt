package se.eelde.toggles.enumconfiguration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
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
import se.eelde.toggles.composetheme.ToggleEditorDialog
import se.eelde.toggles.composetheme.rememberShowNavigationIconInExtraPane

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnumValueView(
    asDialog: Boolean,
    modifier: Modifier = Modifier,
    viewModel: EnumValueViewModel = hiltViewModel(),
    back: () -> Unit,
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val showNavigationIcon = rememberShowNavigationIconInExtraPane()

    if (asDialog) {
        ToggleEditorDialog(
            title = viewState.title.orEmpty(),
            modifier = modifier,
        ) {
            EnumValueView(
                state = viewState,
                setEnumValue = { viewModel.saveClick(it) },
                revert = { viewModel.revertClick() },
                popBackStack = { back() },
                asDialog = true,
            )
        }
    } else {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text(viewState.title.orEmpty()) },
                    navigationIcon = {
                        if (showNavigationIcon) {
                            IconButton(onClick = { back() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                )
            },
        ) { paddingValues ->
            EnumValueView(
                state = viewState,
                setEnumValue = { viewModel.saveClick(it) },
                revert = { viewModel.revertClick() },
                popBackStack = { back() },
                asDialog = false,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
@Suppress("LongParameterList", "DEPRECATION")
internal fun EnumValueView(
    state: ViewState,
    setEnumValue: suspend (String) -> Unit,
    revert: suspend () -> Unit,
    popBackStack: () -> Unit,
    asDialog: Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Surface(modifier = modifier.padding(16.dp)) {
        Column {
            LazyColumn(modifier = Modifier.weight(1f, fill = !asDialog)) {
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
                                if (state.expressiveList) {
                                    RadioButton(selected = selected, onClick = null)
                                } else if (selected) {
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
            @OptIn(ExperimentalMaterial3ExpressiveApi::class)
            ButtonGroup(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
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
