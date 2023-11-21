package com.blautic.pikkucam.ui.configactivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.api.SessionManager;
import com.blautic.pikkucam.databinding.FragmentContentActivityBinding;
import com.blautic.pikkucam.ui.ConnectFragment;
import com.blautic.pikkucam.ui.SecondFragment;
import com.blautic.pikkucam.ui.SettingsFragment;
import com.blautic.pikkucam.viewmodel.MainViewModel;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ContentActivityFragment extends Fragment {
    FragmentContentActivityBinding binding;

    MainViewModel mainViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContentActivityBinding.inflate(inflater, container, false);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    public void removeFragment(){
        SecondFragment secondFragment = (SecondFragment) getParentFragment().getChildFragmentManager().findFragmentByTag("SecondFragment");
        FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
        if (secondFragment!=null){
            transaction.show(secondFragment);
        }
        transaction.remove(this);
        transaction.commit();
    }
}


