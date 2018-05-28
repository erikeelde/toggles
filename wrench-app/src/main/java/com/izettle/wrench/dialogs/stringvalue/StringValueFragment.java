package com.izettle.wrench.dialogs.stringvalue;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;

import com.izettle.wrench.R;
import com.izettle.wrench.databinding.FragmentStringValueBinding;
import com.izettle.wrench.di.Injectable;

import javax.inject.Inject;

public class StringValueFragment extends DialogFragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private FragmentStringValueBinding binding;
    private FragmentStringValueViewModel viewModel;

    public static StringValueFragment newInstance(StringValueFragmentArgs args) {
        StringValueFragment fragment = new StringValueFragment();
        fragment.setArguments(args.toBundle());
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        assert getArguments() != null;

        binding = FragmentStringValueBinding.inflate(LayoutInflater.from(getContext()), null);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FragmentStringValueViewModel.class);

        StringValueFragmentArgs args = StringValueFragmentArgs.fromBundle(getArguments());

        viewModel.init(args.getConfigurationId(), args.getScopeId());

        viewModel.getConfiguration().observe(this, wrenchConfiguration -> {
            if (wrenchConfiguration != null) {
                getDialog().setTitle(wrenchConfiguration.key());
            }
        });

        viewModel.getSelectedConfigurationValueLiveData().observe(this, wrenchConfigurationValue -> {
            viewModel.setSelectedConfigurationValue(wrenchConfigurationValue);
            if (wrenchConfigurationValue != null) {
                binding.value.setText(wrenchConfigurationValue.getValue());
            }
        });

        binding.value.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                AsyncTask.execute(() -> viewModel.updateConfigurationValue(binding.value.getText().toString()));
                dismiss();
            }
            return false;
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_scope)
                .setView(binding.getRoot())
                .setPositiveButton(android.R.string.ok,
                        (dialog, whichButton) -> {
                            AsyncTask.execute(() -> viewModel.updateConfigurationValue(binding.value.getText().toString()));
                            dismiss();
                        }
                )
                .setNegativeButton(R.string.revert,
                        (dialog, whichButton) -> {
                            if (viewModel.getSelectedConfigurationValue() != null) {
                                AsyncTask.execute(() -> viewModel.deleteConfigurationValue());
                            }
                            dismiss();
                        }
                )
                .create();
    }
}
