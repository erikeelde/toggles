package com.izettle.wrench.dialogs.scope

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchScope
import kotlinx.android.synthetic.main.simple_list_item.view.*
import se.eelde.toggles.R

class ScopeRecyclerViewAdapter(private val listener: Listener) : ListAdapter<WrenchScope, ScopeRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.simple_list_item, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.view.value.text = item.name
        holder.view.setOnClickListener { listener.onClick(it, item) }
    }

    interface Listener {
        fun onClick(view: View, wrenchScope: WrenchScope)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WrenchScope>() {
            override fun areItemsTheSame(oldApplication: WrenchScope, newApplication: WrenchScope): Boolean {
                return oldApplication.id == newApplication.id
            }

            override fun areContentsTheSame(oldApplication: WrenchScope, newApplication: WrenchScope): Boolean {
                return oldApplication == newApplication
            }
        }
    }
}
