package se.eelde.toggles.oss.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.izettle.wrench.oss.detail.OssDetailViewModel

@Composable
internal fun OssDetailView(viewModel: OssDetailViewModel = hiltViewModel()) {

    val uiState = viewModel.state.collectAsStateWithLifecycle()

    val thirdPartyMetadata = viewModel.getThirdPartyMetadata().observeAsState()

    Surface(modifier = Modifier.padding(16.dp)) {
        Column {
            Text(uiState.value.licenceMetadata.dependency)
            if (!thirdPartyMetadata.value.isNullOrBlank()) {
                Text(thirdPartyMetadata.value!!)
            }
        }
    }
}
