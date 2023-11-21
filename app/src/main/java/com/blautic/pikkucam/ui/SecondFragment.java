package com.blautic.pikkucam.ui;

import static com.blautic.pikkucam.ble.BlueREMDevice.BLE_STATUS;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.api.response.Model;
import com.blautic.pikkucam.ble.BlueREMDevice;
import com.blautic.pikkucam.cfg.CfgDef;
import com.blautic.pikkucam.cfg.CfgVals;
import com.blautic.pikkucam.databinding.FragmentSecondBinding;
import com.blautic.pikkucam.db.ProfileSettings;
import com.blautic.pikkucam.db.ProfileSettingsDao;
import com.blautic.pikkucam.service.FullService;
import com.blautic.pikkucam.ui.configactivity.ContentActivityFragment;
import com.blautic.pikkucam.ui.galery.GalleryFragment;
import com.blautic.pikkucam.ui.inference.InfoInferenceFragment;
import com.blautic.pikkucam.ui.userguide.GuideFragment;
import com.blautic.pikkucam.viewmodel.MainViewModel;
import com.blautic.pikkucam.viewmodel.TrainingViewModel;
import com.blautic.pikkucam.widget.CustomSpinner;
import com.blautic.trainingapp.android.tensorflow.MpuMotionDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;


@AndroidEntryPoint
public class SecondFragment extends Fragment {

    public static final String NUMBER_VIDEO_OK = "NUMBER_VIDEO_OK";
    public static final String NUMBER_VIDEO_ERROR = "NUMBER_VIDEO_ERROR";
    public static final String PIKKU_SAVE_VIDEO = "PIKKU_SAVE_VIDEO";
    public static final String NUMBER_VIDEO_MODIFIED = "NUMBER_VIDEO_MODIFIED";
    public static final String BLE_NOTIFY = "BTCSCORE_BLE_NOTIFY";
    //Sonido
    private static SoundPool soundPool;
    private final String TAG = "SecondFragment";
    public FragmentSecondBinding binding;
    public FullService mBoundService;
    public int bateria;
    public int conexion;
    public boolean macChanged = false;
    /*****************************SERVICE*******************************/
    public ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mBoundService = ((FullService.LocalBinder) service).getService();
            mBoundService.startConnections();

            if (mBoundService != null) {
                //mBoundService.sessionNumber = sessionNumber;
                mBoundService.sessionNumber = 1;
                if (mBoundService.getTargetDevice(1) != null) {
                    if (mBoundService.getTargetDevice(1).isConnected()) {
                        bateria = mBoundService.getTargetDevice(1).battery;
                        conexion = 1;
                        binding.imageViewConexion.setImageResource(R.drawable.recurso_conexion_3);
                        if (bateria < 33) {
                            binding.imageViewBateria.setImageResource(R.drawable.recurso_bateria_1);
                        } else if (bateria < 66) {
                            binding.imageViewBateria.setImageResource(R.drawable.recurso_bateria_2);
                        } else {
                            binding.imageViewBateria.setImageResource(R.drawable.recurso_bateria_3);
                        }
                    } else {
                        binding.imageViewBateria.setImageResource(R.drawable.recurso_bateria_0);
                        binding.imageViewConexion.setImageResource(R.drawable.recurso_conexion_0);

                        bateria = 0;
                        conexion = 0;
                    }
                } else {

                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }

    };

    NewMainActivity act;
    Fragment thisfragment;
    int[][] iBtns = new int[][]{{0, 0}, {0, 0}};
    MainViewModel viewModel;
    boolean isBlack = false;
    String initSubtitle = "";


    private final List<MpuMotionDetector> mpuMotionDetectors = new ArrayList<>();

