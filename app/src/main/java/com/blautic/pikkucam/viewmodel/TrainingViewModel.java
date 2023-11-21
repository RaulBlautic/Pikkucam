package com.blautic.pikkucam.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.blautic.pikkucam.api.PikkucamRepository;
import com.blautic.pikkucam.api.PlataformaRepository;
import com.blautic.pikkucam.api.response.LoginResponse;
import com.blautic.pikkucam.api.response.Model;
import com.blautic.pikkucam.api.response.User;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class TrainingViewModel extends ViewModel {

    @Inject
    PikkucamRepository pikkucamRepository;


    @Inject
    PlataformaRepository plataformaRepository;


    @Inject
    TrainingViewModel(PikkucamRepository pikkucamRepository, PlataformaRepository plataformaRepository) {
        this.pikkucamRepository = pikkucamRepository;
        this.plataformaRepository = plataformaRepository;
    }

    public LiveData<LoginResponse> loginAccessToken(String email, String password) {
        return plataformaRepository.loginAccessToken(email, password);
    }

    public LiveData<List<Model>> getModels() {
        return pikkucamRepository.getModels();
    }

    public LiveData<User> getUserMe() {
        return plataformaRepository.getUserMe();
    }

}
