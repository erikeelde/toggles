package com.izettle.wrench.applicationlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import se.eelde.toggles.databinding.FragmentApplicationsBinding
import se.eelde.toggles.viewLifecycle

@AndroidEntryPoint
internal class ApplicationsFragment : Fragment() {

    private var binding: FragmentApplicationsBinding by viewLifecycle()

    private val model by viewModels<ApplicationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentApplicationsBinding.inflate(layoutInflater, container, false).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ApplicationAdapter()

        lifecycleScope.launch {
            model.applications.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        binding.list.adapter = adapter

        lifecycleScope.launch {
            adapter.loadStateFlow.distinctUntilChangedBy {
                it.refresh
            }.collect {
                val snapshot = adapter.snapshot()
                if (snapshot.size == 0) {
                    binding.animator.displayedChild =
                        binding.animator.indexOfChild(binding.noApplicationsEmptyView)
                } else {
                    binding.animator.displayedChild =
                        binding.animator.indexOfChild(binding.list)
                }
            }
        }
    }
}