    BroadcastReceiver receiverInference = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            for (MpuMotionDetector mpuMotionDetector : mpuMotionDetectors) {
                if (mpuMotionDetector != null) {
                    Log.d("MPU", "MPU");
                    mpuMotionDetector.onMpuChanged(intent.getParcelableExtra("data"));
                }
            }

        }
    };

    private ProfileSettingsDao profileSettingsDao;
    private ProfileSettings profileSettings;
    private List<ProfileSettings> profileSettingsArray = new ArrayList<>();
    private ArrayList<String> profileNames;
    private CameraFragment cameraFragment;

    //BigSignal
    private boolean bigSignalOnLong = false;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BlueREMDevice.BLE_READY: {
                    if (mBoundService != null) {
                        bateria = mBoundService.getTargetDevice(1).battery;
                        conexion = 1;
                        binding.imageViewConexion.setImageResource(R.drawable.recurso_conexion_3);

                        boolean ble5 = intent.getBooleanExtra("ble5", true);
                        profileSettings.setBl5(ble5);

                        Executor myExecutor = Executors.newSingleThreadExecutor();
                        myExecutor.execute(() -> profileSettingsDao.insert(profileSettings));

                        viewModel.setConnexionLiveData(R.drawable.recurso_conexion_3);

                        updateBatteryViews(bateria);
                    }
                    break;
                }
                case BlueREMDevice.BLE_DISC: {
                    profileSettings.setFirmwareVersion(0);
                    Executor myExecutor = Executors.newSingleThreadExecutor();
                    myExecutor.execute(() -> profileSettingsDao.insert(profileSettings));

                    viewModel.setConnexionLiveData(R.drawable.recurso_bateria_0);
                    viewModel.setBatteryLiveData(R.drawable.recurso_conexion_0);

                    uiSwapBigSignal(false);
                    binding.imageViewBateria.setImageResource(R.drawable.recurso_bateria_0);
                    binding.imageViewConexion.setImageResource(R.drawable.recurso_conexion_0);
                    updateSettingsFragmentViews(R.drawable.recurso_conexion_0, R.drawable.recurso_bateria_0);

                    bateria = 0;
                    conexion = 0;
                    break;
                }
                case BlueREMDevice.BLE_READ: {
                    int device = intent.getIntExtra("team", -1);
                    int btn = intent.getIntExtra("btn", -1);
                    int val = intent.getIntExtra("data", -1);
                    int dur = intent.getIntExtra("dur", 0);

                    updateGreenScreenBackground(dur);
                    viewModel.setConnectionLiveData(true);
                    Log.d(TAG, "BLE_READ");
                    mngButton(device, btn, val, dur);
                    break;
                }
                case BlueREMDevice.BLE_PHY: {
                    boolean phy = intent.getBooleanExtra("phy", false);
                    binding.longRange.setVisibility(phy ? View.VISIBLE : View.INVISIBLE);
                    break;
                }
                case PIKKU_SAVE_VIDEO: {
                    profileSettings.setIs_mov_recognized(false);
                    Executor myExecutor = Executors.newSingleThreadExecutor();
                    myExecutor.execute(() -> {
                        profileSettingsDao.insert(profileSettings);
                        cameraFragment.saveVideo();
                    });
                    break;
                }
                case NUMBER_VIDEO_OK: {
                    Timber.d("HA FUNCIONADO BIEN");
                    break;
                }
                case NUMBER_VIDEO_ERROR: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("No Se ha podido guardar el video");
                    builder.setPositiveButton("OK", (dialogInterface, i) -> {
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    Timber.d("HA FUNCIONADO MAL");
                    break;
                }
                case BLE_STATUS: {
                    int battery = intent.getIntExtra("batt", 0);
                    updateBatteryViews(battery);
                    break;
                }
                case BlueREMDevice.BLE_FIRMWARE_VERSION: {
                    if (mBoundService != null) {
                        int firmwareVersion = intent.getIntExtra("firmware", 0);
                        boolean ble5 = intent.getBooleanExtra("ble5", true);
                        Log.d("FIRMWARE VERSION", String.valueOf(firmwareVersion));
                        profileSettings.setFirmwareVersion(firmwareVersion);
                        profileSettings.setBl5(ble5);

                        Executor myExecutor = Executors.newSingleThreadExecutor();
                        myExecutor.execute(() -> profileSettingsDao.insert(profileSettings));
                    }
                    break;
                }
            }
        }

        private void updateBatteryViews(int battery) {
            int batteryImageResource;
            if (battery < 33) {
                batteryImageResource = R.drawable.recurso_bateria_1;
            } else if (battery < 66) {
                batteryImageResource = R.drawable.recurso_bateria_2;
            } else {
                batteryImageResource = R.drawable.recurso_bateria_3;
            }

            viewModel.setBatteryLiveData(batteryImageResource);
            binding.imageViewBateria.setImageResource(batteryImageResource);
            updateSettingsFragmentViews(batteryImageResource, batteryImageResource);
        }

        private void updateSettingsFragmentViews(int conexionImageResource, int batteryImageResource) {
            for (Fragment f : cameraFragment.getChildFragmentManager().getFragments()) {
                if (f instanceof SettingsFragment) {
                    ((SettingsFragment) f).binding.imageViewConexion.setImageResource(conexionImageResource);
                    ((SettingsFragment) f).binding.imageViewBateria.setImageResource(batteryImageResource);
                }
            }
        }

        private void updateGreenScreenBackground(int dur) {
            int greenScreenBackgroundColor = dur > 2000 ? getResources().getColor(R.color.PIKKU_MAGENTA) : getResources().getColor(R.color.PIKKU_GREEN);
            binding.greenScreen.setBackgroundColor(greenScreenBackgroundColor);
        }
    };

    //private List<Movement> selectedMovements = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonContinueGuide.setVisibility(View.GONE);
        act = (NewMainActivity) getActivity();
        viewModel.setStepLiveData(0);

        IntentFilter Filter = new IntentFilter(BlueREMDevice.BLE_DISC);
        Filter.addAction(BlueREMDevice.BLE_READY);
        Filter.addAction(BlueREMDevice.BLE_READ);
        Filter.addAction(PIKKU_SAVE_VIDEO);
        Filter.addAction(BlueREMDevice.BLE_FIRMWARE_VERSION);
        Filter.addAction(BlueREMDevice.BLE_STATUS);
        Filter.addAction(BlueREMDevice.BLE_PHY);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isFirstTimeGuide = sharedPreferences.getBoolean("GuideUserFirstTime", true);

        if (isFirstTimeGuide) {
            sharedPreferences.edit().putBoolean("GuideUserFirstTime", false).apply();

            GuideFragment dialog = new GuideFragment();
            dialog.setCancelable(false);
            dialog.show(this.getParentFragmentManager(), "Dialog");

            LocalBroadcastManager.getInstance(act).unregisterReceiver(receiver);

            binding.buttonPlay.setVisibility(View.GONE);
            binding.buttonSettings.setVisibility(View.GONE);
            binding.buttonConnect.setVisibility(View.GONE);
            binding.ConstraintLayoutSpinner.setVisibility(View.GONE);
            binding.ConstraintLayoutSpinnerAI.setVisibility(View.VISIBLE);
            binding.guideText.setVisibility(View.GONE);

            viewModel.getStepLiveData().observe(getViewLifecycleOwner(), step -> {
                if (step == 5) {
                    binding.buttonSettings.performClick();
                }
                if (step == 6) {
                    new Handler().postDelayed(() -> {
                        GuideFragment dialog1 = new GuideFragment();
                        dialog1.setCancelable(false);
                        dialog1.show(getParentFragmentManager(), "Dialog");
                    }, 200);
                }
                if (step == 7) {
                    binding.buttonPlay.setVisibility(View.GONE);
                    binding.buttonSettings.setVisibility(View.GONE);
                    binding.buttonConnect.setVisibility(View.GONE);
                    binding.ConstraintLayoutSpinner.setVisibility(View.GONE);
                    binding.ConstraintLayoutSpinnerAI.setVisibility(View.VISIBLE);
                    binding.guideText.setVisibility(View.VISIBLE);
                }
                if (step == 9) {
                    binding.buttonPlay.performClick();
                }
                if (step == 10) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            GuideFragment dialog = new GuideFragment();
                            dialog.setCancelable(false);
                            dialog.show(getParentFragmentManager(), "Dialog");
                        }
                    }, 200);
                }
            });

            getParentFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                    super.onFragmentViewDestroyed(fm, f);
                    getParentFragmentManager().unregisterFragmentLifecycleCallbacks(this);

                    if (viewModel.getStepLiveData().getValue() != 6) {
                        binding.buttonPlay.setVisibility(View.VISIBLE);
                        binding.buttonSettings.setVisibility(View.VISIBLE);
                        binding.buttonConnect.setVisibility(View.VISIBLE);
                        binding.ConstraintLayoutSpinner.setVisibility(View.VISIBLE);
                        binding.ConstraintLayoutSpinnerAI.setVisibility(View.VISIBLE);
                    } else {
                        binding.guideText.setVisibility(View.VISIBLE);
                    }
                    LocalBroadcastManager.getInstance(act).registerReceiver(receiver, Filter);
                }
            }, false);

        } else {
            binding.buttonPlay.setVisibility(View.VISIBLE);
            binding.buttonSettings.setVisibility(View.VISIBLE);
            binding.buttonConnect.setVisibility(View.VISIBLE);
            binding.ConstraintLayoutSpinner.setVisibility(View.VISIBLE);
            binding.ConstraintLayoutSpinnerAI.setVisibility(View.VISIBLE);
            binding.guideText.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(act).registerReceiver(receiver, Filter);
        }


        binding.infoIaButton.setOnClickListener(view1 -> {
            Fragment infoInferenceFragment = new InfoInferenceFragment();
            FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
            transaction.add(R.id.second_fragment_layout, infoInferenceFragment);
            transaction.hide(thisfragment);
            transaction.show(infoInferenceFragment);
            transaction.commit();
        });


        binding.buttonInfo.setOnClickListener(view1 -> {
            Fragment infoFragment = new InfoFragment();
            FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
            transaction.add(R.id.second_fragment_layout, infoFragment);
            transaction.hide(thisfragment);
            transaction.commit();
        });

        binding.ConstraintLayoutSpinnerAI.setOnClickListener(view13 -> {
            viewModel.clearSelectedModels();
            viewModel.setIsSessionActive(false);
            viewModel.clearThresholdInferences();
            mpuMotionDetectors.clear();
            binding.infoIaButton.setVisibility(View.VISIBLE);
            Fragment activitySelectionFragment = new ContentActivityFragment();
            FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
            transaction.add(R.id.second_fragment_layout, activitySelectionFragment);
            transaction.hide(thisfragment);
            transaction.commit();
        });

        viewModel.getThresholdInferences().observe(getViewLifecycleOwner(), floats -> {

            for (int i = 0; i < floats.size(); i++) {
                Float aFloat = floats.get(i);
                Log.d("UMBRAL INDICE " + i, "umbral: " + aFloat);
            }

            Log.d("CAMBIOS THREASHOLD", "CAMBIOS THREADSHOLSD");
            if (floats.size() == mpuMotionDetectors.size()){
                for (int i = 0; i < mpuMotionDetectors.size(); i++) {
                    mpuMotionDetectors.get(i).setThreshold(floats.get(i));
                }
            }
        });

        viewModel.getIsSessionActive().observe(getViewLifecycleOwner(), isSessionActive -> {
            Log.d("IS SESSION ACTIVE", isSessionActive.toString());

            //TODO SI ESTA ACTIVA EMPEZAR LAS INFERENECIAS
            if (isSessionActive) {

                IntentFilter filter = new IntentFilter(BlueREMDevice.BLE_READ_SENSOR);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiverInference, filter);

                if (mpuMotionDetectors.isEmpty()) {
                    for (int i = 0; i < viewModel.getSelectedModelsFinal().size(); i++) {
                        Model model = viewModel.getSelectedModelsFinal().get(i);
                        initMotionDetector(i, model);
                    }
                } else {

                    for (MpuMotionDetector mpuMotionDetector : mpuMotionDetectors) {
                        mpuMotionDetector.stop();
                    }

                    mpuMotionDetectors.clear();
                    for (int i = 0; i < viewModel.getSelectedModelsFinal().size(); i++) {
                        Model model = viewModel.getSelectedModelsFinal().get(i);
                        initMotionDetector(i, model);
                    }
                }
            } else {
                for (MpuMotionDetector mpuMotionDetector : mpuMotionDetectors) {
                    mpuMotionDetector.stop();
                }
            }
        });

        viewModel.getSelectedModels().observe(getViewLifecycleOwner(), models -> {

            Log.d("OBSREVADO", "OBSERVADOR");
            if (models.size() == 0) {
                binding.iaActivityText.setText("Selecciona una Actividad");
            } else {
                binding.iaActivityText.setText("Actividades Seleccionadas: " + models.size());
            }
        });

        binding.buttonContinueGuide.setOnClickListener(view12 -> {
            viewModel.setStepLiveData(8);
            GuideFragment dialog = new GuideFragment();
            dialog.setCancelable(false);
            dialog.show(getParentFragmentManager(), "Dialog");
            binding.buttonPlay.setVisibility(View.VISIBLE);
            binding.buttonSettings.setVisibility(View.VISIBLE);
            binding.buttonConnect.setVisibility(View.VISIBLE);
            binding.ConstraintLayoutSpinner.setVisibility(View.VISIBLE);
            binding.ConstraintLayoutSpinnerAI.setVisibility(View.VISIBLE);
            binding.buttonContinueGuide.setVisibility(View.GONE);
            binding.guideText.setVisibility(View.GONE);
        });

        cameraFragment = (CameraFragment) getParentFragment();

        thisfragment = this;

        MainViewModel viewModel = new ViewModelProvider(act).get(MainViewModel.class);

        profileSettingsDao = viewModel.getDatabase().profileSettingsDao();

        SharedPreferences sharedPref = act.getSharedPreferences("Pikkucam", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        profileSettingsDao.getProfileSettingsLive().observe(getViewLifecycleOwner(), profileSettingsArrayData -> {
            if (sharedPref.getInt("Selected_mode", 0) < profileSettingsArrayData.size()) {
                profileSettings = profileSettingsArrayData.get(sharedPref.getInt("Selected_mode", 0));

                cameraFragment.listenProfile.setValue(profileSettings);
            } else {
                profileSettings = profileSettingsArrayData.get(0);
                cameraFragment.listenProfile.setValue(profileSettings);
                editor.putInt("Selected_mode", 0);
            }
            if (mBoundService != null) {
                mBoundService.profileSettings = profileSettings;
            }
            profileSettingsArray = profileSettingsArrayData;
            loadActualData();

            try {
                int x = sharedPref.getInt("Selected_mode", 0);
                binding.spinnerModo.setSelection(x);
            } catch (IndexOutOfBoundsException e) {
                binding.spinnerModo.setSelection(0);
            }

        });

        binding.buttonConnect.setOnClickListener(v -> {
            Fragment connectFragment = new ConnectFragment();
            FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
            transaction.add(R.id.second_fragment_layout, connectFragment);
            transaction.hide(thisfragment);
            transaction.commit();
        });

        binding.buttonSettings.setOnClickListener(v -> {
            Fragment settingsFragment = new SettingsFragment();
            FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
            transaction.add(R.id.second_fragment_layout, settingsFragment, "SettingsFragment");
            transaction.hide(thisfragment);
            transaction.commit();
        });

        binding.spinnerModo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("Selected_mode", position);
                editor.apply();
                profileSettings = profileSettingsArray.get(position);
                cameraFragment.listenProfile.setValue(profileSettings);
                if (mBoundService != null) {
                    mBoundService.profileSettings = profileSettings;
                    mBoundService.isDriveEnabled = profileSettings.isUploadToDrive();
                }
                if (!profileSettings.isSubtitleButtons() && profileSettings.isSubtitle()) {
                    binding.constraintLayoutClickTitulo.setVisibility(View.VISIBLE);
                    String text = "<b>" + profileSettings.getSubtitle() + "</b>     " + act.getString(R.string.cambiar_titulo);
                    binding.textViewClickTitulo.setText(Html.fromHtml(text));
                } else {
                    binding.constraintLayoutClickTitulo.setVisibility(View.INVISIBLE);
                }
                cameraFragment.setupDisplay();
                cameraFragment.listenProfile.setValue(profileSettings);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.drawableSpinnerEnd.setOnClickListener(v -> {
            binding.spinnerModo.performClick();
        });

        binding.drawableSpinner.setOnClickListener(v -> {
            binding.spinnerModo.performClick();
        });

        binding.spinnerModo.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(Spinner spinner) {
                binding.drawableSpinnerEnd.setImageResource(R.drawable.recurso_57);
            }

            @Override
            public void onSpinnerClosed(Spinner spinner) {
                binding.drawableSpinnerEnd.setImageResource(R.drawable.recurso_56);
            }
        });

        binding.textViewClickTitulo.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(getString(R.string.titulo_video));

            final EditText input = new EditText(act);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String text = "<b>" + input.getText().toString() + "</b>     " + act.getString(R.string.cambiar_titulo);
                binding.textViewClickTitulo.setText(Html.fromHtml(text));
                profileSettings.setSubtitle(input.getText().toString());
                Executor myExecutor = Executors.newSingleThreadExecutor();
                myExecutor.execute(() -> {
                    profileSettingsDao.insert(profileSettings);
                    if (mBoundService != null) {
                        mBoundService.profileSettings = profileSettings;
                        mBoundService.isDriveEnabled = profileSettings.isUploadToDrive();
                    }
                });
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        final boolean[] isCamera = {true};

        binding.cameraButton.setOnClickListener(v -> {
            if (isCamera[0]) {
                binding.cameraButton.setImageResource(R.drawable.recurso_66);
                binding.textViewNumero.setVisibility(View.VISIBLE);
                binding.textViewNumero.setText(String.valueOf(cameraFragment.numOfVideos));
                binding.textViewVDeos.setVisibility(View.VISIBLE);
                isCamera[0] = false;
            }
        });

        binding.buttonPlay.setOnClickListener(view1 -> {
            Fragment sessionsFragment = new GalleryFragment();
            FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
            transaction.add(R.id.second_fragment_layout, sessionsFragment);
            transaction.hide(thisfragment);
            transaction.commit();
        });

        binding.buttonChangeColor.setOnClickListener(v -> {
            if (!isBlack) {
                binding.imageViewBateria.setColorFilter(getResources().getColor(android.R.color.black));
                binding.imageViewConexion.setColorFilter(getResources().getColor(android.R.color.black));
                binding.seekBarZoom.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                binding.seekBarZoom.getThumb().setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                binding.seekBarExposure.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                binding.seekBarExposure.getThumb().setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                binding.imageViewZoom.setColorFilter(getResources().getColor(android.R.color.black));
                binding.imageViewExposure.setColorFilter(getResources().getColor(android.R.color.black));
                binding.buttonChangeColor.setColorFilter(getResources().getColor(android.R.color.white));
                isBlack = true;
            } else {
                binding.imageViewBateria.setColorFilter(getResources().getColor(android.R.color.white));
                binding.imageViewConexion.setColorFilter(getResources().getColor(android.R.color.white));
                binding.seekBarZoom.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
                binding.seekBarZoom.getThumb().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
                binding.seekBarExposure.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
                binding.seekBarExposure.getThumb().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
                binding.imageViewZoom.setColorFilter(getResources().getColor(android.R.color.white));
                binding.imageViewExposure.setColorFilter(getResources().getColor(android.R.color.white));
                binding.buttonChangeColor.setColorFilter(getResources().getColor(android.R.color.black));
                isBlack = false;
            }
        });
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
        soundPool.load(act, R.raw.beep1, 1);
        soundPool.load(act, R.raw.beep3, 2);




        binding.shootPicture.setOnClickListener(view1 -> {
            cameraFragment.saveVideo();
        });

    }

    private void loadActualData() {

        profileNames = new ArrayList<>();

        for (int i = 0; i < profileSettingsArray.size(); i++) {
            profileNames.add(profileSettingsArray.get(i).getName() == null ? "" : profileSettingsArray.get(i).getName());
        }

        if (!profileSettings.isSubtitleButtons() && profileSettings.isSubtitle()) {
            binding.constraintLayoutClickTitulo.setVisibility(View.VISIBLE);
            String text = "<b>" + profileSettings.getSubtitle() + "</b>     " + act.getString(R.string.cambiar_titulo);
            binding.textViewClickTitulo.setText(Html.fromHtml(text));

            initSubtitle = profileSettings.getSubtitle();

        } else {
            binding.constraintLayoutClickTitulo.setVisibility(View.INVISIBLE);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(act, R.layout.spinner_item, profileNames);
        binding.spinnerModo.setAdapter(adapter);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView");
        cameraFragment.stopVideo();

        cameraFragment.onPause();

        if (mBoundService != null) mBoundService.setCurrentSession(false, 0);
        //act.unbindService(mConnection);
        mBoundService = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        if (cameraFragment != null) cameraFragment.onPause();
        act = null;
        cameraFragment = null;
        try {
            LocalBroadcastManager.getInstance(act).unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void mngButton(int device, int btn, int val, int dur) {
        if (thisfragment.isHidden()) {
            return;
        }

        if (device != CfgDef.DEVICE1) {
            return;
        }

        if ((btn == 1 && (profileSettings.getCameraButtons() == 0 || profileSettings.getCameraButtons() == 2)) || (btn == 2 && (profileSettings.getCameraButtons() == 1 || profileSettings.getCameraButtons() == 2))) {

            if (val == 1 && iBtns[device - 1][btn - 1] == 0) {
                if (profileSettings.isSound()) {
                    playBeep(1, 0);
                }
                if (profileSettings.isGreenScreen()) {
                    uiSwapBigSignal(true);
                }
                iBtns[device - 1][btn - 1] = val;

            } else if (val == 0 && iBtns[device - 1][btn - 1] == 1) {
                bigSignalOnLong = false;
                iBtns[device - 1][btn - 1] = 0;
                if (profileSettings.isGreenScreen()) {
                    uiSwapBigSignal(false);
                }

                // Decidimos si ha sido larga
                String subtitle = dur < 2000 ? getShortSubtitle(btn) : getLongSubtitle(btn);
                if (profileSettings.isSubtitleButtons()) {
                    cameraFragment.subtitle = subtitle;
                }

                if (cameraFragment.isVideoRunning && cameraFragment.validVideo) {
                    binding.cameraButton.callOnClick();
                    cameraFragment.saveVideo();
                    if (cameraFragment.numOfVideos > 0) {
                        binding.guideText.setVisibility(View.GONE);
                        binding.buttonContinueGuide.setVisibility(binding.buttonPlay.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    } else {
                        binding.guideText.setVisibility(binding.buttonPlay.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                        binding.buttonContinueGuide.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private String getShortSubtitle(int btn) {
        if (btn == 1) {
            return profileSettings.getButton1_short_subtitle();
        } else if (btn == 2) {
            return profileSettings.getButton2_short_subtitle();
        }
        return "";
    }

    private String getLongSubtitle(int btn) {
        if (btn == 1) {
            return profileSettings.getButton1_long_subtitle();
        } else if (btn == 2) {
            return profileSettings.getButton2_long_subtitle();
        }
        return "";
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        doBindService();
        try {
            if (act.mIsBound && mBoundService != null && mBoundService.cfgSessionMacs != null) {
                if (!mBoundService.cfgSessionMacs.isMacEnabled(CfgVals.DEVICE1) || macChanged) {
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            IntentFilter Filter = new IntentFilter(BlueREMDevice.BLE_DISC);
            Filter.addAction(BlueREMDevice.BLE_READY);
            Filter.addAction(BlueREMDevice.BLE_READ);
            Filter.addAction(BlueREMDevice.BLE_PHY);
            Filter.addAction(BlueREMDevice.BLE_FIRMWARE_VERSION);
            Filter.addAction(BlueREMDevice.BLE_STATUS);
            Filter.addAction(PIKKU_SAVE_VIDEO);
            Filter.addAction(NUMBER_VIDEO_OK);
            Filter.addAction(NUMBER_VIDEO_ERROR);
            try {
                LocalBroadcastManager.getInstance(act).unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            if (binding.buttonPlay.getVisibility() == View.VISIBLE) {
                LocalBroadcastManager.getInstance(act).registerReceiver(receiver, Filter);
            }
        }, 1000);

    }

    void doBindService() {
        act.bindService(new Intent(act, FullService.class), mConnection, Context.BIND_AUTO_CREATE);
        act.mIsBound = true;
    }

    public void playBeep(int soundID, int repeats) {
        float volume = (float) 1.0;
        if (true) soundPool.play(soundID, volume, volume, 1, repeats, 1f);
    }

    private void uiSwapBigSignal(boolean show) {
        if (show) {
            binding.greenScreen.setVisibility(View.VISIBLE);
        } else {
            binding.greenScreen.setVisibility(View.GONE);
        }
    }

    public void initMotionDetector(int index, Model model) {

        mpuMotionDetectors.add(index, new MpuMotionDetector(model));

        mpuMotionDetectors.get(index).setMotionDetectorListener(new MpuMotionDetector.MotionDetectorListener() {

            @Override
            public void onTryMotionMotionRecognized(float correctProb) {
                if (viewModel.getCaptureTry().getValue().get(index)){
                    cameraFragment.saveVideo();
                    playBeep(1, 0);
                }
            }

            @Override
            public void onCorrectMotionRecognized(float correctProb) {
                if (!viewModel.getCaptureTry().getValue().get(index)){
                    cameraFragment.saveVideo();
                    playBeep(1, 0);
                }
            }

            @Override
            public void onOutputScores(float[] outputScores) {
                viewModel.setResultInferences(outputScores, index);

            }
        });

        mpuMotionDetectors.get(index).start();
    }

}