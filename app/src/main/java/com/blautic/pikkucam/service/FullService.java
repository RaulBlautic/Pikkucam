package com.blautic.pikkucam.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

import com.blautic.pikkucam.PikkuCamApplication;
import com.blautic.pikkucam.ble.BlueREMDevice;
import com.blautic.pikkucam.ble.bleUUID;
import com.blautic.pikkucam.cfg.CfgSessionMacs;
import com.blautic.pikkucam.cfg.CfgVals;
import com.blautic.pikkucam.db.AppDatabase;
import com.blautic.pikkucam.db.Devices;
import com.blautic.pikkucam.db.DevicesDao;
import com.blautic.pikkucam.db.ProfileSettings;
import com.blautic.pikkucam.db.ProfileSettingsDao;
import com.blautic.pikkucam.db.SessionProfile;
import com.blautic.pikkucam.db.VideoToProcess;
import com.blautic.pikkucam.db.VideoToProcessDao;
import com.blautic.pikkucam.drive.DriveServiceHelper;
import com.blautic.pikkucam.drive.GoogleDriveFileHolder;
import com.blautic.pikkucam.video.Logo2Video;
import com.blautic.pikkucam.video.MulticameraItem;
import com.blautic.pikkucam.video.VideoItem;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import timber.log.Timber;


public class FullService extends JobIntentService {

    public static final String NUMBER_VIDEO_OK = "NUMBER_VIDEO_OK";
    public static final String NUMBER_VIDEO_ERROR = "NUMBER_VIDEO_ERROR";

    public static final String SERVICE_NOTIFICATION_CHANNEL_ID = "service_notification_channel_id1";
    public static final String SERVICE_NOTIFICATION_CHANNEL_NAME = "Persistent notification shown when communicating";
    private static final int SERVICE_NOTIFICATION_ID = 5005;
    //Service
    private final IBinder myBinder = new LocalBinder();
    private final int NO_ACTION = -1;
    private final int ACTION_TIMER = 0;
    public int numberOfSamples = 20;
    //Cfg
    public CfgSessionMacs cfgSessionMacs;
    //public BleAdapter bleAdapter;
    public boolean isDriveEnabled = false;
    public ProfileSettings profileSettings;
    public int sessionNumber = 0;
    public boolean reconnect = true;
    public boolean isBound = false;
    //timers
    Timer timer;
    TimerTask timerTask;
    int timer_seconds = 10000;
    //Receiver
    IntentFilter localFt = new IntentFilter();
    //Bluetooth
    BlueREMDevice[] bleTargets = new BlueREMDevice[CfgVals.MAX_NUMBER_DEVICES + 1];
    //Fechas
    SimpleDateFormat sfDate = new SimpleDateFormat("HH:mm:ss dd/MM");
    Devices devices;
    DriveServiceHelper mDriveServiceHelper;
    ProfileSettingsDao profileSettingsDao;
    VideoToProcessDao videoToProcessDao;
    Executor myExecutor;
    DevicesDao devicesDao;
    private String TAG = "FS";
    //Notification
    private NotificationManager mNotificationManager;
    private Devices masterDevice;
    private boolean connectingOn = false;
    private boolean[] connecting = new boolean[CfgVals.MAX_NUMBER_DEVICES + 1];
    BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Log.d("Full Service Local:",intent.getAction());

