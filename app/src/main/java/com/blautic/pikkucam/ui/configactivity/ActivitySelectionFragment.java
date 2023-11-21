package com.blautic.pikkucam.ui.configactivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blautic.pikkucam.adapters.activity.ItemModelsAdapter;
import com.blautic.pikkucam.api.response.Model;
import com.blautic.pikkucam.databinding.FragmentActivitySelectionBinding;
import com.blautic.pikkucam.ui.SecondFragment;
import com.blautic.pikkucam.viewmodel.MainViewModel;
import com.blautic.pikkucam.viewmodel.TrainingViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;

@AndroidEntryPoint
public class ActivitySelectionFragment extends Fragment {

    private TrainingViewModel trainingViewModel;
    private MainViewModel mainViewModel;

    private FragmentActivitySelectionBinding binding;

    private ItemModelsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainingViewModel = new ViewModelProvider(requireActivity()).get(TrainingViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivitySelectionBinding.inflate(inflater, container, false);

        adapter = new ItemModelsAdapter(model -> {
            mainViewModel.addSelectedModel(model);
            return Unit.INSTANCE;
        }, model -> {
            mainViewModel.removeSelectedModel(model);
            return Unit.INSTANCE;
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.progressBar.setVisibility(View.VISIBLE);

        binding.modelList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.modelList.setAdapter(adapter);

        trainingViewModel.getModels().observe(getViewLifecycleOwner(), models -> {
            binding.progressBar.setVisibility(View.INVISIBLE);
            adapter.submitList(models);
            binding.modelList.setAdapter(adapter);
        });

        binding.confirmButton.setOnClickListener(view1 -> {

            mainViewModel.setIsSessionActive(true);
            mainViewModel.initThresholdInferences(mainViewModel.getSelectedModelsFinal().size());
            mainViewModel.initResultInferences(mainViewModel.getSelectedModelsFinal().size());
            mainViewModel.initCaptureTry(mainViewModel.getSelectedModelsFinal().size());

            Log.d("MODELS","" +  mainViewModel.getSelectedModelsFinal().size());
            requireActivity().onBackPressed();
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}