package com.blautic.pikkucam.ui.inference;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.adapters.VPAdapter;
import com.blautic.pikkucam.adapters.activity.ItemConfigInferenceAdapter;
import com.blautic.pikkucam.adapters.activity.ItemModelsAdapter;
import com.blautic.pikkucam.databinding.FragmentInfoInferenceBinding;
import com.blautic.pikkucam.ui.SecondFragment;
import com.blautic.pikkucam.viewmodel.MainViewModel;
import com.google.android.material.tabs.TabLayout;

import kotlin.Unit;

public class InfoInferenceFragment extends Fragment {

    FragmentInfoInferenceBinding binding;

    MainViewModel viewModel;

    private ItemConfigInferenceAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInfoInferenceBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Boolean.TRUE.equals(viewModel.getIsSessionActive().getValue())) {
            binding.stopButton.setText("Finalizar");
            binding.stopButton.setBackgroundColor(getResources().getColor(R.color.PIKKU_RED));
        } else {
            binding.stopButton.setText("Reanudar");
            binding.stopButton.setBackgroundColor(getResources().getColor(R.color.PIKKU_GREEN));
        }


        adapter = new ItemConfigInferenceAdapter((model, index) -> {
            //TODO AQUI MODIFICAR EL VALOR DE LA POS INDEX DEL INTENTO A TRUE LIVE DATA
            return Unit.INSTANCE;
        }, (model, index) -> {
            //TODO AQUI MODIFICAR EL VALOR DE LA POS INDEX DEL INTENTO A FALSE LIVE DATA
            return Unit.INSTANCE;
        }, (aFloat, index) -> {
            Log.d("CHANGE SLIDER", aFloat + "    " + index);
            viewModel.setThresholdInferences(aFloat, index);
            return Unit.INSTANCE;
        }, (aBoolean, index) -> {
            viewModel.setCaptureTry(aBoolean, index);
            return Unit.INSTANCE;
        }, viewModel);

        binding.modelList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.modelList.setAdapter(adapter);

        viewModel.getSelectedModels().observe(getViewLifecycleOwner(), models -> {
            adapter.submitList(models);
            binding.modelList.setAdapter(adapter);
        });

        binding.stopButton.setOnClickListener(view1 -> {

            if (viewModel.getSelectedModels().getValue().size() > 0) {
                if (Boolean.TRUE.equals(viewModel.getIsSessionActive().getValue())) {
                    viewModel.setIsSessionActive(false);
                    binding.stopButton.setText("Reanudar");
                    binding.stopButton.setBackgroundColor(getResources().getColor(R.color.PIKKU_GREEN));
                } else {
                    viewModel.setIsSessionActive(true);
                    binding.stopButton.setText("Finalizar");
                    binding.stopButton.setBackgroundColor(getResources().getColor(R.color.PIKKU_RED));
                }
            }
        });

        binding.closeButton.setOnClickListener(view1 -> {
            removeFragment();
        });


    }

    public void removeFragment() {
        SecondFragment secondFragment = (SecondFragment) getParentFragment().getChildFragmentManager().findFragmentByTag("SecondFragment");
        FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
        if (secondFragment != null) {
            transaction.show(secondFragment);
        }
        transaction.remove(this);
        transaction.commit();
    }
}
