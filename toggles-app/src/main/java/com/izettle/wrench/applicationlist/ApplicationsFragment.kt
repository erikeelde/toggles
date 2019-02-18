package com.izettle.wrench.applicationlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.izettle.wrench.databinding.FragmentApplicationsBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

internal class ApplicationsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val model by viewModels<ApplicationViewModel> { viewModelFactory }

    private lateinit var fragmentApplicationsBinding: FragmentApplicationsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentApplicationsBinding = FragmentApplicationsBinding.inflate(inflater, container, false)
        fragmentApplicationsBinding.list.layoutManager = LinearLayoutManager(context)
        return fragmentApplicationsBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentApplicationsBinding.list.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ApplicationAdapter()
        model.applications.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
        fragmentApplicationsBinding.list.adapter = adapter

        model.isListEmpty.observe(viewLifecycleOwner, Observer { isEmpty ->
            val animator = fragmentApplicationsBinding.animator
            if (isEmpty == null || isEmpty) {
                animator.displayedChild = animator.indexOfChild(fragmentApplicationsBinding.noApplicationsEmptyView)
            } else {
                animator.displayedChild = animator.indexOfChild(fragmentApplicationsBinding.list)
            }
        })
    }
}
