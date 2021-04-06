package se.eelde.toggles.oss.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izettle.wrench.oss.detail.OssDetailViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun OssDetailView(viewModel: OssDetailViewModel) {

    val uiState = viewModel.state.collectAsState()

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
