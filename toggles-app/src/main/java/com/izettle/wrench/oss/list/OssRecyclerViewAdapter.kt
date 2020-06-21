package com.izettle.wrench.oss.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.izettle.wrench.databinding.OssListItemBinding
import com.izettle.wrench.oss.LicenceMetadata

class OssRecyclerViewAdapter(private val clickCallback: ((LicenceMetadata) -> Unit)?) : ListAdapter<LicenceMetadata, OssViewHolder>(object : DiffUtil.ItemCallback<LicenceMetadata?>() {
    override fun areItemsTheSame(p0: LicenceMetadata, p1: LicenceMetadata): Boolean = p0.dependency == p1.dependency

    override fun areContentsTheSame(p0: LicenceMetadata, p1: LicenceMetadata): Boolean = p0 == p1
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OssViewHolder {

        val binding = OssListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.setOnClickListener {
            binding.licenceMetadata?.let {
                clickCallback?.invoke(it)
            }
        }
        return OssViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OssViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}