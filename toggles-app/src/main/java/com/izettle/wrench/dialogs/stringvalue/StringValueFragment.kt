package com.izettle.wrench.dialogs.stringvalue

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import dagger.android.support.DaggerDialogFragment
import kotlinx.android.synthetic.main.fragment_string_value.view.*
import se.eelde.toggles.R
import javax.inject.Inject

class StringValueFragment : DaggerDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val args: StringValueFragmentArgs by navArgs()

    private val viewModel by viewModels<FragmentStringValueViewModel> { viewModelFactory }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_string_value, null)

        viewModel.init(args.configurationId, args.scopeId)

        viewModel.configuration.observe(this, Observer { wrenchConfiguration ->
            if (wrenchConfiguration != null) {
                requireDialog().setTitle(wrenchConfiguration.key)
            }
        })

        viewModel.selectedConfigurationValueLiveData.observe(this, Observer { wrenchConfigurationValue ->
            viewModel.selectedConfigurationValue = wrenchConfigurationValue
            if (wrenchConfigurationValue != null) {
                view.value.setText(wrenchConfigurationValue.value)
            }
        })

        view.value.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.updateConfigurationValue(view.value.text!!.toString())
                dismiss()
            }
            false
        }

        return AlertDialog.Builder(requireActivity())
                .setTitle(".")
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.updateConfigurationValue(view.value.text!!.toString())
                    dismiss()
                }
                .setNegativeButton(R.string.revert) { _, _ ->
                    if (viewModel.selectedConfigurationValue != null) {
                        viewModel.deleteConfigurationValue()
                    }
                    dismiss()
                }
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
