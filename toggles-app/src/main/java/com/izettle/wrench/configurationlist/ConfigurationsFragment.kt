package com.izettle.wrench.configurationlist

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.R
import se.eelde.toggles.TogglesTheme
import se.eelde.toggles.configurationlist.ConfigurationListView

@AndroidEntryPoint
class ConfigurationsFragment :
    Fragment(),
    SearchView.OnQueryTextListener {
    private var currentFilter: CharSequence? = null
    private var searchView: SearchView? = null

    private val viewModel by viewModels<ConfigurationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_compose, container, false).apply {
        findViewById<ComposeView>(R.id.compose).setContent {
            TogglesTheme {
                ConfigurationListView(findNavController(), viewModel)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_configurations_list, menu)

        val item = menu.findItem(R.id.action_filter_configurations)
        searchView = item.actionView as SearchView
        searchView!!.setOnQueryTextListener(this)

        item.setOnActionExpandListener(
            object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    return true // Return true to collapse action view
                }

                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    return true // Return true to expand action view
                }
            }
        )

        if (currentFilter.isNullOrBlank()) {
            item.expandActionView()
            searchView!!.setQuery(currentFilter, true)
        }
    }

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.setQuery(newText)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_restart_application -> {
                val wrenchApplication = viewModel.wrenchApplication

                val activityManager =
                    requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityManager.killBackgroundProcesses(wrenchApplication.packageName)

                val intent =
                    requireContext().packageManager.getLaunchIntentForPackage(wrenchApplication.packageName)
                if (intent != null) {
                    requireContext().startActivity(Intent.makeRestartActivityTask(intent.component))
                } else if (this@ConfigurationsFragment.view != null) {
                    Snackbar.make(
                        this@ConfigurationsFragment.requireView(),
                        R.string.application_not_installed,
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                true
            }
            R.id.action_application_settings -> {
                val wrenchApplication = viewModel.wrenchApplication

                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", wrenchApplication.packageName, null)
                    )
                )
                true
            }
            R.id.action_delete_application -> {
                viewModel.deleteApplication(viewModel.wrenchApplication)
                findNavController().navigateUp()
                true
            }
            R.id.action_change_scope -> {
                findNavController().navigate(
                    ConfigurationsFragmentDirections.actionConfigurationsFragmentToScopeFragment(
                        viewModel.applicationId
                    )
                )
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
