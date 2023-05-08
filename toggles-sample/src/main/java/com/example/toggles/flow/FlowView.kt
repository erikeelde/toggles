package com.example.toggles.flow

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.toggles.TogglesValuesView

@Composable
fun FlowView(viewModel: TogglesFlowViewModel, modifier: Modifier = Modifier) {
    TogglesValuesView(viewState = viewModel.viewState.value, modifier)
}
