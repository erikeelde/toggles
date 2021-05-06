package se.eelde.toggles.oss.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.izettle.wrench.oss.list.OssListViewModel
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.R
import se.eelde.toggles.TogglesTheme

@AndroidEntryPoint
class OssFragment : Fragment() {

    private val viewModel by viewModels<OssListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_compose, container, false).apply {
        findViewById<ComposeView>(R.id.compose).setContent {
            TogglesTheme {
                OssListView(findNavController(), viewModel)
            }
        }
    }
}
