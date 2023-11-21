package com.blautic.pikkucam.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.display.DisplayManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.databinding.FragmentCameraBinding;
import com.blautic.pikkucam.db.Devices;
import com.blautic.pikkucam.db.DevicesDao;
import com.blautic.pikkucam.db.ProfileSettings;
import com.blautic.pikkucam.db.ProfileSettingsDao;
import com.blautic.pikkucam.service.FullService;
import com.blautic.pikkucam.training.BackgroundExecutor;
import com.blautic.pikkucam.training.TrimVideoUtils;
import com.blautic.pikkucam.video.Camera2HSVideoFragment;
import com.blautic.pikkucam.video.OnTrimVideoListener;
import com.blautic.pikkucam.video.PlayVideoFragment;
import com.blautic.pikkucam.video.VideoTools;
import com.blautic.pikkucam.viewmodel.MainViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import timber.log.Timber;


public class CameraFragment extends Fragment implements PlayVideoFragment.OnPlayVideoListener, OnTrimVideoListener, DisplayManager.DisplayListener {

    public static final String PIKKU_SAVE_VIDEO = "PIKKU_SAVE_VIDEO";
    public static final String NUMBER_VIDEO_MODIFIED = "NUMBER_VIDEO_MODIFIED";
    static final int PIKKU_ASSOC_REQUEST = 1;
    static final int SESSION_TYPE_NOVIDEO = 100;
    static final int SESSION_TYPE_TIME = 0;
    static final int SESSION_TYPE_STROKE = 1;
    static final int SESSION_TYPE_MANUAL = 3;
    static final int SESSION_TYPE_ACTIVITY = 2;
    private static final int VIDEO_TYPE_TIME = 2;
    private static final int VIDEO_TYPE_NA = 0;
    private static final int REQUEST_PERMISSION = 1;
    private static int MIN_GAP_BETWEEN_VIDEOS = 5;

    public Camera2HSVideoFragment camera2VideoFragment;

    public SecondFragment menuFragment;

    public ProfileSettings profileSettings;

    public boolean assigned = false;

    public MutableLiveData<ProfileSettings> listenProfile;
    /**
     * variables COACH VIDEO BASE
     */
    // public CoachProfile profile;
    public VideoTools videoTools;
    public int selectedCamera;
    public boolean auto_exposure = true;
    public int manual_current_exposure;
    public String subtitle;
    public boolean isVideoRunning = false;
    public boolean validVideo = false;  //Indicará cuando un vídeo puede ser salvado
    public int sessionNumber;
    public int numOfVideos = 0;
    FragmentCameraBinding binding;
    String prefix = "session";
    String lastVideoName = null;
    //Timers
    int seconds = 0;
    int secondsOfLastVideo = 0;
    int secondsVideoByAutoStroke = 0;
    int secondsSession = 0;
    Timer timer;
    TimerTask timerTask;
    int timer_seconds = 1000;
    NewMainActivity act;
    private Devices devices;
    private DevicesDao devicesDao;
    private ProfileSettingsDao profileSettingsDao;
    private ProfileSettings[] profileSettingsArray;
    private Executor myExecutor;
    private String TAG = "CAMERA_FRAGMENT";
    private MainViewModel viewModel;
    private FullService mBoundService;
    private int min_exposure;
    private boolean has_zoom;
    private int max_zoom_factor;
    private float current_zoom_factor;
    /**
     * fin variables COACH VIDEO BASE
     */

    private boolean statsOn = false;
    private Context context;
    private boolean isPlayingVideo = false;
    private boolean saveVideo = false;
    private boolean saveVideoByAutoStroke = false;
    private long durationMax = 0;
    private long durationMin = 0;
    private int seconds_x_slot = 30;
    private int sessionType = SESSION_TYPE_TIME;
    private int MAX_VIDEO_SECS = 600;
    private int MANUAL_VIDEO_SECS_BAR = 60;
    private int saveVideoType = VIDEO_TYPE_TIME;
    private SimpleDateFormat fmt = new SimpleDateFormat("ddMMyyyy_HHmm");
    private AsyncTask asyncTask;
    private Calendar lastVideo;
    private PlayVideoFragment playVideoFragment;
    private WindowManager windowManager;
    private DisplayManager displayManager;
    private int baseRotation;

