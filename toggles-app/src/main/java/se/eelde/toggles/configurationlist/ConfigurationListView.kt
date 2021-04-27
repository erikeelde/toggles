package se.eelde.toggles.configurationlist

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.izettle.wrench.configurationlist.ConfigurationViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ConfigurationListView(
    navController: NavController,
    viewModel: ConfigurationViewModel
) {
    val uiState = viewModel.state.collectAsState()

    LazyColumn() {
        uiState.value.configurations.forEach { configuration ->
            item {

                ListItem(
                    modifier = Modifier.clickable {
                        Log.w("Clicked configuration", "")
                    }) {
                    Text(configuration.key!!)
                }
            }
        }
    }
}