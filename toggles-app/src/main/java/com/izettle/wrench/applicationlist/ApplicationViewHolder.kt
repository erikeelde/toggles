package com.izettle.wrench.applicationlist

import android.content.pm.PackageManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.izettle.wrench.database.WrenchApplication
import se.eelde.toggles.R
import se.eelde.toggles.databinding.ApplicationListItemBinding

internal class ApplicationViewHolder(val binding: ApplicationListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bindTo(application: WrenchApplication) {
        try {
            val packageManager = binding.root.context.packageManager
            val icon = packageManager.getApplicationIcon(application.packageName)

            binding.applicationIcon.setImageDrawable(icon)
            binding.status.text = ""
        } catch (e: PackageManager.NameNotFoundException) {
            binding.applicationIcon.setImageResource(R.drawable.ic_report_black_24dp)
            binding.status.setText(R.string.not_installed)
            e.printStackTrace()
        }

        binding.applicationName.text = application.applicationLabel

        binding.root.setOnClickListener { v ->
            val applicationId = application.id
            Navigation.findNavController(v).navigate(
                ApplicationsFragmentDirections.actionApplicationsFragmentToConfigurationsFragment(
                    applicationId,
                    application.applicationLabel
                )
            )
        }
    }

    fun clear() {
        binding.applicationIcon.setImageDrawable(null)
        binding.applicationName.text = null
        binding.status.text = null
        binding.root.setOnClickListener(null)
    }
}
