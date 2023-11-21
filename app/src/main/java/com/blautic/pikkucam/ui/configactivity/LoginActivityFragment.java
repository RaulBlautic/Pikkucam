package com.blautic.pikkucam.ui.configactivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.api.SessionManager;
import com.blautic.pikkucam.databinding.FragmentLoginActivityBinding;
import com.blautic.pikkucam.viewmodel.MainViewModel;
import com.blautic.pikkucam.viewmodel.TrainingViewModel;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivityFragment extends Fragment {

    TrainingViewModel trainingViewModel;

    MainViewModel mainViewModel;

    FragmentLoginActivityBinding binding;

    SessionManager sessionManager;
    String email;
    String password;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginActivityBinding.inflate(inflater, container, false);
        trainingViewModel = new ViewModelProvider(requireActivity()).get(TrainingViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(getContext());

        if (!Objects.equals(sessionManager.getAuthToken(), null)){
            binding.userLogged.setVisibility(View.VISIBLE);
            binding.userUnLogged.setVisibility(View.INVISIBLE);
            binding.userName.setText(sessionManager.getUserName());
        } else {
            binding.userLogged.setVisibility(View.INVISIBLE);
            binding.userUnLogged.setVisibility(View.VISIBLE);
        }

        binding.continueButton.setOnClickListener(view1 -> {
            Navigation.findNavController(view1).navigate(R.id.action_loginActivityFragment_to_activitySelectionFragment);
        });

        binding.logoutButton.setOnClickListener(view1 -> {
            sessionManager.saveAuthToken(null);
            binding.userLogged.setVisibility(View.INVISIBLE);
            binding.userUnLogged.setVisibility(View.VISIBLE);
        });

        binding.emailTextFieldInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.passwordTextFieldInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                password = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.loginButton.setOnClickListener(view1 -> {


                trainingViewModel.loginAccessToken(email, password).observe(getViewLifecycleOwner(), loginResponse -> {
                    sessionManager.saveAuthToken(loginResponse.accessToken);
                    sessionManager.saveUserName(email);
                    Navigation.findNavController(view1).navigate(R.id.action_loginActivityFragment_to_activitySelectionFragment);
                });


        });
    }

    private boolean checkFields(String email, String password) {
        return true;
    }
}
