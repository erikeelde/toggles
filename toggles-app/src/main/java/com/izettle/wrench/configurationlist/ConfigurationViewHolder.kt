package com.izettle.wrench.configurationlist

import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchConfigurationValue
import com.izettle.wrench.database.WrenchConfigurationWithValues
import com.izettle.wrench.database.WrenchScope
import se.eelde.toggles.databinding.ConfigurationListItemBinding

internal class ConfigurationViewHolder internal constructor(
    val binding: ConfigurationListItemBinding,
    val listener: ConfigurationRecyclerViewAdapter.Listener
) : RecyclerView.ViewHolder(binding.root) {

    fun bindTo(configuration: WrenchConfigurationWithValues, model: ConfigurationViewModel) {
        binding.title.text = configuration.key

        val lol = configuration.configurationValues!!

        val defaultScope = model.defaultScopeLiveData.value
        val selectedScope = model.selectedScopeLiveData.value

        val (_, _, value) = getItemForScope(defaultScope, lol)!!

        binding.defaultValue.text = value

        val selectedScopedItem = getItemForScope(selectedScope, lol)
        if (selectedScopedItem != null && selectedScopedItem.scope != defaultScope!!.id) {
            binding.defaultValue.paintFlags =
                binding.defaultValue.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.customValue.text = selectedScopedItem.value
            binding.customValue.visibility = View.VISIBLE
        } else {
            binding.customValue.text = null
            binding.defaultValue.paintFlags =
                binding.defaultValue.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding.customValue.visibility = View.GONE
        }
        binding.root.setOnClickListener { view ->
            listener.configurationClicked(
                view,
                configuration
            )
        }
    }

    @Suppress("ReturnCount")
    private fun getItemForScope(
        scope: WrenchScope?,
        wrenchConfigurationValues: Set<WrenchConfigurationValue>
    ): WrenchConfigurationValue? {
        if (scope == null) {
            return null
        }

        for (wrenchConfigurationValue in wrenchConfigurationValues) {
            if (wrenchConfigurationValue.scope == scope.id) {
                return wrenchConfigurationValue
            }
        }

        return null
    }

    fun clear() {
        binding.title.text = null
        binding.defaultValue.text = null
        binding.root.setOnClickListener(null)
    }
}
