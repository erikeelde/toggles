package se.eelde.toggles.configurationlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cyclone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import se.eelde.toggles.composetheme.AppBarState

fun NavGraphBuilder.configurationsNavigations(
    navController: NavController,
    onComposing: (AppBarState) -> Unit,
) {
    composable(
        "configurations/{applicationId}",
        arguments = listOf(navArgument("applicationId") { type = NavType.LongType })
    ) {
        val viewModel: ConfigurationViewModel = hiltViewModel()
        val uiState = viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(key1 = uiState.value.application?.applicationLabel) {
            onComposing(
                AppBarState(
                    title = "${uiState.value.application?.applicationLabel}",
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
                            viewModel.deleteApplication(uiState.value.application!!)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null
                            )
                        }
                    }
                )
            )
        }

        ConfigurationListView(
            navController = navController,
            uiState = uiState,
        )
    }
}
