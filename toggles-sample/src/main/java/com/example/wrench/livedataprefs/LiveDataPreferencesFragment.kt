package com.example.wrench.livedataprefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.wrench.databinding.FragmentLiveDataPreferencesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LiveDataPreferencesFragment : Fragment() {

    private lateinit var binding: FragmentLiveDataPreferencesBinding
    private val viewModel by viewModels<LiveDataPreferencesFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentLiveDataPreferencesBinding.inflate(inflater, container, false).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getStringConfiguration().observe(viewLifecycleOwner) {
            binding.stringConfiguration.text = it
        }

        viewModel.getUrlConfiguration().observe(viewLifecycleOwner) {
            binding.urlConfiguration.text = it
        }

        viewModel.getBooleanConfiguration().observe(viewLifecycleOwner) {
            binding.booleanConfiguration.text = it.toString()
        }

        viewModel.getIntConfiguration().observe(viewLifecycleOwner) {
            binding.intConfiguration.text = it.toString()
        }

        viewModel.getEnumConfiguration().observe(viewLifecycleOwner) {
            binding.enumConfiguration.text = it.toString()
        }
    }
}
