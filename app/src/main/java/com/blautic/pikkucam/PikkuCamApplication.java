package com.blautic.pikkucam;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import com.blautic.pikkucam.JobService.MyJobService;
import com.blautic.pikkucam.db.AppDatabase;
import com.blautic.pikkucam.db.ProfileSettings;
import com.blautic.pikkucam.db.ProfileSettingsDao;
import com.blautic.pikkucam.service.FullService;
import com.blautic.pikkucam.video.VideoTools;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

@HiltAndroidApp
public class PikkuCamApplication extends Application {

    public Typeface pikkuFont;
    public String BASE_PATH = "/Pikkucam/";
    public String INTERNAL_PATH = "";
    public String FILES_PATH = "";
    public String logoName_max = "watermark_max.png";
    public String logoName_h = "watermark_h.png";
    public String logoName_m = "watermark_m.png";
    public String logoName_l = "watermark_l.png";
    public SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public SimpleDateFormat logFmt = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
    public VideoTools vtools;
    //public FirebaseApp firebaseApp;
    private String advFileSet = "pikku_magenta.png";
    private String advFileGame = "pikku_blue.png";
    private String advFileCoach = "pikku_sponsor.png";
    private int widthPixels;
    private int heightPixels;
    private ProfileSettingsDao profileSettingsDao;
    private ProfileSettings profileSettings;
    private ProfileSettings[] profileSettingsArray;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        BASE_PATH = getFilesDir() + "/" + "Pikku";
        INTERNAL_PATH = getFilesDir() + "/";
        FILES_PATH = getFilesDir() + "/Pikkucam/AdvertisingFiles/";
        profileSettings = new ProfileSettings();
        createDefaultProfileSettings();
        //StartFullService();
        buildCameraFeatures();
        makePikkucamDir();
        //createTensorFlowFile();
    }

    public void StartFullService() {

       startService(new Intent(PikkuCamApplication.this, FullService.class));
    }

    public int getWidthPixels() {
        return widthPixels;
    }

    public void setWidthPixels(int widthPixels) {
        this.widthPixels = widthPixels;
    }

    public int getHeightPixels() {
        return heightPixels;
    }

    public void setHeightPixels(int heigthPixels) {
        this.heightPixels = heigthPixels;
    }

    public Typeface getFont() {
        return pikkuFont;
    }

    public String getPath() {
        return BASE_PATH;
    }

    public String getInternalPath() {
        return INTERNAL_PATH;
    }

    public SimpleDateFormat getDateFmt() {
        return dateFmt;
    }

    public String getAdvFileSet() {
        return advFileSet;
    }

    public String getAdvFileGame() {
        return advFileGame;
    }

    public String getAdvFileCoach() {
        return advFileCoach;
    }

    private void buildCameraFeatures() {
        vtools = new VideoTools(this);
        vtools.getCameraCapabilities();
    }

    public void makePikkucamDir() {
        String saveLocation = getFilesDir() + File.separator + "Pikkucam";
        File saveDir = new File(saveLocation);
        if (!saveDir.isDirectory()) {
            saveDir.mkdirs();
        }
        makeAdvertisingFilesDir();
    }

    public void makeAdvertisingFilesDir() {
        String saveLocation = getFilesDir() + File.separator + "Pikkucam" + File.separator + "AdvertisingFiles";
        File saveDir = new File(saveLocation);
        if (!saveDir.isDirectory()) {
            saveDir.mkdirs();
        }
    }

    public void createDefaultProfileSettings() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "pikkucam").fallbackToDestructiveMigration().build();

        profileSettingsDao = db.profileSettingsDao();
        Executor myExecutor = Executors.newSingleThreadExecutor();
        SharedPreferences sharedPref = getSharedPreferences("Pikkucam", Context.MODE_PRIVATE);

        myExecutor.execute(() -> {
            profileSettingsArray = profileSettingsDao.getProfileSettingsArray();
            int profile = sharedPref.getInt("Selected_mode", 0);
            if (profileSettingsArray.length != 0) {
                if (profile < profileSettingsArray.length) {
                    profileSettings = profileSettingsArray[profile];
                } else {
                    profileSettings = profileSettingsArray[0];
                }
            }

            if (profileSettings.getName() == null) {
                profileSettings = new ProfileSettings();
                profileSettings.setName("Base");
                profileSettings.setUploadToDrive(false);
                profileSettings.setShowPubli(false);
                profileSettings.setShowInfo(true);
                profileSettings.setIsSubtitle(false);
                profileSettings.setSubtitle("subtitulo");
                profileSettings.setButton1_short_subtitle("subtitulo");
                profileSettings.setButton1_long_subtitle("subtitulo");
                profileSettings.setButton2_short_subtitle("subtitulo");
                profileSettings.setButton2_long_subtitle("subtitulo");
                profileSettings.setGreenScreen(true);
                profileSettings.setSound(true);
                profileSettings.setCamera(0);
                profileSettings.setSlowMotion(false);
                profileSettings.setVideoQuality(1);
                profileSettings.setCameraButtons(2);
                profileSettings.setVideo_duration("10");
                profileSettings.setSubtitleButtons(false);
                profileSettings.setMovimientos(false);
                profileSettingsDao.insert(profileSettings);
            }
        });
        db.close();
    }

    public GoogleApiClient getPikkuGoogleApiClient() {
        return getPikkuGoogleApiClient();
    }

}
