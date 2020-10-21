package com.izettle.wrench.dialogs.enumvalue

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchPredefinedConfigurationValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_enum_value.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import se.eelde.toggles.R

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EnumValueFragment : DialogFragment(), PredefinedValueRecyclerViewAdapter.Listener {

    private val viewModel by viewModels<FragmentEnumValueViewModel>()
    private lateinit var adapter: PredefinedValueRecyclerViewAdapter

    private val args: EnumValueFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_enum_value, null)

        viewModel.viewState.observe(
            this,
            { viewState ->
                if (viewState != null) {
                    if (view.container.visibility == View.INVISIBLE && viewState.title != null) {
                        view.container.visibility = View.VISIBLE
                    }
                    view.title.text = viewState.title

                    if (viewState.saving || viewState.reverting) {
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
                        }
                    }
                }
            }
        )

        adapter = PredefinedValueRecyclerViewAdapter(this)
        view.list.adapter = adapter
        view.list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        viewModel.predefinedValues.observe(
            this,
            { items ->
                if (items != null) {
                    adapter.submitList(items)
                }
            }
        )

        view.revert.setOnClickListener {
            viewModel.revertClick()
        }

        return AlertDialog.Builder(requireActivity())
            .setView(view)
            .create()
    }

    override fun onClick(view: View, item: WrenchPredefinedConfigurationValue) {
        viewModel.saveClick(item.value!!)
        dismiss()
    }

    companion object {

        fun newInstance(args: EnumValueFragmentArgs): EnumValueFragment {
            val fragment = EnumValueFragment()
            fragment.arguments = args.toBundle()
            return fragment
        }
    }
}
