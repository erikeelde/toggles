package se.eelde.toggles.example.flow

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FlowView(viewModel: TogglesFlowViewModel, modifier: Modifier = Modifier) {
    TogglesValuesView(viewState = viewModel.viewState.value, modifier)
}
