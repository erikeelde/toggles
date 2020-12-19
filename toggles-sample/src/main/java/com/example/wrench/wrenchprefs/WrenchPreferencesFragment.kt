package com.example.wrench.wrenchprefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.wrench.databinding.FragmentWrenchPreferencesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WrenchPreferencesFragment : Fragment() {

    private lateinit var binding: FragmentWrenchPreferencesBinding
    private val viewModel by viewModels<WrenchPreferencesFragmentViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentWrenchPreferencesBinding.inflate(inflater, container,false).also {
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
