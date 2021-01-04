package com.izettle.wrench.oss.detail

import android.app.Dialog
import android.os.Bundle
import android.text.util.Linkify
import androidx.appcompat.app.AlertDialog
import androidx.core.text.util.LinkifyCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.izettle.wrench.oss.LicenceMetadata
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.databinding.FragmentOssDetailBinding

@AndroidEntryPoint
class OssDetailFragment : DialogFragment() {

    private lateinit var binding: FragmentOssDetailBinding
    private val viewModel by viewModels<OssDetailViewModel>()

    private val args: OssDetailFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val root = FragmentOssDetailBinding.inflate(layoutInflater).also {
            binding = it
        }.root

        val licenceMetadata = LicenceMetadata(args.dependency, args.skip.toLong(), args.length)

        viewModel.getThirdPartyMetadata(licenceMetadata).observe(
            this,
            {
                binding.text.text = it
                LinkifyCompat.addLinks(binding.text, Linkify.WEB_URLS)
            }
        )

        return AlertDialog.Builder(requireActivity())
            .setTitle(licenceMetadata.dependency)
            .setView(root)
            .setPositiveButton("dismiss") { _, _ ->
                dismiss()
            }
            .create()
    }
}
