package se.eelde.toggles.bubble

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import se.eelde.toggles.R
import se.eelde.toggles.TogglesUriMatcher

class BubbleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bubble, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (TogglesUriMatcher.getTogglesUriMatcher().match(requireActivity().intent.data)) {
            TogglesUriMatcher.APPLICATION_ID -> {
                val applicationId = 1L
                Navigation.findNavController(view).navigate(
                    BubbleFragmentDirections.actionBubbleFragmentToApplicationConfigurationsGraph(
                        applicationId
                    )
                )
            }
            else -> {
                throw IllegalArgumentException("This activity does not support this uri: ${requireActivity().intent.data}")
            }
        }
    }
}
