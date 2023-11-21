package com.blautic.pikkucam.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.databinding.FragmentInfoBinding;

public class InfoFragment extends Fragment {


    FragmentInfoBinding binding;
    CameraFragment cameraFragment;
    SecondFragment secondFragment;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraFragment = (CameraFragment) getParentFragment();
        secondFragment = (SecondFragment) cameraFragment.getChildFragmentManager().findFragmentByTag("SecondFragment");
    }

    public void removeFragment(){
        FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
        transaction.remove(InfoFragment.this);
        transaction.show(secondFragment);
        transaction.commit();
    }
}