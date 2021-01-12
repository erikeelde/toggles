package com.izettle.wrench.dialogs.enumvalue

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchPredefinedConfigurationValue
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.databinding.FragmentEnumValueBinding

@AndroidEntryPoint
class EnumValueFragment : DialogFragment(), PredefinedValueRecyclerViewAdapter.Listener {

    private lateinit var binding: FragmentEnumValueBinding
    private val viewModel by viewModels<FragmentEnumValueViewModel>()
    private lateinit var adapter: PredefinedValueRecyclerViewAdapter

    private val args: EnumValueFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentEnumValueBinding.inflate(layoutInflater)

        viewModel.viewState.observe(
            this,
            { viewState ->
                if (viewState != null) {
                    if (binding.container.visibility == View.INVISIBLE && viewState.title != null) {
                        binding.container.visibility = View.VISIBLE
                    }
                    binding.title.text = viewState.title

                    if (viewState.saving || viewState.reverting) {
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
                        }
                    }
                }
            }
        )

        adapter = PredefinedValueRecyclerViewAdapter(this)
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        viewModel.predefinedValues.observe(
            this,
            { items ->
                if (items != null) {
                    adapter.submitList(items)
                }
            }
        )

        binding.revert.setOnClickListener {
            viewModel.revertClick()
        }

        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
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
