package com.example.wrench.livedataprefs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.wrench.databinding.FragmentLiveDataPreferencesBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class LiveDataPreferencesFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<LiveDataPreferencesFragmentViewModel> { viewModelFactory }

    private lateinit var binding: FragmentLiveDataPreferencesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLiveDataPreferencesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
}

