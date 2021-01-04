package com.izettle.wrench.oss.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.databinding.FragmentOssBinding

@AndroidEntryPoint
class OssFragment : Fragment() {

    private lateinit var binding: FragmentOssBinding
    private val viewModel by viewModels<OssListViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentOssBinding.inflate(layoutInflater, container, false).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = OssRecyclerViewAdapter(
            clickCallback = {
                Log.d("Tag", "dependency = " + it.dependency)

                requireView().findNavController().navigate(OssFragmentDirections.actionActionOssToActionOssDetail(it.dependency, it.skipBytes.toInt(), it.length))
            }
        )
        binding.recView.adapter = adapter

        viewModel.getThirdPartyMetadata().observe(
            viewLifecycleOwner,
            {
                adapter.submitList(it)
            }
        )
    }
}
