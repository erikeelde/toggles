package com.izettle.wrench.applicationlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_applications.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import se.eelde.toggles.R
import javax.inject.Inject

internal class ApplicationsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val model by viewModels<ApplicationViewModel> { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_applications, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ApplicationAdapter()

        lifecycleScope.launch {
            model.applications.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        list.adapter = adapter

        lifecycleScope.launch {
            adapter.loadStateFlow.distinctUntilChangedBy {
                it.refresh
            }.collect {
                val snapshot = adapter.snapshot()
                if (snapshot.size == 0) {
                    animator.displayedChild = animator.indexOfChild(no_applications_empty_view)
                } else {
                    animator.displayedChild = animator.indexOfChild(list)
                }
            }
        }
    }
}
