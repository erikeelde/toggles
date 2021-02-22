package com.izettle.wrench.dialogs.autocompletestringvalue

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.databinding.FragmentAutocompleteStringValueBinding
import se.eelde.toggles.viewLifecycle

@AndroidEntryPoint
class AutoCompleteStringValueFragment : DialogFragment() {

    private var binding: FragmentAutocompleteStringValueBinding by viewLifecycle()
    private val viewModel by viewModels<FragmentStringValueViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        this.binding = FragmentAutocompleteStringValueBinding.inflate(layoutInflater)

        viewModel.viewState.observe(
            this,
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
            this,
            { viewEffect ->
                if (viewEffect != null) {
                    viewEffect.getContentIfNotHandled()?.let { contentIfNotHandled ->
                        when (contentIfNotHandled) {
                            ViewEffect.Dismiss -> dismiss()
                            is ViewEffect.ValueChanged -> binding.value.setText(contentIfNotHandled.value)
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

        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }
}