package se.eelde.toggles.oss.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.izettle.wrench.oss.list.OssListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OssListView(
    navController: NavController,
    viewModel: OssListViewModel = hiltViewModel()
) {
    val uiState = viewModel.getThirdPartyMetadata().observeAsState()

    uiState.value?.let {
        Surface(modifier = Modifier.padding(16.dp)) {
            LazyColumn {
                it.forEach { licenseMetadata ->
                    item {
                        ListItem(
                            modifier = Modifier.clickable {
                                navController.navigate("oss/${licenseMetadata.dependency}/${licenseMetadata.skipBytes}/${licenseMetadata.length}")
                            },
                            headlineText = { Text(licenseMetadata.dependency) }
                        )
                    }
                }
            }
        }
    }
}
