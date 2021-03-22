package com.izettle.wrench.oss.detail

import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.util.LinkifyCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.izettle.wrench.oss.LicenceMetadata
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.databinding.FragmentOssDetailBinding

@AndroidEntryPoint
class OssDetailFragment : Fragment() {

    private lateinit var binding: FragmentOssDetailBinding
    private val viewModel by viewModels<OssDetailViewModel>()

    private val args: OssDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentOssDetailBinding.inflate(layoutInflater).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val licenceMetadata = LicenceMetadata(args.dependency, args.skip.toLong(), args.length)

        binding.title.text = licenceMetadata.dependency

        viewModel.getThirdPartyMetadata(licenceMetadata).observe(viewLifecycleOwner) {
            binding.text.text = it
            LinkifyCompat.addLinks(binding.text, Linkify.WEB_URLS)
        }
    }
}
