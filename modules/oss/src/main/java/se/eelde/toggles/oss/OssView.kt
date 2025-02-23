@file:OptIn(ExperimentalFoundationApi::class)

package se.eelde.toggles.oss

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
fun OssView(modifier: Modifier = Modifier) {
    OssView(viewModel = hiltViewModel(), modifier = modifier)
}

@Composable
fun OssView(viewModel: OssProjectViewModel, modifier: Modifier = Modifier) {
    val uiState = viewModel.uiState.collectAsState()
    Surface(modifier = modifier) {
        when (val state = uiState.value.loadingState) {
            is State.Failed -> ErrorView(stringResource(id = R.string.error))
            is State.Loading -> LoadingView(stringResource(id = R.string.loading))
            is State.Success -> OssView(state.data)
        }
    }
}

@Composable
fun OssView(artifacts: ImmutableList<ViewData>, modifier: Modifier = Modifier) {
    val licenses: SnapshotStateList<License> = remember { mutableStateListOf() }
    var alertTitle by remember { mutableStateOf("") }
    LicenseSelector(alertTitle, licenses.toPersistentList()) {
        licenses.clear()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp),
                    text = stringResource(id = R.string.oss_description)

                )
            }
        }

        val grouped: Map<String, List<ViewData>> =
            artifacts.groupBy { it.title[0].uppercaseChar().toString() }
        grouped.forEach { (title, list) ->
            stickyHeader {
                CharacterHeader(title)
            }
            items(list) { artifact ->
                ListItem(
                    headlineContent = {
                        Text(text = artifact.title)
                    },
                    modifier = Modifier.clickable {
                        alertTitle = artifact.title
                        licenses.addAll(artifact.licenses)
                    }
                )
            }
        }
    }
}

@Composable
fun CharacterHeader(initial: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 4.dp
        ),
        text = initial
    )
}

@Preview(showSystemUi = true)
@Composable
internal fun LicenseSelectorPreview() {
    Column(Modifier.fillMaxSize()) {
        LicenseSelector("Licenses", persistentListOf(License("aaa", "http://google.se"))) {
        }
    }
}

@Composable
fun LicenseSelector(title: String, licenses: ImmutableList<License>, close: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    if (licenses.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                close()
            },
            title = {
                Text(text = title)
            },
            text = {
                Column {
                    licenses.forEach { license ->
                        ListItem(
                            headlineContent = {
                                Text(text = license.title)
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Filled.Link,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable {
                                uriHandler.openUri(license.url)
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = close
                ) {
                    Text(stringResource(id = R.string.close))
                }
            },
        )
    }
}
