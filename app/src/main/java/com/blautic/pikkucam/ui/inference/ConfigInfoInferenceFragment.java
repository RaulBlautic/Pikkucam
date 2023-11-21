package com.blautic.pikkucam.ui.inference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.blautic.pikkucam.databinding.FragmentConfigInfoInferenceBinding;
import com.blautic.pikkucam.viewmodel.MainViewModel;
import com.blautic.pikkucam.viewmodel.TrainingViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ConfigInfoInferenceFragment extends Fragment {

    FragmentConfigInfoInferenceBinding binding;

    TrainingViewModel trainingViewModel;

    MainViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        trainingViewModel = new ViewModelProvider(this).get(TrainingViewModel.class);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = FragmentConfigInfoInferenceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}



