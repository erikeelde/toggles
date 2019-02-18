package com.izettle.wrench.oss.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.izettle.wrench.oss.LicenceMetadata
import se.eelde.toggles.R

class OssRecyclerViewAdapter(private val clickCallback: ((LicenceMetadata) -> Unit)) : ListAdapter<LicenceMetadata, OssViewHolder>(object : DiffUtil.ItemCallback<LicenceMetadata?>() {
    override fun areItemsTheSame(p0: LicenceMetadata, p1: LicenceMetadata): Boolean = p0.dependency == p1.dependency

    override fun areContentsTheSame(p0: LicenceMetadata, p1: LicenceMetadata): Boolean = p0 == p1
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OssViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.oss_list_item, parent, false)
        return OssViewHolder(root, clickCallback)
    }

    override fun onBindViewHolder(holder: OssViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}