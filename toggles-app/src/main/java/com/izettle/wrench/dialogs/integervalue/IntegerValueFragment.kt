package com.izettle.wrench.dialogs.integervalue

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.izettle.wrench.dialogs.setWidthPercent
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.databinding.FragmentIntegerValueBinding

@AndroidEntryPoint
class IntegerValueFragment : DialogFragment() {

    private lateinit var binding: FragmentIntegerValueBinding
    private val viewModel by viewModels<FragmentIntegerValueViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentIntegerValueBinding.inflate(layoutInflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setWidthPercent(90)

        viewModel.viewState.observe(
            this,
            { viewState ->
                if (viewState != null) {
                    val invisible = (binding.container.visibility == View.INVISIBLE)
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
