package com.izettle.wrench.oss.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_oss.*
import se.eelde.toggles.R

@AndroidEntryPoint
class OssFragment : Fragment() {

    private val viewModel by viewModels<OssListViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        LayoutInflater.from(requireContext()).inflate(R.layout.fragment_oss, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = OssRecyclerViewAdapter(
            clickCallback = {
                Log.d("Tag", "dependency = " + it.dependency)

                requireView().findNavController().navigate(OssFragmentDirections.actionActionOssToActionOssDetail(it.dependency, it.skipBytes.toInt(), it.length))
            }
        )
        recView.adapter = adapter

        viewModel.getThirdPartyMetadata().observe(
            viewLifecycleOwner,
            Observer {
                adapter.submitList(it)
            }
        )
    }
}
