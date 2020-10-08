package com.izettle.wrench.configurationlist

import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchConfigurationValue
import com.izettle.wrench.database.WrenchConfigurationWithValues
import com.izettle.wrench.database.WrenchScope
import kotlinx.android.synthetic.main.configuration_list_item.view.*

internal class ConfigurationViewHolder internal constructor(
    val containerView: View,
    val listener: ConfigurationRecyclerViewAdapter.Listener
) : RecyclerView.ViewHolder(containerView) {

    fun bindTo(configuration: WrenchConfigurationWithValues, model: ConfigurationViewModel) {
        containerView.title.text = configuration.key

        val lol = configuration.configurationValues!!

        val defaultScope = model.defaultScopeLiveData.value
        val selectedScope = model.selectedScopeLiveData.value

        val (_, _, value) = getItemForScope(defaultScope, lol)!!

        containerView.default_value.text = value

        val selectedScopedItem = getItemForScope(selectedScope, lol)
        if (selectedScopedItem != null && selectedScopedItem.scope != defaultScope!!.id) {
            containerView.default_value.paintFlags = containerView.default_value.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            containerView.custom_value.text = selectedScopedItem.value
            containerView.custom_value.visibility = View.VISIBLE
        } else {
            containerView.custom_value.text = null
            containerView.default_value.paintFlags = containerView.default_value.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            containerView.custom_value.visibility = View.GONE
        }
        containerView.setOnClickListener { view -> listener.configurationClicked(view, configuration) }
    }

    @Suppress("ReturnCount")
    private fun getItemForScope(scope: WrenchScope?, wrenchConfigurationValues: Set<WrenchConfigurationValue>): WrenchConfigurationValue? {
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
        containerView.title.text = null
        containerView.default_value.text = null
        containerView.setOnClickListener(null)
    }
}
