package com.izettle.wrench.dialogs.stringvalue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.databinding.FragmentStringValueBinding

@AndroidEntryPoint
class StringValueFragment : Fragment() {

    private lateinit var binding: FragmentStringValueBinding
    private val viewModel by viewModels<FragmentStringValueViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentStringValueBinding.inflate(layoutInflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.viewState.observe(
            viewLifecycleOwner,
            { viewState ->
                if (viewState != null) {
                    val invisible = (this.binding.container.visibility == View.INVISIBLE)
                    if (binding.container.visibility == View.INVISIBLE && viewState.title != null) {
                        binding.container.visibility = View.VISIBLE
                    }
                    binding.title.text = viewState.title

                    if (invisible) {
                        binding.value.jumpDrawablesToCurrentState()
                    }

                    if (viewState.saving || viewState.reverting) {
                        binding.value.isEnabled = false
                        binding.save.isEnabled = false
                        binding.revert.isEnabled = false
                    }
                }
            }
        )

        viewModel.viewEffects.observe(
            viewLifecycleOwner,
            { viewEffect ->
                if (viewEffect != null) {
                    viewEffect.getContentIfNotHandled()?.let { contentIfNotHandled ->
                        when (contentIfNotHandled) {
                            ViewEffect.Dismiss -> findNavController().popBackStack()
                            is ViewEffect.ValueChanged -> binding.value.setText(contentIfNotHandled.value.toString())
                        }
                    }
                }
            }
        )

        binding.revert.setOnClickListener {
            viewModel.revertClick()
        }

        binding.save.setOnClickListener {
            viewModel.saveClick(binding.value.text.toString())
        }
    }
}
