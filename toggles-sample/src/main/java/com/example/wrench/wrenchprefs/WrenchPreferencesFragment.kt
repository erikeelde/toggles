package com.example.wrench.wrenchprefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.wrench.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_wrench_preferences.*

@AndroidEntryPoint
class WrenchPreferencesFragment : Fragment() {

    private val viewModel by viewModels<WrenchPreferencesFragmentViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        LayoutInflater.from(requireContext()).inflate(R.layout.fragment_wrench_preferences, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getStringConfiguration().observe(viewLifecycleOwner) {
            string_configuration.text = it
        }

        viewModel.getUrlConfiguration().observe(viewLifecycleOwner) {
            url_configuration.text = it
        }

        viewModel.getBooleanConfiguration().observe(viewLifecycleOwner) {
            boolean_configuration.text = it.toString()
        }

        viewModel.getIntConfiguration().observe(viewLifecycleOwner) {
            int_configuration.text = it.toString()
        }

        viewModel.getEnumConfiguration().observe(viewLifecycleOwner) {
            enum_configuration.text = it.toString()
        }
    }
}
