package se.eelde.toggles.oss.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.izettle.wrench.oss.detail.OssDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.R
import se.eelde.toggles.compose_theme.TogglesTheme

@AndroidEntryPoint
class OssDetailFragment : Fragment() {

    private val viewModel by viewModels<OssDetailViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_compose, container, false).apply {
        findViewById<ComposeView>(R.id.compose).setContent {
            TogglesTheme {
                OssDetailView(viewModel)
            }
        }
    }
}
