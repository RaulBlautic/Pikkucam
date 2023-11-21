package com.blautic.pikkucam.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.blautic.pikkucam.db.ProfileSettings;

public class ProfileSettingsViewModel extends ViewModel {

    // Create a LiveData with a String
    private MutableLiveData<ProfileSettings> currentSettings;

    public MutableLiveData<ProfileSettings> getCurrentSettings() {
        if (currentSettings == null) {
            currentSettings = new MutableLiveData<ProfileSettings>();
        }
        return currentSettings;
    }

// Rest of the ViewModel...

}
