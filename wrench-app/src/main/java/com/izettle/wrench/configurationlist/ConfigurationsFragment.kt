package com.izettle.wrench.configurationlist

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.izettle.wrench.R
import com.izettle.wrench.core.Bolt
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchConfigurationWithValues
import com.izettle.wrench.databinding.FragmentConfigurationsBinding
import com.izettle.wrench.dialogs.booleanvalue.BooleanValueFragment
import com.izettle.wrench.dialogs.booleanvalue.BooleanValueFragmentArgs
import com.izettle.wrench.dialogs.enumvalue.EnumValueFragment
import com.izettle.wrench.dialogs.enumvalue.EnumValueFragmentArgs
import com.izettle.wrench.dialogs.integervalue.IntegerValueFragment
import com.izettle.wrench.dialogs.integervalue.IntegerValueFragmentArgs
import com.izettle.wrench.dialogs.scope.ScopeFragment
import com.izettle.wrench.dialogs.stringvalue.StringValueFragment
import com.izettle.wrench.dialogs.stringvalue.StringValueFragmentArgs
import org.koin.androidx.viewmodel.ext.viewModel


class ConfigurationsFragment : Fragment(), SearchView.OnQueryTextListener, ConfigurationRecyclerViewAdapter.Listener {
    private lateinit var fragmentConfigurationsBinding: FragmentConfigurationsBinding
    private var currentFilter: CharSequence? = null
    private var searchView: SearchView? = null
    private val model: ConfigurationViewModel by viewModel()

    private fun updateConfigurations(wrenchConfigurations: List<WrenchConfigurationWithValues>) {
        var adapter = fragmentConfigurationsBinding.list.adapter as ConfigurationRecyclerViewAdapter?
        if (adapter == null) {
            adapter = ConfigurationRecyclerViewAdapter(this, model)

            fragmentConfigurationsBinding.list.adapter = adapter
        }
        adapter.submitList(wrenchConfigurations)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putCharSequence(STATE_FILTER, searchView!!.query)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            currentFilter = savedInstanceState.getCharSequence(STATE_FILTER)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentConfigurationsBinding.inflate(inflater, container, false).also { fragmentConfigurationsBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assert(arguments != null)

        val args = ConfigurationsFragmentArgs.fromBundle(arguments!!)
        model.setApplicationId(args.applicationId)

        fragmentConfigurationsBinding.list.layoutManager = LinearLayoutManager(context)

        model.wrenchApplication.observe(this, Observer { wrenchApplication ->
            if (wrenchApplication != null) {
                (activity as AppCompatActivity).supportActionBar!!.title = wrenchApplication.applicationLabel
            }
        })

        model.defaultScopeLiveData.observe(this, Observer { scope ->
            if (scope != null && fragmentConfigurationsBinding.list.adapter != null) {
                fragmentConfigurationsBinding.list.adapter!!.notifyDataSetChanged()
            }
        })

        model.selectedScopeLiveData.observe(this, Observer { scope ->
            if (scope != null && fragmentConfigurationsBinding.list.adapter != null) {
                fragmentConfigurationsBinding.list.adapter!!.notifyDataSetChanged()
            }
            // fragmentConfigurationsBinding.scopeButton.text = scope!!.name
        })

        model.configurations.observe(this, Observer { wrenchConfigurationWithValues ->
            if (wrenchConfigurationWithValues != null) {
                updateConfigurations(wrenchConfigurationWithValues)
            }
        })

        model.isListEmpty.observe(this, Observer { isEmpty ->
            val animator = fragmentConfigurationsBinding.animator
            if (isEmpty == null || isEmpty) {
                animator.displayedChild = animator.indexOfChild(fragmentConfigurationsBinding.noConfigurationsEmptyView)
            } else {
                animator.displayedChild = animator.indexOfChild(fragmentConfigurationsBinding.list)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_configurations_list, menu)

        val item = menu.findItem(R.id.action_filter_configurations)
        searchView = item.actionView as SearchView
        searchView!!.setOnQueryTextListener(this)

        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                return true // Return true to collapse action view
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true // Return true to expand action view
            }
        })

        if (currentFilter.isNullOrBlank()) {
            item.expandActionView()
            searchView!!.setQuery(currentFilter, true)
        }
    }

