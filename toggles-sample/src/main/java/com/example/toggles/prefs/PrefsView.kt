package com.example.toggles.prefs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.toggles.TogglesValuesView

@Composable
fun PrefsView(viewModel: TogglesPrefsViewModel, modifier: Modifier = Modifier) =
    TogglesValuesView(viewState = viewModel.viewState.value, modifier)
