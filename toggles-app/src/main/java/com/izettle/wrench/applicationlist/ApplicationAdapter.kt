package com.izettle.wrench.applicationlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.izettle.wrench.R
import com.izettle.wrench.database.WrenchApplication

internal class ApplicationAdapter : PagedListAdapter<WrenchApplication, ApplicationViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        return ApplicationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.application_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = getItem(position)

        if (application != null) {
            holder.bindTo(application)
        } else {
            holder.clear()
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WrenchApplication>() {
            override fun areItemsTheSame(oldApplication: WrenchApplication, newApplication: WrenchApplication): Boolean {
                return oldApplication.id == newApplication.id
            }

            override fun areContentsTheSame(oldApplication: WrenchApplication, newApplication: WrenchApplication): Boolean {
                return oldApplication == newApplication
            }
        }
    }
}
