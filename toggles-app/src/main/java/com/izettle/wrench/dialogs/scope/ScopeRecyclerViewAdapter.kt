package com.izettle.wrench.dialogs.scope

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchScope
import se.eelde.toggles.databinding.SimpleListItemBinding

class ScopeRecyclerViewAdapter(private val listener: Listener) :
    ListAdapter<WrenchScope, ScopeRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SimpleListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.value.text = item.name
        holder.binding.root.setOnClickListener { listener.onClick(it, item) }
    }

    interface Listener {
        fun onClick(view: View, wrenchScope: WrenchScope)
    }

    inner class ViewHolder(val binding: SimpleListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WrenchScope>() {
            override fun areItemsTheSame(
                oldApplication: WrenchScope,
                newApplication: WrenchScope
            ): Boolean {
                return oldApplication.id == newApplication.id
            }

            override fun areContentsTheSame(
                oldApplication: WrenchScope,
                newApplication: WrenchScope
            ): Boolean {
                return oldApplication == newApplication
            }
        }
    }
}
