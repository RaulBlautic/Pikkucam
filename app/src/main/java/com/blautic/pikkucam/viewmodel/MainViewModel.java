package com.blautic.pikkucam.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.blautic.pikkucam.api.response.Model;
import com.blautic.pikkucam.db.AppDatabase;
import com.blautic.pikkucam.db.Devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {


    //TODO ONTRY
    private final MutableLiveData<List<Boolean>> captureTry = new MutableLiveData<>(new ArrayList<>());


    public LiveData<List<Boolean>> getCaptureTry() { return captureTry; }


    public void initCaptureTry(int sizes) {
        List<Boolean> captureTryValue = captureTry.getValue();
        if (captureTryValue != null) {
            for (int i = 0; i < sizes; i++) {
                captureTryValue.add(i, false);
                captureTry.setValue(captureTryValue);
            }
        }
    }


    public void setCaptureTry(Boolean value, int index){
        List<Boolean> captureTryValue = captureTry.getValue();
        if (captureTryValue != null) {
            captureTryValue.set(index, value);
            captureTry.setValue(captureTryValue);
        }
    }






    private final MutableLiveData<List<float[]>> resultInferences = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<float[]>> getResultInferences() {
        return resultInferences;
    }

    public void initResultInferences(int sizes){
        List<float[]> resultInferencesValue = resultInferences.getValue();

        if (resultInferencesValue != null) {
            for (int i = 0; i < sizes; i++) {
                resultInferencesValue.add(i, new float[]{0,0});
                resultInferences.setValue(resultInferencesValue);
            }
        }
    }


    public void setResultInferences(float[] value, int index) {
        List<float[]> resultInferencesValue = resultInferences.getValue();
        if (resultInferencesValue != null) {
            resultInferencesValue.set(index, value);
            resultInferences.setValue(resultInferencesValue);
        }
    }









    private final MutableLiveData<List<Float>> thresholdInferences = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Float>> getThresholdInferences() {
        return thresholdInferences;
    }

    public void setThresholdInferences(Float value, int index) {
        List<Float> thresholdInferencesValue = thresholdInferences.getValue();
        if (thresholdInferencesValue != null) {
            thresholdInferencesValue.set(index, value);
            thresholdInferences.setValue(thresholdInferencesValue);
        }
    }

    public void clearThresholdInferences() {
        thresholdInferences.setValue(new ArrayList<>());
    }


    public void initThresholdInferences(int sizes){
        List<Float> thresholdInferencesValue = thresholdInferences.getValue();
        if (thresholdInferencesValue != null) {
            for (int i = 0; i < sizes; i++) {
                thresholdInferencesValue.add(i, 80f);
                thresholdInferences.setValue(thresholdInferencesValue);
            }
        }
    }








    public MutableLiveData<Boolean> getIsSessionActive() {
        return isSessionActive;
    }

    public void setIsSessionActive(Boolean isSessionActive) {
        this.isSessionActive.setValue(isSessionActive);
    }

    MutableLiveData<Boolean> isSessionActive = new MutableLiveData<>(false);








    private final MutableLiveData<List<Model>> selectedModels = new MutableLiveData<>(new ArrayList<>());

    public void addSelectedModel(Model model) {
        Log.d("ADD", "ADD");
        List<Model> currentSelectedModels = selectedModels.getValue();
        if (currentSelectedModels != null && !currentSelectedModels.contains(model)) {
            currentSelectedModels.add(model);
            selectedModels.setValue(currentSelectedModels);
        }
    }

    public void clearSelectedModels() {
        selectedModels.setValue(new ArrayList<>());
    }

    public void removeSelectedModel(Model model) {
        Log.d("REMOVE", "REMOVE");
        List<Model> currentSelectedModels = selectedModels.getValue();
        if (currentSelectedModels != null) {
            currentSelectedModels.remove(model);
            selectedModels.setValue(currentSelectedModels);
        }
    }

    public LiveData<List<Model>> getSelectedModels() {
        Log.d("GET", selectedModels.getValue().size() + "");
        return selectedModels;
    }

    public List<Model> getSelectedModelsFinal() {
        Log.d("GET", selectedModels.getValue().size() + "");
        return selectedModels.getValue();
    }








    MutableLiveData<Boolean> connectionLiveData = new MutableLiveData<>();

    MutableLiveData<Integer> selectedModelIndex = new MutableLiveData<>();

    MutableLiveData<Boolean> settingChangedLiveData = new MutableLiveData<>();

    //OBSERVA EL VALO DEL BOOL PARA SABER SI LA CAMARA PRINCIPAL O NO
    MutableLiveData<Boolean> cameraStateLiveData = new MutableLiveData<>();

    //OBSERVA EN QUE PASO DE LA GUIA DE USUARIO ESTAMOS
    MutableLiveData<Integer> stepLiveData = new MutableLiveData<>();

    //OBSERVA LA BATERIA
    MutableLiveData<Integer> batteryLiveData = new MutableLiveData<>();

    //OBSERVA LA CONEXIÓN
    MutableLiveData<Integer> connexionLiveData = new MutableLiveData<>();

    public MutableLiveData<Integer> getSettingsMultiCameraLiveData() {
        return settingsMultiCameraLiveData;
    }

    public void setSettingsMultiCameraLiveData(Integer settingsMultiCamera) {
        this.settingsMultiCameraLiveData.setValue(settingsMultiCamera);
    }

    MutableLiveData<Integer> settingsMultiCameraLiveData = new MutableLiveData<>();

    MutableLiveData<Boolean> pauseInference = new MutableLiveData<>();

    public MutableLiveData<Boolean> getReloadInference() {
        return reloadInference;
    }

    public void setReloadInference(Boolean reloadInference) {
        this.reloadInference.setValue(reloadInference);
    }

    MutableLiveData<Boolean> reloadInference = new MutableLiveData<>();

    private AppDatabase database;

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = Room.databaseBuilder(application, AppDatabase.class, "pikkucam").fallbackToDestructiveMigration().build();
    }

    public MutableLiveData<Boolean> getPauseInference() {
        return pauseInference;
    }

    public void setPauseInference(Boolean pauseInference) {
        this.pauseInference.setValue(pauseInference);
    }

    public MutableLiveData<Integer> getBatteryLiveData() {
        return batteryLiveData;
    }

    public void setBatteryLiveData(Integer batteryLiveData) {
        this.batteryLiveData.setValue(batteryLiveData);
    }

    public MutableLiveData<Integer> getConnexionLiveData() {
        return connexionLiveData;
    }

    public void setConnexionLiveData(Integer connexionLiveData) {
        this.connexionLiveData.setValue(connexionLiveData);
    }

    public MutableLiveData<Boolean> getSettingChangedLiveData() {
        return settingChangedLiveData;
    }

    public void setSettingChangedLiveData(Boolean settingChanged) {
        this.settingChangedLiveData.setValue(settingChanged);
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public MutableLiveData<Integer> getSelectedModelIndex() {
        return selectedModelIndex;
    }

    public void setSelectedModelIndex(Integer selectedModelIndex) {
        this.selectedModelIndex.setValue(selectedModelIndex);
    }

    public MutableLiveData<Boolean> getCameraStateLiveData() {
        return cameraStateLiveData;
    }

    public void setCameraStateLiveData(Boolean cameraState) {
        this.cameraStateLiveData.setValue(cameraState);
    }

    public LiveData<Boolean> getConnectionLiveData() {
        return connectionLiveData;
    }

    public void setConnectionLiveData(Boolean state) {
        this.connectionLiveData.postValue(state);
    }

    public LiveData<Integer> getStepLiveData() {
        return stepLiveData;
    }

    public void setStepLiveData(int step) {
        this.stepLiveData.setValue(step);
    }

    public void saveDevice(String mac) {
        Executor myExecutor = Executors.newSingleThreadExecutor();
        myExecutor.execute(() -> {
            database.devicesDao().update(new Devices(mac));
        });
    }

}
