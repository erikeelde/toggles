package com.izettle.wrench.oss.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.oss.LicenceMetadata
import kotlinx.android.synthetic.main.oss_list_item.view.*

class OssViewHolder(val view: View, private val clickCallback: ((LicenceMetadata) -> Unit)) : RecyclerView.ViewHolder(view) {
    private lateinit var licenceMetadata: LicenceMetadata

    init {
        view.setOnClickListener {
            clickCallback.invoke(licenceMetadata)
        }
    }

    fun bind(lmd: LicenceMetadata) {
        licenceMetadata = lmd
        view.licence_dependency.text = licenceMetadata.dependency
    }
}
