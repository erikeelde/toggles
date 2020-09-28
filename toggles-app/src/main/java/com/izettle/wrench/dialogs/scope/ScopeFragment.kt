package com.izettle.wrench.dialogs.scope

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_scope.view.*
import se.eelde.toggles.R

@AndroidEntryPoint
class ScopeFragment : DialogFragment(), ScopeRecyclerViewAdapter.Listener {

    private val viewModel by viewModels<ScopeFragmentViewModel>()
    private var adapter: ScopeRecyclerViewAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val root = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_scope, null, false)

        viewModel.init(requireArguments().getLong(ARGUMENT_APPLICATION_ID))

        viewModel.scopes.observe(this, { scopes -> adapter!!.submitList(scopes) })

        viewModel.selectedScopeLiveData.observe(this, { wrenchScope -> viewModel.selectedScope = wrenchScope })

        adapter = ScopeRecyclerViewAdapter(this)
        root.list.adapter = adapter
        root.list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_scope)
                .setView(root)
                .setPositiveButton("Add") { _, _ ->

                    val input = EditText(requireContext())
                    input.setSingleLine()

                    AlertDialog.Builder(requireContext())
                            .setTitle("Create new scope")
                            .setView(input)
                            .setPositiveButton("OK"
                            ) { _, _ ->
                                val scopeName = input.text.toString()
                                viewModel.createScope(scopeName)
                            }.setNegativeButton("Cancel", null)
                            .show()
                }
                .setNegativeButton("Delete"
                ) { _, _ ->
                    val selectedScope = viewModel.selectedScope
                    if (selectedScope != null) {
                        if (!WrenchScope.isDefaultScope(selectedScope)) {
                            viewModel.removeScope(selectedScope)
                        }
                    }
                }
                .create()
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
