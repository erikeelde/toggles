package com.example.wrench.wrenchprefs


import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.wrench.databinding.FragmentWrenchPreferencesBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class WrenchPreferencesFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<WrenchPreferencesFragmentViewModel> { viewModelFactory }

    private lateinit var binding: FragmentWrenchPreferencesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWrenchPreferencesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val providers = mutableMapOf<String, ProviderInfo>()
        for (installedPackage in requireActivity().packageManager.getInstalledPackages(PackageManager.GET_PROVIDERS)) {
            installedPackage.providers?.forEach { providerInfo: ProviderInfo? ->
                if (providerInfo?.authority != null) {
                    providers[providerInfo.authority] = providerInfo
                }
            }
        }

        binding.viewModel = viewModel
        return binding.root
    }
}
