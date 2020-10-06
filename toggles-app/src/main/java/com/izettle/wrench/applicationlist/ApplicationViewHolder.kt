package com.izettle.wrench.applicationlist

import android.content.pm.PackageManager
import android.view.View
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchApplication
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.application_list_item.*
import se.eelde.toggles.R

internal class ApplicationViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bindTo(application: WrenchApplication) {
        try {
            val packageManager = containerView.context.packageManager
            val icon = packageManager.getApplicationIcon(application.packageName)

            applicationIcon.setImageDrawable(icon)
            status.text = ""
        } catch (e: PackageManager.NameNotFoundException) {
            applicationIcon.setImageResource(R.drawable.ic_report_black_24dp)
            status.setText(R.string.not_installed)
            e.printStackTrace()
        }

        applicationName.text = application.applicationLabel

        containerView.setOnClickListener { v ->
            val applicationId = application.id
            Navigation.findNavController(v).navigate(ApplicationsFragmentDirections.actionApplicationsFragmentToConfigurationsFragment(applicationId))
        }
    }

    fun clear() {
        applicationIcon.setImageDrawable(null)
        applicationName.text = null
        status.text = null
        containerView.setOnClickListener(null)
    }
}
