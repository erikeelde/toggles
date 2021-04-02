package com.izettle.wrench.dialogs.scope

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchScope
import com.izettle.wrench.dialogs.setWidthPercent
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.R
import se.eelde.toggles.databinding.FragmentScopeBinding

@AndroidEntryPoint
class ScopeFragment : DialogFragment(), ScopeRecyclerViewAdapter.Listener {

    private lateinit var binding: FragmentScopeBinding
    private val viewModel by viewModels<ScopeFragmentViewModel>()
    private var adapter: ScopeRecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentScopeBinding.inflate(layoutInflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setWidthPercent(90)

        viewModel.init(requireArguments().getLong(ARGUMENT_APPLICATION_ID))

        viewModel.scopes.observe(this, { scopes -> adapter!!.submitList(scopes) })

        viewModel.selectedScopeLiveData.observe(
            this,
            { wrenchScope -> viewModel.selectedScope = wrenchScope }
        )

        adapter = ScopeRecyclerViewAdapter(this)
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        binding.addButton.setOnClickListener {
            val input = EditText(requireContext())
            input.setSingleLine()

            AlertDialog.Builder(requireContext())
                .setTitle("Create new scope")
                .setView(input)
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    val scopeName = input.text.toString()
                    viewModel.createScope(scopeName)
                }.setNegativeButton("Cancel", null)
                .show()
        }

        binding.deleteButton.setOnClickListener {
            val selectedScope = viewModel.selectedScope
            if (selectedScope != null) {
                if (!WrenchScope.isDefaultScope(selectedScope)) {
                    viewModel.removeScope(selectedScope)
                }
            }
        }
    }

    override fun onClick(view: View, wrenchScope: WrenchScope) {
        viewModel.selectScope(wrenchScope)
        dismiss()
    }

    companion object {

        private const val ARGUMENT_APPLICATION_ID = "ARGUMENT_APPLICATION_ID"

        fun newInstance(applicationId: Long): ScopeFragment {
            val fragment = ScopeFragment()
            val args = Bundle()
            args.putLong(ARGUMENT_APPLICATION_ID, applicationId)
            fragment.arguments = args
            return fragment
        }
    }
}
