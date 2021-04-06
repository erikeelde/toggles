package se.eelde.toggles.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import se.eelde.toggles.R

class HelpFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View = inflater.inflate(R.layout.fragment_compose, container, false).apply {
        findViewById<ComposeView>(R.id.compose).setContent {
            HelpView()
        }
    }

}
