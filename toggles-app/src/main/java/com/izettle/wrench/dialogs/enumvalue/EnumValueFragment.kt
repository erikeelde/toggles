package com.izettle.wrench.dialogs.enumvalue

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.R
import com.izettle.wrench.database.WrenchPredefinedConfigurationValue
import com.izettle.wrench.databinding.FragmentEnumValueBinding
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class EnumValueFragment : DaggerDialogFragment(), PredefinedValueRecyclerViewAdapter.Listener {

    private lateinit var binding: FragmentEnumValueBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<FragmentEnumValueViewModel> { viewModelFactory }
    private lateinit var adapter: PredefinedValueRecyclerViewAdapter

    private val args: EnumValueFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentEnumValueBinding.inflate(LayoutInflater.from(context))

        viewModel.init(args.configurationId, args.scopeId)

        viewModel.configuration.observe(this, Observer { wrenchConfiguration ->
            if (wrenchConfiguration != null) {
                requireDialog().setTitle(wrenchConfiguration.key)
            }
        })

        viewModel.selectedConfigurationValueLiveData.observe(this, Observer { wrenchConfigurationValue ->
            if (wrenchConfigurationValue != null) {
                viewModel.selectedConfigurationValue = wrenchConfigurationValue
            }
        })

        adapter = PredefinedValueRecyclerViewAdapter(this)
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        viewModel.predefinedValues.observe(this, Observer { items ->
            if (items != null) {
                adapter.submitList(items)
            }
        })

        return AlertDialog.Builder(requireActivity())
                .setTitle(".")
                .setView(binding.root)
                .setNegativeButton(R.string.revert
                ) { _, _ ->
                    if (viewModel.selectedConfigurationValue != null) {
                        viewModel.deleteConfigurationValue()
                    }
                    dismiss()
                }
                .create()
    }

    override fun onClick(view: View, item: WrenchPredefinedConfigurationValue) {
        viewModel.updateConfigurationValue(item.value!!)
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
