package se.eelde.toggles.dialogs.scope

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.izettle.wrench.dialogs.scope.ScopeFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.R
import se.eelde.toggles.compose_theme.TogglesTheme

@AndroidEntryPoint
class ScopeFragment : Fragment() {

    private val viewModel by viewModels<ScopeFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_compose, container, false).apply {
        findViewById<ComposeView>(R.id.compose).setContent {
            TogglesTheme {
                ScopeValueView(findNavController(), viewModel)
            }
        }
    }
}
