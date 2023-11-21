package com.blautic.pikkucam.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.blautic.pikkucam.api.data.NetworkState;
import com.blautic.pikkucam.api.response.LoginResponse;
import com.blautic.pikkucam.api.response.Model;
import com.blautic.pikkucam.api.response.User;
import com.blautic.trainingapp.entity.Status;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class PlataformaRepository {

    public final MutableLiveData<NetworkState> networkState = new MutableLiveData<>();

    private final PlataformaApi plataformaApi;

    public PlataformaRepository(PlataformaApi plataformaApi) {
        this.plataformaApi = plataformaApi;
    }

    public LiveData<LoginResponse> loginAccessToken(String email, String password) {
        networkState.postValue(NetworkState.RUNNING);
        MutableLiveData<LoginResponse> modelsMutableLiveData = new MutableLiveData<>();
        plataformaApi.loginAccessToken(email, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                networkState.setValue(NetworkState.SUCCESS);
                if (response.code() == 200 && response.body() != null) {
                    LoginResponse models = response.body();
                    modelsMutableLiveData.setValue(models);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                networkState.setValue(NetworkState.error(t.getLocalizedMessage()));
                modelsMutableLiveData.setValue(null);
            }
        });

        return modelsMutableLiveData;
    }

    public LiveData<User> getUserMe() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        plataformaApi.getUserMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.code() == 200 && response.body() != null) {
                    userLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Timber.e(t);
            }
        });

        return userLiveData;
    }
}
