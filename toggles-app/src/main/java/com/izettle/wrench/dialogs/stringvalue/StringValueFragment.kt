package com.izettle.wrench.dialogs.stringvalue

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_string_value.view.*
import se.eelde.toggles.R

@AndroidEntryPoint
class StringValueFragment : DialogFragment() {

    private val viewModel by viewModels<FragmentStringValueViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_string_value, null)

        viewModel.viewState.observe(
            this,
            { viewState ->
                if (viewState != null) {
                    val invisible = (view.container.visibility == View.INVISIBLE)
                    if (view.container.visibility == View.INVISIBLE && viewState.title != null) {
                        view.container.visibility = View.VISIBLE
                    }
                    view.title.text = viewState.title

                    if (invisible) {
                        view.value.jumpDrawablesToCurrentState()
                    }

                    if (viewState.saving || viewState.reverting) {
                        view.value.isEnabled = false
                        view.save.isEnabled = false
                        view.revert.isEnabled = false
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
                            is ViewEffect.ValueChanged -> view.value.setText(contentIfNotHandled.value.toString())
                        }
                    }
                }
            }
        )

        view.revert.setOnClickListener {
            viewModel.revertClick()
        }

        // view.value.setOnCheckedChangeListener { _, isChecked -> viewModel.checkedChanged(isChecked) }

        view.save.setOnClickListener {
            viewModel.saveClick(view.value.text.toString())
        }

        return AlertDialog.Builder(requireActivity())
            .setView(view)
            .create()
    }

    companion object {

        fun newInstance(args: StringValueFragmentArgs): StringValueFragment {
            val fragment = StringValueFragment()
            fragment.arguments = args.toBundle()
            return fragment
        }
    }
}
