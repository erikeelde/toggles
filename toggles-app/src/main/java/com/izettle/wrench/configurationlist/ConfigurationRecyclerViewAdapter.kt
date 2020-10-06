package com.izettle.wrench.configurationlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.izettle.wrench.database.WrenchConfigurationWithValues
import se.eelde.toggles.R

internal class ConfigurationRecyclerViewAdapter(
    private val listener: Listener,
    private val model: ConfigurationViewModel
) : ListAdapter<WrenchConfigurationWithValues, ConfigurationViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigurationViewHolder {
        return ConfigurationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.configuration_list_item, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ConfigurationViewHolder, position: Int) {
        val application = getItem(position)

        if (application != null) {
            holder.bindTo(application, model)
        } else {
            holder.clear()
        }
    }

    interface Listener {
        fun configurationClicked(v: View, configuration: WrenchConfigurationWithValues)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WrenchConfigurationWithValues>() {
            override fun areItemsTheSame(oldWrenchConfigurationWithValues: WrenchConfigurationWithValues, newWrenchConfigurationWithValues: WrenchConfigurationWithValues): Boolean {
                return oldWrenchConfigurationWithValues.id == newWrenchConfigurationWithValues.id
            }

            override fun areContentsTheSame(oldWrenchConfigurationWithValues: WrenchConfigurationWithValues, newWrenchConfigurationWithValues: WrenchConfigurationWithValues): Boolean {
                return oldWrenchConfigurationWithValues == newWrenchConfigurationWithValues &&
                    oldWrenchConfigurationWithValues.configurationValues!!.size == newWrenchConfigurationWithValues.configurationValues!!.size &&
                    oldWrenchConfigurationWithValues.configurationValues!!.containsAll(newWrenchConfigurationWithValues.configurationValues!!)
            }
        }
    }
}