            if (intent.getAction().equals(BlueREMDevice.BLE_READY)) {
                int dev = intent.getIntExtra("device", -1);
                if (dev != -1) connecting[dev] = false;

            } else if (intent.getAction().equals(BlueREMDevice.BLE_DISC)) {
                int dev = intent.getIntExtra("device", -1);
                if (dev != -1) {
                    connecting[dev] = false;
                }

            }
        }

    };
    private FileWriter mFileWriter;
    private SessionProfile selectedProfile = new SessionProfile();
    private ArrayList<VideoItem> listOfVideos;// = new ArrayList<>();
    private ArrayList<VideoItem> listOfVideosToUpload;// = new ArrayList<>();
    private ArrayList<MulticameraItem> multicameraItems;
    private int timer_on = NO_ACTION;
    private int estado = 0;
    private boolean uploadingFile = false;
    private boolean activityBound = false;
    private String id = null;
    private String strVideoBeingUploaded;
    private int idxVideoBeingChanged = -1;
    private int idxVideoBeingUploaded = -1;
    private AppDatabase db;
    private Logo2Video logo2Video;
    private boolean makingMosaic = false;
    private int idxMosaicBeingCreated = -1;
    private boolean changingLogo = false;
    private boolean isMaster;
    private String strVideoBeingChanged;
    private String logoPath;
    private boolean activeSession = true;
    private boolean sendFile2Master = false;

    /********************************** Constructor *****************************/
    public FullService() {
        //super("FullService");
        Log.d("Service", "Constructor");
    }

    static public void enqueueWork(Context context, Intent work) {
        enqueueWork(context, FullService.class, 1, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Log.d("Service", "Running");
    }

    /************************** Bound Service **********************/

    @Override
    public IBinder onBind(Intent intent) {
        //userGuideViewModel.setConnectionLiveData(true);
        isBound = true;
        Log.d("Service", "onBind");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        isBound = false;
        return super.onUnbind(intent);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "onCreate");
        setLocalReceiver();

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "pikkucam").fallbackToDestructiveMigration().build();

        devicesDao = db.devicesDao();

        videoToProcessDao = db.videoToProcessDao();

        myExecutor = Executors.newSingleThreadExecutor();

        myExecutor.execute(() -> {

            devices = devicesDao.loadAll();

            if (devices == null) {
                devices = new Devices();
                devices.setMac1("-");
                devices.setMac2("-");
                devices.setMac3("-");
                devices.setMac4("-");
                devicesDao.insert(devices);
            }

            setCfgMacs();

            List<VideoToProcess> list = videoToProcessDao.getVideoListToProcess();
            if (listOfVideos == null) {
                listOfVideos = new ArrayList<>();
            }
            if (list != null && list.size() != 0) {
                for (VideoToProcess v : list) {
                    File file = new File(v.getPath());
                    if (!file.exists()) {
                        videoToProcessDao.deleteByKey(v.getId());
                        continue;
                    }
                    try {
                        VideoItem vI = new VideoItem(v.getPath(), v.getSession_id(), v.getType(), v.getSubtitle_type(), v.getSubtitle_txt(), v.getStatus());
                        vI.setDate(v.getDate());
                        vI.setAdvertEnabled(v.getAdv_type() == 1);
                        vI.setAdvertFile(v.getAdv_file());
                        vI.setVideoQuality(v.getSource());
                        vI.setProfileName(v.getProfile());
                        vI.setDb_id((long) v.getId());
                        listOfVideos.add(vI);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            initConnections();
        });

        initService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerTask.cancel();
        timerTask = null;
        closeBleTargets();


        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        Log.d("Service", "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;

    }

/*
    public void getMacs() {

        myExecutor = Executors.newSingleThreadExecutor();
        myExecutor.execute(() -> {

            devices = devicesDao.loadAll();
            if (devices == null) {
                devices = new Devices();
                devices.setMac1("-");
                devices.setMac2("-");
                devices.setMac3("-");
                devices.setMac4("-");
                devicesDao.insert(devices);
            }

            try {
                Log.d(TAG, bleAdapter.getMacAddress());
                bleAdapter = new BleAdapter();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            setCfgMacs();
            bleAdapter.initialize(this);
            initConnections();
        });


    }
*/

    /************************* LOCAL RECEIVER *******************************************************/

    void setLocalReceiver() {

        localFt.addAction(BlueREMDevice.BLE_READY);
        localFt.addAction(BlueREMDevice.BLE_DISC);
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, localFt);
    }

    /********************************** CONFIGURATION ********************/


    public BlueREMDevice[] getTargets() {
        return bleTargets;
    }

    public BlueREMDevice getTargetDevice(int n) {
        return bleTargets[n];
    }

    /******************** TEMPORIZADORES ******************************/

    public void startTimer(int duration, int period) {
        stoptimertask();
        timer = new Timer();
        initializeTimerTask();
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, duration);//,period); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                //Log.d("FullService","TimerTask");

                checkConnections();
                startTimer(timer_seconds, 0);
                try {
                    mngState(timer_on);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    public void stopService() {
        Log.w(TAG, "STOP SERVICE");
        closeBleTargets();
        stoptimertask();
        this.stopSelf();
    }

    /************************** MACs ******************************************/
    public void setCfgSessionMacs(CfgSessionMacs s) {
        cfgSessionMacs = s;
    }

    private void setCfgMacs() {
        CfgSessionMacs cfgSessionMacs = new CfgSessionMacs();

        if (!devices.getMac1().contentEquals("-")) {
            cfgSessionMacs.setMacEnabled(CfgVals.DEVICE1, true);
            cfgSessionMacs.setMac(CfgVals.DEVICE1, devices.getMac1());
            cfgSessionMacs.setBle5(CfgVals.DEVICE1, devices.isBle5);
        } else {
            cfgSessionMacs.setMacEnabled(CfgVals.DEVICE1, false);
        }

        setCfgSessionMacs(cfgSessionMacs);
    }

    /************************** BLE ******************************************/

    public void initConnections() {
        if (connectingOn) closeBleTargets();

        initBleTargets();
        startConnections();
    }

    void initBleTargets() {

        if (cfgSessionMacs != null) {
            for (int i = CfgVals.DEVICE1; i <= CfgVals.MAX_NUMBER_DEVICES; i++) {
                if (cfgSessionMacs.isMacEnabled(i)) {
                    try {
                        if (cfgSessionMacs.getMac(i) != null) {

                            bleTargets[i] = new BlueREMDevice(i);
                            bleTargets[i].initDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(cfgSessionMacs.getMac(i)), cfgSessionMacs.isBle5(i));//"20:C3:8F:BD:2C:52"));
                            bleTargets[i].initContextDevice(this);

                            configureTargetDefault(i);
                            SystemClock.sleep(100);
                            Log.i(TAG, "Init bleTarget(" + i + "):" + cfgSessionMacs.getMac(i));
                        } else {
                            bleTargets[i] = null;
                            Log.i(TAG, "Init bleTarget(" + i + "):NULL");
                        }
                    } catch (IllegalArgumentException il) {
                        Log.i(TAG, "Init bleTarget(" + i + "):NO VALID MAC");
                    }
                }
            }
        }
    }

    public void startConnections() {

        if (cfgSessionMacs != null) {
            for (int i = CfgVals.DEVICE1; i <= CfgVals.MAX_NUMBER_DEVICES; i++) {
                if (bleTargets[i] != null) {
                    if (!bleTargets[i].isConnected() && !connecting[i]) {
                        try {
                            connecting[i] = true;
                            Log.i(TAG, "Connecting (" + i + ") ---> " + cfgSessionMacs.getMac(i));
                            bleTargets[i].connect();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    private void checkConnections() {
        for (int i = CfgVals.DEVICE1; i <= CfgVals.MAX_NUMBER_DEVICES; i++) {
            if (bleTargets[i] != null) {
                Timber.d("RECONNECT VALUE: %s", reconnect + " " + " Conecting: " + connecting[1] + " BLETARGET: " + bleTargets[1].isConnected());
                if (!bleTargets[i].isConnected() && !connecting[i]) {
                    reconnectDevice(i);
                }
            }
        }
    }

    private void reconnectDevice(int device) {

        //Log.d("RECONNECT VALUE: ", reconnect+" " + " Conecting: " + connecting[1] );
        if (!reconnect) {
            return;
        }

        //Recogemos configuración previa
		    /*SnsCfg snsCfg = bleTargets[device].getSensorDevice().snsCfg;
			bleTargets[device].close();
			bleTargets[device] = new BlueRemDevice(device);
			bleTargets[device].initDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(cfgSessionMacs.getMac(device)));//"20:C3:8F:BD:2C:52"));
			bleTargets[device].initContextDevice(this);
			bleTargets[device].getSensorDevice().setNewSnsCfg(snsCfg);*/
        // bleTargets[device].disconnect();


        try {
            connecting[device] = true;
            Timber.d("Reconnecting (" + device + ") ---> " + cfgSessionMacs.getMac(device));
            bleTargets[device].reconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void closeBleTargets() {
        for (int i = CfgVals.DEVICE1; i <= CfgVals.MAX_NUMBER_DEVICES; i++) {
            if (bleTargets[i] != null) {
                bleTargets[i].setCloseBGatt(true);
                bleTargets[i].close();
                bleTargets[i] = null;
            }
        }
    }

    private void configureTargetDefault(int device) {
        bleTargets[device].sensorDevice.snsCfg.setMpuMode(bleUUID.OPER_ACEL_RAW);
        bleTargets[device].sensorDevice.snsCfg.setSensorsCfg(CfgVals.BITS_ACEL, CfgVals.BITS_GYR, (byte) CfgVals.BITS_MAGNET, (byte) CfgVals.SCALE_ACC_4G,
                (byte) CfgVals.GYR_SCALE_1000, CfgVals.BASE_PERIOD, CfgVals.PACKS_PER_INTERVAL);
        bleTargets[device].sensorDevice.snsCfg.createOrders();
    }

    public void changeMAC(String newMAC) {
        closeBleTargets();

        bleTargets[1] = new BlueREMDevice(1);
        bleTargets[1].initDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(newMAC), cfgSessionMacs.isBle5(1));//"20:C3:8F:BD:2C:52"));
        bleTargets[1].initContextDevice(this);
        configureTargetDefault(1);
        reconnect = true;
        connecting[1] = false;
    }

    /******************** FOREGROUND **************************************************************************************************************************/
    private NotificationManager getManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    //Add notification channel for supporting Notification in Api 26 (Oreo)
    private void createNotificationChannels() {
        List<NotificationChannel> notificationChannels = new ArrayList<>();
        NotificationChannel serviceNotificationChannel = new NotificationChannel(
                SERVICE_NOTIFICATION_CHANNEL_ID,
                SERVICE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        serviceNotificationChannel.enableLights(true);
        serviceNotificationChannel.setLightColor(Color.RED);
        serviceNotificationChannel.setShowBadge(true);
        serviceNotificationChannel.enableVibration(true);
        serviceNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannels.add(serviceNotificationChannel);
        getManager().createNotificationChannels(notificationChannels);
    }

    private void setForeground(Notification notification, int ID) {
        startForeground(ID, notification);
    }

    /***********************************************************************************************************/
    public void sendAllUnixTime() {
        if (bleTargets != null) {
            byte[] order = new byte[4];
            int unixTime = (int) (System.currentTimeMillis() / 1000);
            byte[] downloadTime = new byte[]{
                    (byte) (unixTime >> 24),
                    (byte) (unixTime >> 16),
                    (byte) (unixTime >> 8),
                    (byte) unixTime

            };
            order[0] = 0;//downloadTime[3];
            order[1] = 0;//downloadTime[2];
            order[2] = 0;//downloadTime[1];
            order[3] = 0;//downloadTime[0];
            for (int i = 0; i < 2; i++) {
                if (bleTargets[i] != null) sendUnixTime(i, order);
            }
        }
    }

    private void sendUnixTime(int n, byte[] order) {

        if (bleTargets[n].isConnected())
            bleTargets[n].writeChar(bleUUID.UUID_PIKKU_UNIX, order);

    }

    public void setDriveEnable(boolean what) {
        isDriveEnabled = what;
    }

    /********************************* CONTROL DE ESTADOS ********************************/

    private void initService() {
        //if(!activityBound)
        //{
        if (listOfVideos == null) listOfVideos = new ArrayList<>();
        if (listOfVideosToUpload == null) listOfVideosToUpload = new ArrayList<>();
        if (multicameraItems == null) multicameraItems = new ArrayList<>();


        //mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (logo2Video == null) initLogo2Video();
        //updateVideoDeleteTime();


        if (timer == null) {
            startTimer(timer_seconds, 0);
            timer_on = ACTION_TIMER;
        }

        if (profileSettings == null) {
            Executor myExecutor = Executors.newSingleThreadExecutor();

            ProfileSettingsDao profileSettingsDao = db.profileSettingsDao();

            myExecutor.execute(() -> {
                ProfileSettings[] profileSettingsArray = profileSettingsDao.getProfileSettingsArray();
                SharedPreferences sharedPref = getSharedPreferences("Pikkucam", Context.MODE_PRIVATE);

                try {
                    profileSettings = profileSettingsArray[sharedPref.getInt("Selected_mode", 0)];
                } catch (IndexOutOfBoundsException e) {
                    profileSettings = profileSettingsArray[0];
                }

                isDriveEnabled = profileSettings.isUploadToDrive();
            });
        } else {
            isDriveEnabled = profileSettings.isUploadToDrive();
        }
    }

    private void mngState(int action) throws IOException {
        Log.d("FS mngState", "Sta:" + estado + " actBound:" + activityBound);

        startLogo2Video();
        startUploadingVideo();
        startTimer(timer_seconds, 0);

        timer_on = ACTION_TIMER;
    }

    public void addFile2VideoList(long session, String path, int highspeedFPS, int subtitleType, String subtitleTxt, int numfile, boolean advEnable, String advFile) {
        Date date = new Date();
        VideoItem videoItem = new VideoItem(path, session, highspeedFPS, subtitleType, subtitleTxt, numfile);
        videoItem.setDate(date);
        videoItem.setAdvertEnabled(advEnable);
        videoItem.setAdvertFile(advFile);
        videoItem.setVideoQuality(profileSettings.getVideoQuality());
        videoItem.setProfileSettings(profileSettings);

        if (listOfVideos == null) {
            listOfVideos = new ArrayList<>();

        }

        listOfVideos.add(videoItem);

        VideoToProcess videoToProcess = new VideoToProcess();
        videoToProcess.setPath(path);
        videoToProcess.setSession_id(session);
        videoToProcess.setDate(date);
        videoToProcess.setProfile(profileSettings.getName());
        if (highspeedFPS > 0) videoToProcess.setType(highspeedFPS);
        else videoToProcess.setType(0);
        videoToProcess.setSubtitle_type(subtitleType);
        videoToProcess.setSubtitle_txt(subtitleTxt);
        videoToProcess.setStatus(numfile);
        videoToProcess.setSource(profileSettings.getVideoQuality());
        if (advEnable) {
            videoToProcess.setAdv_type(1);
            videoToProcess.setAdv_file(advFile);
        } else videoToProcess.setAdv_type(0);

        if (videoToProcessDao != null) {
            long id = videoToProcessDao.insert(videoToProcess);
            videoItem.setDb_id(id);
        }

        Log.i(TAG, "ADD LOGO2VIDEO:" + session + " list_size:" + listOfVideos.size() + " db_id:" + videoToProcess.getId());
    }

    private boolean getNextVideoFromList() {
        int size = listOfVideos.size();
        if (size > 0 && !changingLogo) {
            if (size == 1) {
                //Protegemos video que podría reproducirse por ser el más reciente
                if (listOfVideos.get(0).getSession() != sessionNumber || !activeSession || (isMaster || sendFile2Master)) {
                    File file;

                    strVideoBeingChanged = listOfVideos.get(0).getPath();
                    file = new File(strVideoBeingChanged);

                    if (strVideoBeingChanged == null && file != null && file.exists()) {
                        if (videoToProcessDao != null && listOfVideos.get(0).getDb_id() != -1)
                            videoToProcessDao.deleteByKey((float) listOfVideos.get(0).getDb_id());
                        listOfVideos.remove(0);
                        idxVideoBeingChanged = -1;
                        return false;
                    }
                    idxVideoBeingChanged = 0;
                    return true;

                } else return false;

            } else {
                File file;
                strVideoBeingChanged = listOfVideos.get(0).getPath();
                file = new File(strVideoBeingChanged);
                if (strVideoBeingChanged == null && file != null && file.exists()) {
                    if (videoToProcessDao != null && listOfVideos.get(0).getDb_id() != -1)
                        videoToProcessDao.deleteByKey((float) listOfVideos.get(0).getDb_id());
                    listOfVideos.remove(0);
                    idxVideoBeingChanged = -1;
                    return false;
                }
                idxVideoBeingChanged = 0;
                return true;
            }

        }

        return false;
    }

    private void initLogo2Video() {

        logo2Video = new Logo2Video(this);
        setLogoForVideo();
        //logo2Video.loadFFMpegBinary();

        logo2Video.setListener(new Logo2Video.Logo2VideoListener() {
            @Override
            public void onStart() {
                Log.d(TAG, "******** LOGO2VIDEO STARTING --> " + sessionNumber + "(" + listOfVideos.size() + ")");//+strVideoBeingChanged);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "******** LOGO2VIDEO FINISHED --> " + sessionNumber + "(" + listOfVideos.size() + ")");//+strVideoBeingChanged);
            }

            @Override
            public void onFailure() {
                Intent intent = new Intent(NUMBER_VIDEO_ERROR);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                if (makingMosaic) {
                    if (idxMosaicBeingCreated != -1) deleteMulticameraItem(idxMosaicBeingCreated);
                    makingMosaic = false;
                    Log.d(TAG, "******** MOSAIC FAIL --> " + sessionNumber + "(" + multicameraItems.size() + ")");
                } else {
                    deleteFileInVideoList();
                    changingLogo = false;
                    //Toast.makeText(getApplicationContext(), "Fallo al procesar el video", Toast.LENGTH_LONG);
                    Log.d(TAG, "******** LOGO2VIDEO FAIL --> " + sessionNumber + "(" + listOfVideos.size() + ")");//+strVideoBeingChanged);
                }
            }

            @Override
            public void onSuccess() {
                //  if(idxVideoBeingChanged == -1 ) return;
                boolean hs = listOfVideos.get(idxVideoBeingChanged).isSlowmotion();
                String file_out = strVideoBeingChanged;
                if (hs) file_out = strVideoBeingChanged.replace(".mp4", "_sm.mp4");
                else file_out = strVideoBeingChanged.replace(".mp4", "_.mp4");
                String name = "";
                String sub = listOfVideos.get(idxVideoBeingChanged).getSubtitleTxt();
                if (sub != null && !sub.isEmpty()) {
                    name = sub + "_";
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    addToApi29Gallery(new File(file_out), name);
                } else {
                    String directory = Environment.DIRECTORY_MOVIES + "/PikkuCam";
                    String videoPath = Environment.getExternalStoragePublicDirectory(directory).getAbsolutePath();
                    Log.d(TAG, videoPath);
                    File sd = new File(videoPath);
                    if (!sd.exists()) sd.mkdirs();
                    File inFile = new File(file_out);

                    File outFile = new File(videoPath + "/" + name + inFile.getName());

                    try (InputStream in = new FileInputStream(inFile)) {
                        try (OutputStream out = new FileOutputStream(outFile)) {
                            byte[] buf = new byte[1024];
                            int length;
                            while ((length = in.read(buf)) > 0) {
                                out.write(buf, 0, length);
                            }
                            //FileUtils.copy(in, out);byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            //in.transferTo(out);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.getLocalizedMessage());
                    }

                }
                if (makingMosaic) {

                    if (idxMosaicBeingCreated != -1) {
                        if (isDriveEnabled) {
                            Log.d(TAG, "ADDING FILE to UPLOAD --> " + multicameraItems.get(idxMosaicBeingCreated).getOutput());
                            listOfVideosToUpload.add(new VideoItem(multicameraItems.get(idxMosaicBeingCreated).getOutput(), sessionNumber, 0, 0, null, 0));
                        }
                        deleteMulticameraItem(idxMosaicBeingCreated);
                    }
                    makingMosaic = false;
                    Log.d(TAG, "******** MOSAIC SUCCESS --> " + sessionNumber + "(" + multicameraItems.size() + ")");
                } else {
                    if (isMaster)
                        updateMulticameraItem(0, listOfVideos.get(idxVideoBeingChanged).getNumVideoFile(),
                                listOfVideos.get(idxVideoBeingChanged).getPath());
                    deleteFileInVideoList();
                    if (!isDriveEnabled) {
                        deleteVideoFile2(strVideoBeingChanged);
                    } else {
                        deleteVideoFile(strVideoBeingChanged);
                    }
                    changingLogo = false;
                    Intent intent = new Intent(NUMBER_VIDEO_OK);
                    intent.putExtra("numvideos", 1);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    Log.d(TAG, "******** LOGO2VIDEO SUCCESS --> " + sessionNumber + "(" + listOfVideos.size() + ")");//+strVideoBeingChanged);
                }


            }
        });

    }

    private void addToApi29Gallery(File file, String name) {

        String videoFileName = file.getName();
        String directory = Environment.DIRECTORY_MOVIES + "/PikkuCam";

        ContentValues valuesvideos;
        valuesvideos = new ContentValues();
        valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, directory);
        valuesvideos.put(MediaStore.Video.Media.TITLE, name + videoFileName);
        valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, name + videoFileName);
        valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 1);
        ContentResolver resolver = getApplicationContext().getContentResolver();
        Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); //all video files on primary external storage
        Uri uriSavedVideo = resolver.insert(collection, valuesvideos);

        ParcelFileDescriptor pfd;

        try {
            pfd = getApplicationContext().getContentResolver().openFileDescriptor(uriSavedVideo, "w");

            assert pfd != null;
            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

            // Get the already saved video as fileinputstream from here
            FileInputStream in = new FileInputStream(file);


            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {

                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            pfd.close();
            valuesvideos.clear();
            valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0);
            // valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0); //only your app can see the files until pending is turned into 0

            getContentResolver().update(uriSavedVideo, valuesvideos, null, null);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void startLogo2Video() throws IOException {
        if (getNextVideoFromList() && !makingMosaic) {
            boolean exists;
            changingLogo = true;
            boolean hs = listOfVideos.get(idxVideoBeingChanged).isSlowmotion();
            boolean withSubtitle = profileSettings.isSubtitle();//listOfVideos.get(idxVideoBeingChanged).isSubtitleEnabled();
            boolean withAdvert = profileSettings.isShowPubli();
            String subtitle_text = listOfVideos.get(idxVideoBeingChanged).getSubtitleTxt();


            String file_out;
            if (hs) file_out = strVideoBeingChanged.replace(".mp4", "_sm.mp4");
            else file_out = strVideoBeingChanged.replace(".mp4", "_.mp4");

            if (logo2Video != null) {
                //Quality
                int qua = listOfVideos.get(idxVideoBeingChanged).getVideoQuality();//getProfileSettings().getVideoQuality();

                //High speed - slow motion
                if (hs) {
                    int hsFPS = listOfVideos.get(idxVideoBeingChanged).getHighspeedFPS();
                    exists = logo2Video.setVideoFile(strVideoBeingChanged, file_out, qua, hsFPS);
                } else {
                    exists = logo2Video.setVideoFile(strVideoBeingChanged, file_out, qua, 0);
                }

                if (exists) {
                    //Subtitle
                    if (withSubtitle) {
                        String txt = subtitle_text;//listOfVideos.get(idxVideoBeingChanged).getSubtitleTxt();
                        String txtStart = "PIKKUCAM";//sfDate.format(listOfVideos.get(idxVideoBeingChanged).getDate());
                        logo2Video.setSubtitle(true, txt, txtStart);
                    } else {
                        logo2Video.setSubtitle(false, null, null);
                    }

                    if (withAdvert) {
                        logo2Video.setAdvertInfo(withAdvert, listOfVideos.get(idxVideoBeingChanged).getAdvertFile());
                    } else {
                        logo2Video.setAdvertInfo(withAdvert, null);
                    }

                    //Logo
                    setLogoForVideo();

                    //ProfileName
                    if (listOfVideos.get(idxVideoBeingChanged).getProfileSettings() != null) {
                        logo2Video.setProfileSettings(listOfVideos.get(idxVideoBeingChanged).getProfileSettings().getName());
                    } else {
                        logo2Video.setProfileSettings(listOfVideos.get(idxVideoBeingChanged).getProfileName());
                    }

                    logo2Video.execute();
                } else {

                }
            }
        } else if (!makingMosaic) {
            int idx = checkIfMosaicReady();
            if (idx > -1) startMakingMosaic(idx);


        }
    }

    public void setCurrentSession(boolean active, long session) {
        activeSession = active;
        //sessionNumber = session;
    }

    private void setLogoForVideo() {
        if (profileSettings != null) {
            int quality = profileSettings.getVideoQuality();

            switch (quality) {
                case 0:
                    logoPath = ((PikkuCamApplication) getApplicationContext()).FILES_PATH + ((PikkuCamApplication) getApplicationContext()).logoName_l;
                    break;
                case 1:
                    logoPath = ((PikkuCamApplication) getApplicationContext()).FILES_PATH + ((PikkuCamApplication) getApplicationContext()).logoName_m;
                    break;
                case 2:
                    logoPath = ((PikkuCamApplication) getApplicationContext()).FILES_PATH + ((PikkuCamApplication) getApplicationContext()).logoName_h;
                    break;
                case 3:
                    logoPath = ((PikkuCamApplication) getApplicationContext()).FILES_PATH + ((PikkuCamApplication) getApplicationContext()).logoName_h;
                    break;
            }

            logo2Video.setLogo(logoPath);
        }

    }

    private void deleteFileInVideoList() {
        if (idxVideoBeingChanged >= 0 && listOfVideos.size() > 0) {
            long db_id = listOfVideos.get(idxVideoBeingChanged).getDb_id();
            boolean hs = listOfVideos.get(idxVideoBeingChanged).isSlowmotion();
            addFileToUploadList(listOfVideos.get(idxVideoBeingChanged).getPath(), listOfVideos.get(idxVideoBeingChanged).getSession(), hs);
            //sendFileToMaster(listOfVideos.get(idxVideoBeingChanged).getPath(),hs,listOfVideos.get(idxVideoBeingChanged).getNumVideoFile());

            listOfVideos.remove(idxVideoBeingChanged);

            Executor myExecutor = Executors.newSingleThreadExecutor();
            myExecutor.execute(() -> {
                videoToProcessDao.deleteByKey((float) db_id);
            });

            idxVideoBeingChanged = -1;

        }
    }

    private void addFileToUploadList(String path, Long session, boolean hs) {
        if (isDriveEnabled) {
            String file_out;
            if (hs) file_out = path.replace(".mp4", "_sm.mp4");
            else file_out = path.replace(".mp4", "_.mp4");
            Log.d(TAG, "ADDING FILE to UPLOAD --> " + file_out);
            listOfVideosToUpload.add(new VideoItem(file_out, session, 0, 0, null, 0));
        }
    }

    private void deleteVideoFile(String path) {
        if (path != null) {
            File fdelete = new File(path);
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    //Log.d(TAG,"file Deleted :" + path);
                } else {
                    Log.w(TAG, "file not Deleted :" + path);
                }
            }

        }
    }

    private void deleteVideoFile2(String path) {
        if (path != null) {
            File fdelete = new File(path);
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    //Log.d(TAG,"file Deleted :" + path);
                } else {
                    Log.w(TAG, "file not Deleted :" + path);
                }
            }
            fdelete = new File(path.replace(".mp4", "_.mp4"));
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    //Log.d(TAG,"file Deleted :" + path);
                } else {
                    Log.w(TAG, "file not Deleted :" + path);
                }
            }
            fdelete = new File(path.replace(".mp4", "sm.mp4"));
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    //Log.d(TAG,"file Deleted :" + path);
                } else {
                    Log.w(TAG, "file not Deleted :" + path);
                }
            }
        }
    }

    private boolean getNextFileToUpload() {
        if (isDriveEnabled) {
            if (listOfVideosToUpload.size() > 0 && !uploadingFile) {
                strVideoBeingUploaded = listOfVideosToUpload.get(0).getPath();
                Log.d(TAG, "UPLOAD FILE --> " + strVideoBeingUploaded);
                idxVideoBeingUploaded = 0;
                return true;
            }
        }
        return false;
    }

    private void deleteFileToUploadList(int idx) {
        if (isDriveEnabled) {
            if (idxVideoBeingUploaded >= 0 && idxVideoBeingUploaded < listOfVideosToUpload.size()) {
                listOfVideosToUpload.remove(idx);
                idxVideoBeingUploaded = -1;
            }
        }
    }

    /*************************** UPLOADING *****************************************************/

    private void startUploadingVideo() {
        if (isDriveEnabled) {
            if (getNextFileToUpload() || uploadingFile) {
                Log.d(TAG, "STARTING **************** TO DRIVE:" + listOfVideosToUpload.size());
                uploadingFile = true;

                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

                Log.d(TAG, "" + account.getAccount().name);

                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(
                                this, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());
                Drive googleDriveService =
                        new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("Pikkucam")
                                .build();

                mDriveServiceHelper = new DriveServiceHelper(googleDriveService);

                if (id == null) {
                    mDriveServiceHelper.createFolderIfNotExist("Pikkucam", null)
                            .addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                                @Override
                                public void onSuccess(GoogleDriveFileHolder googleDriveFileHolder) {
                                    Gson gson = new Gson();
                                    Log.d(TAG, "onSuccessCreate:" + gson.toJson(googleDriveFileHolder));
                                    id = googleDriveFileHolder.getId();
                                    //mDriveServiceHelper.uploadFile(new File(strVideoBeingUploaded),mDriveServiceHelper.TYPE_VIDEO,id);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Gson gson = new Gson();
                                    Log.d(TAG, "onFailureCreate: " + e.getMessage());
                                }
                            });
                }

                if (id != null) {
                    mDriveServiceHelper.uploadFile(new File(strVideoBeingUploaded), mDriveServiceHelper.TYPE_VIDEO, id)
                            .addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                                @Override
                                public void onSuccess(GoogleDriveFileHolder googleDriveFileHolder) {
                                    Gson gson = new Gson();
                                    Log.d(TAG, "onSuccessFile: " + gson.toJson(googleDriveFileHolder));
                                    deleteFileToUploadList(idxVideoBeingUploaded);
                                    deleteVideoFile(getApplicationContext().getFilesDir() + "/Pikkucam/" + googleDriveFileHolder.getName());
                                    uploadingFile = false;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailureFile: " + e.getMessage());
                                    deleteFileToUploadList(idxVideoBeingUploaded);
                                    uploadingFile = false;
                                }
                            });
                }
            }
        }
    }

    /************************** COLLAGE-MOSAIC ***********************************/

    private void deleteMulticameraItem(int idx) {
        multicameraItems.remove(idx);
    }

    private int checkIfMosaicReady() {
        for (MulticameraItem item : multicameraItems) {
            if (item.isReady()) {
                return multicameraItems.indexOf(item);
            }
        }
        return -1;
    }

    private void startMakingMosaic(int idx) {
        String basepath;
        boolean hs = multicameraItems.get(idx).isSlowmotion();

        makingMosaic = true;
        idxMosaicBeingCreated = idx;
        makeMosaic(multicameraItems.get(idx).getMaxItems(), idx, hs);

    }

    public void makeMosaic(int numDevices, int idx, boolean hs) {
        String[] inputs = new String[numDevices];

        for (int i = 0; i < numDevices; i++) {
            String aux = multicameraItems.get(idx).getPathItem(i);
            if (i == 0) {
                //Desde los remotos llegan ya con el cambio
                if (hs) inputs[i] = aux.replace(".mp4", "_sm.mp4");
                else inputs[i] = aux.replace(".mp4", "_.mp4");
            } else inputs[i] = aux;

        }
		/*
		String input0=multicameraItems.get(idx).;
		String input1=basepath.replace("pikku_0","pikku_1");*/
        int index = inputs[0].lastIndexOf('/');
        String aux = inputs[0].substring(0, index);
        String output = aux + "/pikku_mosaic_" + multicameraItems.get(idx).getNumItem() + ".mp4";
        multicameraItems.get(idx).setOutput(output);
        switch (numDevices) {
            case 2:
                //logo2Video.mosaic(2,input0,input1,null,null,output);
                Log.d(TAG, "Making Mosaic 2:" + inputs[0] + " " + inputs[1] + " o:" + output);
                if (inputs[0] != null && inputs[1] != null && output != null) {

                    if (isDriveEnabled) {
                        Log.d(TAG, "ADDING FILE to UPLOAD --> " + inputs[1]);
                        listOfVideosToUpload.add(new VideoItem(inputs[1], sessionNumber, 0, 0, null, 0));
                    }


                    logo2Video.mosaic(numDevices, inputs[0], inputs[1], null, null, output);
                } else Log.w(TAG, "Error Mosaic2");
                break;

            case 3:
                //String input2=basepath.replace("pikku_0","pikku_2");
                //logo2Video.mosaic(2,input0,input1,input2,null,output);
                Log.d(TAG, "Making Mosaic 3:" + inputs[0] + " " + inputs[1] + " " + inputs[2] + " o:" + output);
                if (inputs[0] != null && inputs[1] != null && inputs[2] != null && output != null) {

                    if (isDriveEnabled) {
                        Log.d(TAG, "ADDING FILE to UPLOAD --> " + inputs[1]);
                        listOfVideosToUpload.add(new VideoItem(inputs[1], sessionNumber, 0, 0, null, 0));
                        Log.d(TAG, "ADDING FILE to UPLOAD --> " + inputs[2]);
                        listOfVideosToUpload.add(new VideoItem(inputs[2], sessionNumber, 0, 0, null, 0));
                    }

                    logo2Video.mosaic(numDevices, inputs[0], inputs[1], inputs[2], null, output);
                }
                break;

            case 4:
                //String input2_2=basepath.replace("pikku_0","pikku_2");
                //String input3=basepath.replace("pikku_0","pikku_3");
                //logo2Video.mosaic(2,input0,input1,input2_2,input3,output);
                Log.d(TAG, "Making Mosaic 3:" + inputs[0] + " " + inputs[1] + " " + inputs[2] + " " + inputs[3] + " o:" + output);
                if (inputs[0] != null && inputs[1] != null && inputs[2] != null && inputs[3] != null && output != null) {

                    if (isDriveEnabled) {
                        Log.d(TAG, "ADDING FILE to UPLOAD --> " + inputs[1]);
                        listOfVideosToUpload.add(new VideoItem(inputs[1], sessionNumber, 0, 0, null, 0));
                        Log.d(TAG, "ADDING FILE to UPLOAD --> " + inputs[2]);
                        listOfVideosToUpload.add(new VideoItem(inputs[2], sessionNumber, 0, 0, null, 0));
                        Log.d(TAG, "ADDING FILE to UPLOAD --> " + inputs[3]);
                        listOfVideosToUpload.add(new VideoItem(inputs[3], sessionNumber, 0, 0, null, 0));
                    }

                    logo2Video.mosaic(numDevices, inputs[0], inputs[1], inputs[2], inputs[3], output);
                }
                break;


        }

    }

    public void updateMulticameraItem(int numdev, int numfile, String path) {
        int idx = -1;
        for (MulticameraItem item : multicameraItems) {
            if (item.getNumItem() == numfile) {
                idx = multicameraItems.indexOf(item);
                break;
            }
        }

        if (idx > -1) {
            MulticameraItem m = multicameraItems.get(idx);
            m.setPathItem(numdev, path);
            m.setRxStatus(numdev, true);
            if (!m.isReady()) {
                m.setRxItems(m.getRxItems() + 1);
                m.setRxStatus(numdev, true);
            }
        }
    }

    public class LocalBinder extends Binder {
        public FullService getService() {
            Log.d("Service", "localBinder");
            return FullService.this;
        }
    }
}