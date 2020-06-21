package com.izettle.wrench.oss.list

import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.databinding.OssListItemBinding
import com.izettle.wrench.oss.LicenceMetadata

class OssViewHolder(val binding: OssListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(licenceMetadata: LicenceMetadata) {
        binding.licenceMetadata = licenceMetadata
    }

}