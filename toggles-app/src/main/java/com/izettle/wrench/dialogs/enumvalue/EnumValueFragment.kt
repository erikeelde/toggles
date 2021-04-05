package com.izettle.wrench.dialogs.enumvalue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.R
import se.eelde.toggles.TogglesTheme
import se.eelde.toggles.dialogs.enumvalue.EnumValueView

@AndroidEntryPoint
class EnumValueFragment : Fragment() {

    private val viewModel by viewModels<FragmentEnumValueViewModel>()

    private val args: EnumValueFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_compose, container, false).apply {
        findViewById<ComposeView>(R.id.compose).setContent {
            TogglesTheme {
                EnumValueView(findNavController(), viewModel)
            }
        }
    }
}