    override fun onQueryTextChange(newText: String): Boolean {
        model.setQuery(newText)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_restart_application -> {
                model.wrenchApplication.observe(this, object : Observer<WrenchApplication> {
                    override fun onChanged(wrenchApplication: WrenchApplication?) {
                        model.wrenchApplication.removeObserver(this)

                        if (wrenchApplication == null) {
                            return
                        }

                        val activityManager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        activityManager.killBackgroundProcesses(wrenchApplication.packageName)

                        val intent = context!!.packageManager.getLaunchIntentForPackage(wrenchApplication.packageName)
                        if (intent != null) {
                            context!!.startActivity(Intent.makeRestartActivityTask(intent.component))
                        } else if (this@ConfigurationsFragment.view != null) {
                            Snackbar.make(this@ConfigurationsFragment.view!!, R.string.application_not_installed, Snackbar.LENGTH_LONG).show()
                        }
                    }
                })

                return true
            }
            R.id.action_application_settings -> {
                model.wrenchApplication.observe(this, object : Observer<WrenchApplication> {
                    override fun onChanged(wrenchApplication: WrenchApplication?) {
                        model.wrenchApplication.removeObserver(this)
                        if (wrenchApplication == null) {
                            return
                        }

                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", wrenchApplication.packageName, null)))
                    }
                })
                return true
            }
            R.id.action_delete_application -> {
                model.deleteApplication(model.wrenchApplication.value!!)
                Navigation.findNavController(fragmentConfigurationsBinding.root).navigateUp()
                return true
            }
            R.id.action_change_scope -> {
                val args = ConfigurationsFragmentArgs.fromBundle(arguments!!)
                ScopeFragment.newInstance(args.applicationId).show(childFragmentManager, null)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun configurationClicked(v: View, configuration: WrenchConfigurationWithValues) {
        if (model.selectedScopeLiveData.value == null) {
            Snackbar.make(fragmentConfigurationsBinding.root, "No selected scope found", Snackbar.LENGTH_LONG).show()
            return
        }

        val selectedScopeId = model.selectedScopeLiveData.value!!.id

        if (TextUtils.equals(String::class.java.name, configuration.type) || TextUtils.equals(Bolt.TYPE.STRING, configuration.type)) {

            StringValueFragment.newInstance(StringValueFragmentArgs.Builder(configuration.id, selectedScopeId).build()).show(childFragmentManager, null)

        } else if (TextUtils.equals(Int::class.java.name, configuration.type) || TextUtils.equals(Bolt.TYPE.INTEGER, configuration.type)) {

            IntegerValueFragment.newInstance(IntegerValueFragmentArgs.Builder(configuration.id, selectedScopeId).build()).show(childFragmentManager, null)

        } else if (TextUtils.equals(Boolean::class.java.name, configuration.type) || TextUtils.equals(Bolt.TYPE.BOOLEAN, configuration.type)) {

            BooleanValueFragment.newInstance(BooleanValueFragmentArgs.Builder(configuration.id, selectedScopeId).build()).show(childFragmentManager, null)

        } else if (TextUtils.equals(Enum::class.java.name, configuration.type) || TextUtils.equals(Bolt.TYPE.ENUM, configuration.type)) {

            EnumValueFragment.newInstance(EnumValueFragmentArgs.Builder(configuration.id, selectedScopeId).build()).show(childFragmentManager, null)

        } else {

            Snackbar.make(fragmentConfigurationsBinding.root, "Not sure what to do with type: " + configuration.type!!, Snackbar.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val STATE_FILTER = "STATE_FILTER"
    }
}
