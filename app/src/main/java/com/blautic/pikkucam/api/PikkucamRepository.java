package com.blautic.pikkucam.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.blautic.pikkucam.api.data.NetworkState;
import com.blautic.pikkucam.api.response.LoginResponse;
import com.blautic.pikkucam.api.response.Model;
import com.blautic.pikkucam.api.response.User;
import com.blautic.trainingapp.entity.Status;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class PikkucamRepository {

    public final MutableLiveData<NetworkState> networkState = new MutableLiveData<>();

    private final PikkucamApi pikkucamApi;

    public PikkucamRepository(PikkucamApi pikkucamApi) {
        this.pikkucamApi = pikkucamApi;
    }

    public LiveData<List<Model>> getModels() {
        networkState.postValue(NetworkState.RUNNING);
        MutableLiveData<List<Model>> modelsMutableLiveData = new MutableLiveData<>();
        pikkucamApi.getModels().enqueue(new Callback<List<Model>>() {
            @Override
            public void onResponse(@NonNull Call<List<Model>> call, @NonNull Response<List<Model>> response) {
                networkState.setValue(NetworkState.SUCCESS);
                if (response.code() == 200 && response.body() != null) {
                    List<Model> models = response.body();
                    modelsMutableLiveData.setValue(models.stream().filter(model -> model.getDevices().size() == 1 && model.getFldSStatus() == Status.TRAINING_SUCCEEDED.getValue() && model.getFkTipo() == 1).collect(Collectors.toList()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Model>> call, @NonNull Throwable t) {
                networkState.setValue(NetworkState.error(t.getLocalizedMessage()));
                modelsMutableLiveData.setValue(null);
            }
        });

        return modelsMutableLiveData;
    }

}
