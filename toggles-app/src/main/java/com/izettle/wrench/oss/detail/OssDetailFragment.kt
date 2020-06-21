package com.izettle.wrench.oss.detail

import android.app.Dialog
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.text.util.LinkifyCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.izettle.wrench.databinding.FragmentOssDetailBinding
import com.izettle.wrench.oss.LicenceMetadata
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class OssDetailFragment : DaggerDialogFragment() {

    private lateinit var binding: FragmentOssDetailBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<OssDetailViewModel> { viewModelFactory }

    val args: OssDetailFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = FragmentOssDetailBinding.inflate(LayoutInflater.from(context))

        val licenceMetadata = LicenceMetadata(args.dependency, args.skip.toLong(), args.length)

        viewModel.getThirdPartyMetadata(licenceMetadata).observe(this, Observer {
            binding.text.text = it
            LinkifyCompat.addLinks(binding.text, Linkify.WEB_URLS)
        })

        return AlertDialog.Builder(requireActivity())
                .setTitle(licenceMetadata.dependency)
                .setView(binding.root)
                .setPositiveButton("dismiss") { _, _ ->
                    dismiss()
                }
                .create()
    }
}