package com.izettle.wrench.oss.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.izettle.wrench.databinding.FragmentOssBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class OssFragment : DaggerFragment() {
    private lateinit var binding: FragmentOssBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<OssListViewModel> { viewModelFactory }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentOssBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = OssRecyclerViewAdapter(clickCallback = {
            Log.d("Tag", "dependency = " + it.dependency)

            requireView().findNavController().navigate(OssFragmentDirections.actionActionOssToActionOssDetail(it.dependency, it.skipBytes.toInt(), it.length))
        })
        binding.recView.adapter = adapter

        viewModel.getThirdPartyMetadata().observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }
}
