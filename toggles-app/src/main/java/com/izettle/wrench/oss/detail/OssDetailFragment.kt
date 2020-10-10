package com.izettle.wrench.oss.detail

import android.app.Dialog
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.text.util.LinkifyCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.izettle.wrench.oss.LicenceMetadata
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_oss_detail.view.*
import se.eelde.toggles.R

@AndroidEntryPoint
class OssDetailFragment : DialogFragment() {

    private val viewModel by viewModels<OssDetailViewModel>()

    val args: OssDetailFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val root = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_oss_detail, null, false)

        val licenceMetadata = LicenceMetadata(args.dependency, args.skip.toLong(), args.length)

        viewModel.getThirdPartyMetadata(licenceMetadata).observe(
            this,
            {
                root.text.text = it
                LinkifyCompat.addLinks(root.text, Linkify.WEB_URLS)
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
