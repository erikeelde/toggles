package se.eelde.toggles.configurationlist

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cyclone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.configurationsNavigations(
    navController: NavController,
    back: () -> Unit,
) {
    composable(
        "configurations/{applicationId}",
        arguments = listOf(navArgument("applicationId") { type = NavType.LongType })
    ) {
        val viewModel: ConfigurationViewModel = hiltViewModel()
        val uiState = viewModel.state.collectAsStateWithLifecycle()

        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {}

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { uiState.value.application?.applicationLabel },
                    navigationIcon =
                    {
                        IconButton(onClick = { back() }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.restartApplication(uiState.value.application!!)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Cyclone,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            launcher.launch(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts(
                                        "package",
                                        uiState.value.application!!.packageName,
                                        null
                                    )
                                )
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            viewModel.deleteApplication(uiState.value.application!!)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            ConfigurationListView(
                navController = navController,
                uiState = uiState,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}
