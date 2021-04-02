package com.izettle.wrench.dialogs.booleanvalue

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.izettle.wrench.dialogs.setWidthPercent
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.databinding.FragmentBooleanValueBinding

@AndroidEntryPoint
class BooleanValueFragment : DialogFragment() {

    private lateinit var binding: FragmentBooleanValueBinding
    private val viewModel by viewModels<FragmentBooleanValueViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentBooleanValueBinding.inflate(layoutInflater).also { binding = it }.root

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
                            is ViewEffect.CheckedChanged ->
                                binding.value.isChecked =
                                    contentIfNotHandled.enabled
                        }
                    }
                }
            }
        )

        binding.revert.setOnClickListener {
            viewModel.revertClick()
        }

        binding.save.setOnClickListener {
            viewModel.saveClick(binding.value.isChecked.toString())
        }

    }
}
