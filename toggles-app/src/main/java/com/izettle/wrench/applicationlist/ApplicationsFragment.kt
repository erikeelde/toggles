package com.izettle.wrench.applicationlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_applications.*
import se.eelde.toggles.R
import javax.inject.Inject

internal class ApplicationsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val model by viewModels<ApplicationViewModel> { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_applications, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        list.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ApplicationAdapter()
        model.applications.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
        list.adapter = adapter

        model.isListEmpty.observe(viewLifecycleOwner, Observer { isEmpty ->
            val animator = animator
            if (isEmpty == null || isEmpty) {
                animator.displayedChild = animator.indexOfChild(no_applications_empty_view)
            } else {
                animator.displayedChild = animator.indexOfChild(list)
            }
        })
    }
}
