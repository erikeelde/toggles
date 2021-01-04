package com.izettle.wrench.oss.list

import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.oss.LicenceMetadata
import se.eelde.toggles.databinding.OssListItemBinding

class OssViewHolder(
    val binding: OssListItemBinding,
    private val clickCallback: ((LicenceMetadata) -> Unit)
) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var licenceMetadata: LicenceMetadata

    init {
        binding.root.setOnClickListener {
            clickCallback.invoke(licenceMetadata)
        }
    }

    fun bind(lmd: LicenceMetadata) {
        licenceMetadata = lmd
        binding.licenceDependency.text = licenceMetadata.dependency
    }
}