    private boolean isFirstTimeVideo = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "CameraFragment onViewCreated");

        context = getContext();
        act = (NewMainActivity) getActivity();

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

/*        viewModel.getCameraStateLiveData().observe(getViewLifecycleOwner(),aBoolean -> {
            if (!aBoolean){
                initVideo();
            }else{
                onPause();
                stopVideo();
            }
        });*/

        devicesDao = viewModel.getDatabase().devicesDao();
        profileSettingsDao = viewModel.getDatabase().profileSettingsDao();
        videoTools = new VideoTools(requireActivity());
        videoTools.getCameraCapabilities();
        myExecutor = Executors.newSingleThreadExecutor();
        makePikkuSportDir();
        IniAPP();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        if (getChildFragmentManager().getFragments().isEmpty()) {
            Log.d(TAG, "menuFragment assigned");
            menuFragment = new SecondFragment();
            menuFragment = new SecondFragment();
            transaction.add(R.id.second_fragment_layout, menuFragment, "SecondFragment").commit();
        }

        SharedPreferences sharedPref = act.getSharedPreferences("Pikkucam", Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPref.edit();

        listenProfile = new MutableLiveData<ProfileSettings>();

        listenProfile.setValue(profileSettings);

        listenProfile.observe(getViewLifecycleOwner(), changedProfileSettings -> {

            if (changedProfileSettings == null) {
                return;
            }
            if (profileSettings.getName().equals(changedProfileSettings.getName()) && assigned) {
                Log.d(TAG, "ASSIGNED");
                return;
            }
            Log.d(TAG, "NOT ASSIGNED, CAMERA " + changedProfileSettings.getCamera());
            assigned = true;
            profileSettings = changedProfileSettings;
            if (mBoundService != null) {
                mBoundService.profileSettings = profileSettings;
                mBoundService.isDriveEnabled = profileSettings.isUploadToDrive();
            }
            selectedCamera = profileSettings.getCamera();
            seconds_x_slot = Integer.parseInt(profileSettings.getVideo_duration());

            if(isFirstTimeVideo){
                initVideo();
            }
            isFirstTimeVideo = true;
        });


        profileSettingsDao.getProfileSettingsLive().observe(getViewLifecycleOwner(), profileSettingsArrayData -> {

            try{
                profileSettings = profileSettingsArrayData.get(sharedPref.getInt("Selected_mode", 0));
            } catch (IndexOutOfBoundsException e){
                profileSettings = profileSettingsArray[0];
            }


            if (mBoundService != null) {
                mBoundService.profileSettings = profileSettings;
                mBoundService.isDriveEnabled = profileSettings.isUploadToDrive();
            }
        });

        myExecutor.execute(() -> {

            profileSettingsArray = profileSettingsDao.getProfileSettingsArray();
            if (profileSettingsArray.length != 0) {
                try {
                    profileSettings = profileSettingsArray[sharedPref.getInt("Selected_mode", 0)];
                } catch (IndexOutOfBoundsException e){
                    profileSettings = profileSettingsArray[0];
                }
            }
            devices = devicesDao.loadAll();

        });
        numOfVideos = 0;
        setupDisplay();
    }

    public void setupDisplay() {
        windowManager = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
        baseRotation = windowManager.getDefaultDisplay().getRotation();
        Log.d(TAG, "Rotation state: " + String.valueOf(windowManager.getDefaultDisplay().getRotation()));
        binding.camerapreview.setRotation(0);
        displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void getVideos() {

        //ArrayList<Uri> filteredFiles = new ArrayList<>();
        String[] projection = new String[]{
                MediaStore.Video.Media.DISPLAY_NAME,
        };

        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";

        try {
            // Cache column indices.
            Cursor cursor = act.getApplicationContext().getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder);

            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            numOfVideos = cursor.getCount();
            numOfVideos = 0;

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameColumn);
                if (name.contains("pikku_")) {
                    numOfVideos++;
                    menuFragment.binding.textViewNumero.setText(String.valueOf(numOfVideos));
                }
            }
        } catch (Exception e) {
        }

    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    public int getDeviceDefaultOrientation() {

        WindowManager windowManager = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);

        Configuration config = getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }


    /**
     * COACH VIDEO BASE
     */


    public void uiInitVideoControls() {
        Log.d("ZoomRatios", String.valueOf(videoTools.zoomRatios(selectedCamera)));


        int min = videoTools.getMinimumExposure(selectedCamera);
        min_exposure = min;
        int max = videoTools.getMaximumExposure(selectedCamera);
        int max_bar = max - min;
        int half_bar = (max - min) / 2;
        final AppCompatSeekBar exposureBar = ((SecondFragment) Objects.requireNonNull(getChildFragmentManager().findFragmentByTag("SecondFragment"))).binding.seekBarExposure;

        exposureBar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
        exposureBar.setMax(max_bar);

        //**** temporal .Hay que poner el actual
        exposureBar.setProgress(half_bar);//preview.getCurrentExposure() - min_exposure );


        exposureBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //if (!auto_exposure) {
                manual_current_exposure = min_exposure + progress;
                if (camera2VideoFragment != null)
                    camera2VideoFragment.setExposureCompensation(manual_current_exposure);
                //}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        has_zoom = videoTools.hasZoom(selectedCamera);
        max_zoom_factor = videoTools.getMaxZoom(selectedCamera);
        final SeekBar zoomBar = ((SecondFragment) Objects.requireNonNull(getChildFragmentManager().findFragmentByTag("SecondFragment"))).binding.seekBarZoom;

        zoomBar.setMax(max_zoom_factor);
        if (!has_zoom) {
            zoomBar.setMax(0);
        } else {
            zoomBar.setMax(max_zoom_factor);
        }
        zoomBar.setMin(0);
        zoomBar.setProgress(0);

        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                zoomTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


    }


    /****************** ZOOM **************************************/
    public void zoomTo(int new_zoom_factor) {

        if (new_zoom_factor < 0)
            new_zoom_factor = 0;
        else if (new_zoom_factor > max_zoom_factor)
            new_zoom_factor = max_zoom_factor;


        if (this.has_zoom) {
            // don't cancelAutoFocus() here, otherwise we get sluggish zoom behaviour on Camera2 API
            if (camera2VideoFragment != null) {
                current_zoom_factor = camera2VideoFragment.setZoom(new_zoom_factor);
                // TextView zoom_val = (TextView) findViewById(R.id.zoom_value);
                // zoom_val.setText(String.format(Locale.UK,"%.1f",current_zoom_factor)+"x");
            }

        }
    }


    @Override
    public void onDestroy() {
        //JSF initVideo();
        super.onDestroy();
    }

    /**
     * FIN COACH VIDEO BASE
     */


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("SE DESTRUYE LA CAMARA");
        stopVideo();
        binding = null;
        viewModel = null;
        context = null;
        if(mBoundService!= null) mBoundService.stopService();
        mBoundService = null;
        displayManager.unregisterDisplayListener(this);
        displayManager = null;
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    @Override
    public void onResume() {
        displayManager.registerDisplayListener(this, new Handler(Looper.getMainLooper()));
        initVideo();
        super.onResume();
    }

    @Override
    public void onPause() {
        if(displayManager != null) displayManager.unregisterDisplayListener(this);
        stopVideo();
        Timber.d("SE PAUSA LA CAM");
        super.onPause();
    }

    /************************************************************************************************************/

    /************************************************* SETTINGS**************************************************/
    /************************************************************************************************************/

    public void initVideo() {

        if (videoTools == null){
            camera2VideoFragment = Camera2HSVideoFragment.newInstance(CameraCharacteristics.LENS_FACING_FRONT, requireActivity());
        } else {
            camera2VideoFragment = Camera2HSVideoFragment.newInstance(videoTools.extractVideoCamera(selectedCamera), requireActivity());
        }

        camera2VideoFragment.setParameters(0, profileSettings.getVideoQuality(), "session", videoTools.isEnabledVideoCameraHS(selectedCamera), videoTools);

        FragmentTransaction transaction1 = getChildFragmentManager().beginTransaction();

        if (getChildFragmentManager().findFragmentByTag("camera2VideoFragment") != null) {
            transaction1.replace(R.id.camerapreview, camera2VideoFragment, "camera2VideoFragment");
        } else {
            transaction1.add(R.id.camerapreview, camera2VideoFragment, "camera2VideoFragment");
        }
        transaction1.commit();
        startVideo();
        initSession(sessionType);
        lastVideo = Calendar.getInstance();
        playVideoFragment = PlayVideoFragment.newInstance();

        FragmentTransaction transaction2 = getChildFragmentManager().beginTransaction();
        if (getChildFragmentManager().findFragmentByTag("playVideoFragment") != null) {
            transaction2.replace(R.id.video_fragment_placeholder, playVideoFragment, "playVideoFragment");
        } else {
            transaction2.add(R.id.video_fragment_placeholder, playVideoFragment, "playVideoFragment");
        }
        transaction2.commit();

        uiInitVideoControls();
    }

    public void initSession(int type) {
        sessionType = type;
        stoptimertask();
        switch (type) {

            case SESSION_TYPE_NOVIDEO:
                startTimer(timer_seconds);
                break;

            case SESSION_TYPE_TIME:

                seconds = 0;
                validVideo = true;
                startTimer(timer_seconds);
                validVideo = true;

                break;

            case SESSION_TYPE_STROKE:

                seconds = seconds_x_slot;
                startTimer(timer_seconds);
                validVideo = false;
                break;

            case SESSION_TYPE_ACTIVITY:

                seconds = seconds_x_slot;
                startTimer(timer_seconds);
                validVideo = false;
                break;

            case SESSION_TYPE_MANUAL:
                //spVideoTime.setVisibility(View.INVISIBLE);
                //tvMenuTime.setVisibility(View.INVISIBLE);
                startTimer(timer_seconds);
                validVideo = false;
                break;

        }
    }

    /************************** Timers **************************************/
    public void startTimer(int duration) {
        stoptimertask();
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, duration);
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            //Log.d("Timer"," ... Cancelled");

            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                mngTime();

                long now = Calendar.getInstance().getTimeInMillis();
                //Log.d(TAG, "initializeTimerTask");



                startTimer(timer_seconds);
            }
        };


    }

    private void mngTime() {

        if (saveVideoByAutoStroke && Math.abs(seconds - secondsVideoByAutoStroke) > 2) {

            Intent intent = new Intent(PIKKU_SAVE_VIDEO);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            saveVideoByAutoStroke = false;

        }

        switch (sessionType) {
            case SESSION_TYPE_TIME:

                if (isVideoRunning) {
                    seconds++;
                    if (seconds > MAX_VIDEO_SECS) {//seconds_x_slot) {
                        changeVideo();
                        seconds = 0;
                    }

                    validVideo = true;
                }
                break;

            case SESSION_TYPE_MANUAL:

                if (isVideoRunning) {
                    seconds++;
                    if (seconds > MAX_VIDEO_SECS) {//seconds_x_slot) {
                        changeVideo();
                        seconds = 0;
                    }
                }
                break;

            case SESSION_TYPE_STROKE:
            case SESSION_TYPE_ACTIVITY:
                if (isVideoRunning && validVideo) {
                    seconds -= 1;
                    //progressBar.setProgress(seconds);

                    if (seconds <= 0) {
                        Log.w(TAG, "STROKES STOP VIDEO");//O:last"+lastStroke.getTimeInMillis()/1000+" now:"+now);
                        changeVideo();
                        seconds = seconds_x_slot;
                        validVideo = false;


                    }
                } //else progressBar.setProgress(0);
                break;

        }

        secondsSession++;


    }

    @Override
    public void onDisplayAdded(int displayId) {
        android.util.Log.i(TAG, "Display #" + displayId + " added.");
    }

    @Override
    public void onDisplayChanged(int displayId) {
        android.util.Log.i(TAG, "Display #" + displayId + " changed.");

        windowManager = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if (getDeviceDefaultOrientation() == Configuration.ORIENTATION_PORTRAIT && baseRotation == Surface.ROTATION_270 && rotation == Surface.ROTATION_90) {
            binding.camerapreview.setRotation(180);
        } else if (getDeviceDefaultOrientation() == Configuration.ORIENTATION_PORTRAIT && baseRotation == Surface.ROTATION_90 && rotation == Surface.ROTATION_270) {
            binding.camerapreview.setRotation(180);
        } else if (getDeviceDefaultOrientation() == Configuration.ORIENTATION_LANDSCAPE && baseRotation == Surface.ROTATION_180 && rotation == Surface.ROTATION_0) {
            binding.camerapreview.setRotation(180);
        } else if (getDeviceDefaultOrientation() == Configuration.ORIENTATION_LANDSCAPE && baseRotation == Surface.ROTATION_0 && rotation == Surface.ROTATION_180) {
            binding.camerapreview.setRotation(180);
        } else {
            binding.camerapreview.setRotation(0);
        }


    }

    @Override
    public void onDisplayRemoved(int displayId) {
        android.util.Log.i(TAG, "Display #" + displayId + " removed.");
    }

    public void saveVideo() {
        if (camera2VideoFragment != null) {
            long now = Calendar.getInstance().getTimeInMillis() / 1000;

            if (((lastVideo.getTimeInMillis() / 1000) + MIN_GAP_BETWEEN_VIDEOS) < now) {
                camera2VideoFragment.startBackThread();

                asyncTask = new CameraFragment.ChangeVideoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                numOfVideos++;
                menuFragment.binding.textViewNumero.setText(String.valueOf(numOfVideos));

                Intent intent = new Intent(NUMBER_VIDEO_MODIFIED);
                intent.putExtra("numberofvideos", "" + numOfVideos);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                secondsOfLastVideo = seconds;
                seconds = 0;
                //tvNumOfVideos.setText("" + numOfVideos);

                saveVideo = true;
                lastVideo = Calendar.getInstance();
            }
        }
    }


    private void saveVideoReal() throws IOException {

        String name = buildVideoName();

        if (sessionType == SESSION_TYPE_TIME) {
            seconds = 0;
            trimVideo(name);
            startTimer(timer_seconds);
            validVideo = true;
        } else {
            camera2VideoFragment.saveVideo(name);
            lastVideoName = camera2VideoFragment.getLastVideoAbsolutePath();
            insertVideoForLogo();
        }
        saveVideo = false;

    }

    private void insertVideoForLogo() {
        mBoundService = menuFragment.mBoundService;
        if (mBoundService != null) {

            //Slowmotion
            boolean sm = profileSettings.isSlowMotion();

            int hsFps = 0;
            if (sm) {
                int camera = videoTools.extractVideoCamera(profileSettings.getCamera());
                hsFps = videoTools.getHighSpeedFpsRange(camera).getUpper();
            }

            //Select Camera
            int camera = profileSettings.getCamera();

            //Sound
            boolean sound = profileSettings.isSound();

            //Subtitles


            // String subtitle = profileSettings.getSubtitle();
            if (!profileSettings.isSubtitle()) {
                subtitle = "";
            } else if (profileSettings.isSubtitle() && !profileSettings.isSubtitleButtons()) {

                subtitle = profileSettings.getSubtitle();

                //MOVIMIENTO
               /* if(profileSettings.getIs_mov_recognized()){

                    subtitle += "_"+profileSettings.getMov_recognized();

                    profileSettings.setIs_mov_recognized(false);

                    Executor myExecutor = Executors.newSingleThreadExecutor();
                    myExecutor.execute(() -> {
                        profileSettingsDao.insert(profileSettings);
                    });
                }*/
            }

            //VideoQuality
            int videoQuality = profileSettings.getVideoQuality();

            //Advertising
            boolean advertEnabled = profileSettings.isShowPubli();
            String advertFile = profileSettings.getAdvert_file();

            //mBoundService.addFile2VideoList(session_id,lastVideoName,hsFps,subtitleOn,subtitle,numOfVideos,false,null);
            if (advertEnabled)
                mBoundService.addFile2VideoList(sessionNumber, lastVideoName, hsFps, 1, subtitle, numOfVideos, true, advertFile);
            else
                mBoundService.addFile2VideoList(sessionNumber, lastVideoName, hsFps, 0, subtitle, numOfVideos, false, null);

        }
    }

    private void saveVideoByTime() throws IOException {
        String name = null;

        name = "coach_" + fmt.format(new Date()) + "_" + buildVideoName() + "_time.mp4";
        lastVideoName = name;

        if (name != null) trimVideo(name);
        saveVideoType = VIDEO_TYPE_NA;
    }

    /********************TRIMMING *****************************************************************************/


    private void trimVideo(String name) throws IOException {
        final String src = camera2VideoFragment.getVideoPrevFilePath();
        final String dst = camera2VideoFragment.getVideoDirPath() + "/" + name;

        Log.i(TAG, "TRIMMING *************" + src + "   TO:" + dst);

        final File file = new File(src);

        if (file != null) {
            if (getVideoDuration(src)) {

                BackgroundExecutor.execute(
                        new BackgroundExecutor.Task("", 0L, "") {
                            @Override
                            public void execute() {
                                if (durationMax > 0) {
                                    try {
                                        TrimVideoUtils.startTrim(file, dst, durationMin, durationMax, CameraFragment.this);
                                        //numOfVideos++;
                                    } catch (final Throwable e) {
                                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                                    }
                                }
                            }
                        }
                );
            } else onError("ERROR VIDEO");
        }
    }

    private boolean getVideoDuration(String src) throws IOException {
        long timeInmillisec = 0;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(src);
        } catch (IllegalArgumentException i) {
            //i.printStackTrace();
            retriever.release();
            return false;
        } catch (RuntimeException ignored){

        }

        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (time != null) timeInmillisec = Long.parseLong(time);
        else {
            return false;
            //timeInmillisec=secondsLast*1000+1000;//
        }

        durationMax = timeInmillisec;
        if (durationMax < seconds_x_slot * 1000) durationMin = 0;
        else durationMin = durationMax - (seconds_x_slot * 1000);
        retriever.release();
        return true;
    }

    @Override
    public void onTrimStarted() {}

    //********************* TrimVideoListener

    @Override
    public void getResult(final Uri uri) {
        camera2VideoFragment.deletePrevFile();
        camera2VideoFragment.changeFileName(uri.getPath(), buildVideoName());
        lastVideoName = camera2VideoFragment.getLastVideoAbsolutePath();
        insertVideoForLogo();
    }

    @Override
    public void cancelAction() {

        //finish();
    }

    @Override
    public void onError(final String message) {
        if (numOfVideos > 0) {
            numOfVideos--;
            menuFragment.binding.textViewNumero.setText(String.valueOf(numOfVideos));
        }
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //tvNumOfVideos.setTextColor(getResources().getColor(R.color.PIKKU_RED));
                //tvNumOfVideos.setText("X");
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /***************** PLAY ***************************************************************/

    public void startVideo() {
        //camera2Video = new Camera2Video(this,session_id,getVideoQuality(),prefix);
        camera2VideoFragment.setParameters(sessionNumber, profileSettings.getVideoQuality(), prefix, videoTools.isEnabledVideoCameraHS(selectedCamera), videoTools);
        camera2VideoFragment.startBackThread();
        //camera2VideoFragment.setCameraAndStart();
        isVideoRunning = true;
    }

    public void changeVideo() {
        if (camera2VideoFragment != null) camera2VideoFragment.startBackThread();
        //new ChangeVideoTask().execute("");
        asyncTask = new CameraFragment.ChangeVideoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

    }

    public void stopVideo() {
        //Da un error cuando cuando se ejecuta después de saveVideo
        //DE MOMENTO NO SE UTILIZA DURANTE EL USO DE LA ACTIVIDAD Y SE HACE CHANGE VIDEO
        if (camera2VideoFragment != null) {
            camera2VideoFragment.close();
            camera2VideoFragment = null;
            isVideoRunning = false;
        }
    }

    private void showLastVideo() {
        if (playVideoFragment != null && lastVideoName != null) {
            isPlayingVideo = true;
            //llTableTennisCoachScreen.setVisibility(View.GONE);
            binding.videoFragmentPlaceholder.setVisibility(View.VISIBLE);
            playVideoFragment.startVideo(lastVideoName);

        } else {
            //uiSecondButtonTeam1(0);
        }
    }

    public void onPlayVideoFinish() {

        Log.i(TAG, " Video Play Finished");
        binding.videoFragmentPlaceholder.setVisibility(View.GONE);
        //uiSecondButtonTeam1(0);
        //llTableTennisCoachScreen.setVisibility(View.VISIBLE);
        isPlayingVideo = false;
    }

    private String buildVideoName() {
        String name = "" + numOfVideos;
        return name;
    }

    private void IniAPP() {

        String path = "/data/";

        File defDir = new File(path);

        boolean result = true;
        if (!defDir.exists()) {
            //First Time
            result = defDir.mkdir();
            Log.d("Main", " Making directory ..." + defDir.getAbsolutePath());
        }

        Log.d("PIKKU", "DIR:" + defDir.getAbsolutePath() + " ? " + result);
        Log.d("PIKKU", "EXT:" + Environment.getExternalStorageDirectory());
        Log.d("PIKKU", "PUB:" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
    }

    public void makePikkuSportDir() {
        String saveLocation = act.getFilesDir() + File.separator + "Pikkucam";
        File saveDir = new File(saveLocation);
        if (!saveDir.isDirectory()) {
            saveDir.mkdirs();
        }
    }

    public int getLastFolderId() {

        String path = act.getFilesDir() + "/PikkuCam/";
        Log.d("Files", "Path:" + path);
        File directory = new File(path);
        File[] files = directory.listFiles();

        return files.length + 1;
    }

    private void buildSesionFolder() {

        sessionNumber = getLastFolderId();
        String saveLocation = act.getFilesDir() + File.separator + "PikkuCam" + File.separator + "session" + sessionNumber;
        File defDir = new File(saveLocation);
        defDir.mkdir();
        Log.d(TAG, "Making directory..." + defDir.getAbsolutePath());
    }

    /****************VIDEO  *********************************************************************/

    private class ChangeVideoTask extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... str) {
            long totalSize = 0;
            if (isVideoRunning) camera2VideoFragment.changeVideo();
            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {}

        protected void onPostExecute(Long result) {
            if (saveVideo) {
                try {
                    saveVideoReal();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // if(saveVideoType==VIDEO_TYPE_TIME) saveVideoByTime();
            asyncTask = null;
        }
    }
}
