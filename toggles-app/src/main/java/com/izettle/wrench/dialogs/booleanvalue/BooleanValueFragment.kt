package com.izettle.wrench.dialogs.booleanvalue

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import dagger.android.support.DaggerDialogFragment
import kotlinx.android.synthetic.main.fragment_boolean_value.view.*
import se.eelde.toggles.R
import javax.inject.Inject

class BooleanValueFragment : DaggerDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<FragmentBooleanValueViewModel> { viewModelFactory }

    private val args: BooleanValueFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_boolean_value, null)

        viewModel.init(args.configurationId, args.scopeId)

        viewModel.viewState.observe(this, Observer { viewState ->
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
        })

        viewModel.viewEffects.observe(this, Observer { viewEffect ->
            if (viewEffect != null) {
                viewEffect.getContentIfNotHandled()?.let { contentIfNotHandled ->
                    when (contentIfNotHandled) {
                        ViewEffect.Dismiss -> dismiss()
                        is ViewEffect.CheckedChanged -> view.value.isChecked = contentIfNotHandled.enabled
                    }
                }
            }
        })

        view.revert.setOnClickListener {
            viewModel.revertClick()
        }

        view.save.setOnClickListener {
            viewModel.saveClick(view.value.isChecked.toString())
        }

        return AlertDialog.Builder(requireActivity())
                .setView(view)
                .create()
    }

    companion object {

        fun newInstance(args: BooleanValueFragmentArgs): BooleanValueFragment {
            val fragment = BooleanValueFragment()
            fragment.arguments = args.toBundle()
            return fragment
        }
    }
}
