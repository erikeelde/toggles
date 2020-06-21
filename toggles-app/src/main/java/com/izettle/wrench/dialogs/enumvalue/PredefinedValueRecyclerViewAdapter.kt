package com.izettle.wrench.dialogs.enumvalue

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchPredefinedConfigurationValue
import com.izettle.wrench.databinding.SimpleListItemBinding

class PredefinedValueRecyclerViewAdapter internal constructor(
        private val listener: Listener
) : ListAdapter<WrenchPredefinedConfigurationValue, PredefinedValueRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SimpleListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.value.text = item.value
        holder.binding.root.setOnClickListener { listener.onClick(it, item) }
    }

    internal interface Listener {
        fun onClick(view: View, item: WrenchPredefinedConfigurationValue)
    }

    inner class ViewHolder(internal val binding: SimpleListItemBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WrenchPredefinedConfigurationValue>() {
            override fun areItemsTheSame(oldApplication: WrenchPredefinedConfigurationValue, newApplication: WrenchPredefinedConfigurationValue): Boolean {
                return oldApplication.id == newApplication.id
            }

            override fun areContentsTheSame(oldApplication: WrenchPredefinedConfigurationValue, newApplication: WrenchPredefinedConfigurationValue): Boolean {
                return oldApplication == newApplication
            }
        }
    }
}
