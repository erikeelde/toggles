package com.izettle.wrench.dialogs.enumvalue

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchPredefinedConfigurationValue
import se.eelde.toggles.databinding.SimpleListItemBinding

class PredefinedValueRecyclerViewAdapter internal constructor(
    private val listener: Listener
) : ListAdapter<WrenchPredefinedConfigurationValue, PredefinedValueRecyclerViewAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

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

        holder.binding.value.text = item.value
        holder.itemView.setOnClickListener { listener.onClick(it, item) }
    }

    internal interface Listener {
        fun onClick(view: View, item: WrenchPredefinedConfigurationValue)
    }

    inner class ViewHolder(val binding: SimpleListItemBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<WrenchPredefinedConfigurationValue>() {
                override fun areItemsTheSame(
                    oldApplication: WrenchPredefinedConfigurationValue,
                    newApplication: WrenchPredefinedConfigurationValue
                ): Boolean {
                    return oldApplication.id == newApplication.id
                }

                override fun areContentsTheSame(
                    oldApplication: WrenchPredefinedConfigurationValue,
                    newApplication: WrenchPredefinedConfigurationValue
                ): Boolean {
                    return oldApplication == newApplication
                }
            }
    }
}
