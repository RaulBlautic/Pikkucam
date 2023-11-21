package com.blautic.pikkucam.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputType;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blautic.pikkucam.PikkuCamApplication;
import com.blautic.pikkucam.R;
import com.blautic.pikkucam.ble.BlueREMDevice;
import com.blautic.pikkucam.databinding.FragmentSettingsBinding;
import com.blautic.pikkucam.db.ProfileSettings;
import com.blautic.pikkucam.db.ProfileSettingsDao;
import com.blautic.pikkucam.service.FullService;
import com.blautic.pikkucam.video.VideoTools;
import com.blautic.pikkucam.viewmodel.MainViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {
    FragmentSettingsBinding binding;
    String TAG = "SETTINGS_FRAGMENT";
    RadioButton b;
    GoogleSignInClient mGoogleSignInClient;
    boolean incorrectData = false;
    boolean changed = false;
    SettingsFragment thisFragment;
    CameraFragment cameraFragment;
    SecondFragment secondFragment;
    private ProfileSettingsDao profileSettingsDao;
    private ProfileSettings profileSettings;
    private List<ProfileSettings> profileSettingsArray = new ArrayList<ProfileSettings>();
    private List<String> profileNames = new ArrayList<String>();
    private Executor myExecutor;
    private boolean isAdapter = false;
    private VideoTools videoTools;
    private int RC_SIGN_IN = 9001;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private FullService mBoundService;
    private String advertPath;
    private NewMainActivity act;
    private MainViewModel viewModel;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (bluetoothAdapter.isLeCodedPhySupported()) {
            binding.longRangeText.setVisibility(View.VISIBLE);
            binding.longRange.setVisibility(View.VISIBLE);
        } else {
            binding.longRangeText.setVisibility(View.INVISIBLE);
            binding.longRange.setVisibility(View.INVISIBLE);
        }

        act = (NewMainActivity) getActivity();

        cameraFragment = (CameraFragment) getParentFragment();

        viewModel = new ViewModelProvider(act).get(MainViewModel.class);

        viewModel.getSettingChangedLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                binding.buttonSave.setColorFilter(Color.rgb(253, 185, 21));
            } else {
                binding.buttonSave.setColorFilter(Color.rgb(255, 255, 255));
            }
        });


        try {
            String versionName = act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName;
            binding.textViewVersion.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        MainViewModel viewModel = new ViewModelProvider(act).get(MainViewModel.class);

        profileSettingsDao = viewModel.getDatabase().profileSettingsDao();

        SharedPreferences sharedPref = act.getSharedPreferences("Pikkucam", Context.MODE_PRIVATE);

        editor = sharedPref.edit();

        myExecutor = Executors.newSingleThreadExecutor();

        thisFragment = this;

        videoTools = ((PikkuCamApplication) act.getApplicationContext()).vtools;

        profileSettingsDao.getProfileSettingsLive().observe(getViewLifecycleOwner(), profileSettingsArrayData -> {
            int pos = sharedPref.getInt("Selected_mode", 0);
            if (pos < profileSettingsArrayData.size()) {
                profileSettings = profileSettingsArrayData.get(pos);
                cameraFragment.listenProfile.setValue(profileSettings);
            } else {
                profileSettings = profileSettingsArrayData.get(0);
                cameraFragment.listenProfile.setValue(profileSettings);
            }
            profileSettingsArray = profileSettingsArrayData;
            profileNames = new ArrayList<>();
            for (int i = 0; i < profileSettingsArray.size(); i++) {
                profileNames.add(profileSettingsArray.get(i).getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(act, R.layout.spinner_item, profileNames);

            isAdapter = true;
            binding.spinnerModo.setAdapter(adapter);

            if (pos < profileSettingsArrayData.size()) {
                binding.spinnerModo.setSelection(sharedPref.getInt("Selected_mode", 0));
            } else {
                binding.spinnerModo.setSelection(0);
            }

            String text = getString(R.string.modo) + " <b>" + profileSettings.getName() + "</b>";

            binding.buttonCardviewModo.setText(Html.fromHtml(text));
            binding.spinnerModo.setSelection(sharedPref.getInt("Selected_mode", 0));

            loadActualData();

            if (profileSettings != null){
                if (profileSettings.getFirmwareVersion() == 0) {
                    binding.firmwareVersionText.setText("Firmware version: " + "---");
                } else if (profileSettings.isBl5()){
                    binding.firmwareVersionText.setText("Firmware version: " + "5." + profileSettings.getFirmwareVersion());
                } else {
                    binding.firmwareVersionText.setText("Firmware version: " + "4." + profileSettings.getFirmwareVersion());
                }
            }

        });

        secondFragment = (SecondFragment) cameraFragment.getChildFragmentManager().findFragmentByTag("SecondFragment");

        binding.buttonSettings.setOnClickListener(v -> removeFragment());

        View.OnClickListener listenerModo = v -> {
            if (binding.hiddenView.getVisibility() == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenView.setVisibility(View.GONE);
                binding.imagenBotonModo.setImageResource(R.drawable.cardview_abierto);
            } else {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenView.setVisibility(View.VISIBLE);
                binding.imagenBotonModo.setImageResource(R.drawable.cardview_cerrado);
            }
        };

        viewModel.getStepLiveData().observe(getViewLifecycleOwner(), step -> {
            if (step == 5) {
                binding.buttonSiguiente.setVisibility(View.VISIBLE);

            } else {
                binding.buttonSiguiente.setVisibility(View.GONE);
            }
        });

        binding.buttonSiguiente.setOnClickListener(view1 -> {
            viewModel.setStepLiveData(6);
            saveSettings();
            getActivity().onBackPressed();
        });

        binding.buttonCardviewModo.setOnClickListener(listenerModo);

        binding.imagenBotonModo.setOnClickListener(listenerModo);

        binding.spinnerModo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = getString(R.string.modo) + " <b>" + binding.spinnerModo.getSelectedItem().toString() + "</b>";
                binding.buttonCardviewModo.setText(Html.fromHtml(text));
                if (!isAdapter) {
                    profileSettings = profileSettingsArray.get(position);
                    editor.putInt("Selected_mode", position);
                    //Log.d(TAG,"Selected mode " + String.valueOf(position));
                    editor.apply();
                    loadActualData();
                    if (mBoundService != null) {
                        mBoundService.profileSettings = profileSettings;
                        mBoundService.isDriveEnabled = profileSettings.isUploadToDrive();
                    }
                    cameraFragment.setupDisplay();
                    cameraFragment.listenProfile.setValue(profileSettings);
                } else {
                    isAdapter = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        View.OnClickListener listenerCamara = v -> {
            if (binding.hiddenViewCamara.getVisibility() == View.VISIBLE) {
                // The transition of the hiddenView is carried out
                //  by the TransitionManager class.
                // Here we use an object of the AutoTransition
                // Class to create a default transition.
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewCamara.setVisibility(View.GONE);
                binding.imagenBotonCamara.setImageResource(R.drawable.cardview_abierto);
            } else {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewCamara.setVisibility(View.VISIBLE);
                binding.imagenBotonCamara.setImageResource(R.drawable.cardview_cerrado);
            }
        };

        b = getView().findViewById(binding.radioGroupCamara.getCheckedRadioButtonId());

        String text = getString(R.string.camara) + " <b>" + b.getText() + "</b>";

        binding.buttonCardviewCamara.setText(Html.fromHtml(text));

        binding.buttonCardviewCamara.setOnClickListener(listenerCamara);

        binding.imagenBotonCamara.setOnClickListener(listenerCamara);

        binding.radioGroupCamara.setOnCheckedChangeListener((group, checkedId) -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);
            b = getView().findViewById(checkedId);
            String text16 = getString(R.string.camara) + " <b>" + b.getText() + "</b>";
            binding.buttonCardviewCamara.setText(Html.fromHtml(text16));

            if (b.getText().equals(getString(R.string.frontal))) {
                if (binding.buttonSlowMotion.isChecked() && !videoTools.isHighSpeedAvailable(CameraCharacteristics.LENS_FACING_FRONT)) {
                    binding.buttonSlowMotion.setChecked(false);
                    binding.buttonSlowMotion.setEnabled(false);
                    Toast.makeText(act, R.string.camara_no_slowmotion, Toast.LENGTH_SHORT);
                } else if (!binding.buttonSlowMotion.isChecked() && !videoTools.isHighSpeedAvailable(CameraCharacteristics.LENS_FACING_FRONT)) {
                    binding.buttonSlowMotion.setEnabled(false);
                } else {
                    binding.buttonSlowMotion.setEnabled(true);
                }
            } else {
                if (binding.buttonSlowMotion.isChecked() && !videoTools.isHighSpeedAvailable(CameraCharacteristics.LENS_FACING_BACK)) {
                    binding.buttonSlowMotion.setChecked(false);
                    binding.buttonSlowMotion.setEnabled(false);
                    Toast.makeText(act, R.string.camara_no_slowmotion, Toast.LENGTH_SHORT);
                } else if (!binding.buttonSlowMotion.isChecked() && !videoTools.isHighSpeedAvailable(CameraCharacteristics.LENS_FACING_BACK)) {
                    binding.buttonSlowMotion.setEnabled(false);
                } else {
                    binding.buttonSlowMotion.setEnabled(true);
                }
            }
        });

        if (b.getText().equals(getString(R.string.frontal))) {
            if (binding.buttonSlowMotion.isChecked() && !videoTools.isHighSpeedAvailable(CameraCharacteristics.LENS_FACING_FRONT)) {
                binding.buttonSlowMotion.setChecked(false);
                binding.buttonSlowMotion.setEnabled(false);
                Toast.makeText(act, R.string.camara_no_slowmotion, Toast.LENGTH_SHORT);
            } else if (!binding.buttonSlowMotion.isChecked() && !videoTools.isHighSpeedAvailable(CameraCharacteristics.LENS_FACING_FRONT)) {
                binding.buttonSlowMotion.setEnabled(false);
            } else {
                binding.buttonSlowMotion.setEnabled(true);
            }
        } else {
            if (binding.buttonSlowMotion.isChecked() && !videoTools.isHighSpeedAvailable(CameraCharacteristics.LENS_FACING_BACK)) {
                binding.buttonSlowMotion.setChecked(false);
                binding.buttonSlowMotion.setEnabled(false);
                Toast.makeText(act, R.string.camara_no_slowmotion, Toast.LENGTH_SHORT);
            } else if (!binding.buttonSlowMotion.isChecked() && !videoTools.isHighSpeedAvailable(CameraCharacteristics.LENS_FACING_BACK)) {
                binding.buttonSlowMotion.setEnabled(false);
            } else {
                binding.buttonSlowMotion.setEnabled(true);
            }
        }

        View.OnClickListener listenerCalidad = v -> {
            if (binding.hiddenViewCalidad.getVisibility() == View.VISIBLE) {
                // The transition of the hiddenView is carried out
                //  by the TransitionManager class.
                // Here we use an object of the AutoTransition
                // Class to create a default transition.
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewCalidad.setVisibility(View.GONE);
                binding.imagenBotonCalidad.setImageResource(R.drawable.cardview_abierto);
            } else {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewCalidad.setVisibility(View.VISIBLE);
                binding.imagenBotonCalidad.setImageResource(R.drawable.cardview_cerrado);
            }
        };

        View.OnClickListener listenerMultiCam = v -> {
            if (binding.hiddenViewMulticamera.getVisibility() == View.VISIBLE) {
                // The transition of the hiddenView is carried out
                //  by the TransitionManager class.
                // Here we use an object of the AutoTransition
                // Class to create a default transition.
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewMulticamera.setVisibility(View.GONE);
                binding.imagenBotonMulticamera.setImageResource(R.drawable.cardview_abierto);
            } else {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewMulticamera.setVisibility(View.VISIBLE);
                binding.imagenBotonMulticamera.setImageResource(R.drawable.cardview_cerrado);
            }
        };

        binding.buttonCardviewMulticamera.setOnClickListener(listenerMultiCam);

        binding.imagenBotonMulticamera.setOnClickListener(listenerMultiCam);

        b = getView().findViewById(binding.radioGroupCalidad.getCheckedRadioButtonId());

        text = getString(R.string.calidad) + " <b>" + b.getText() + "</b>";

        binding.buttonCardviewCalidad.setText(Html.fromHtml(text));

        binding.buttonCardviewCalidad.setOnClickListener(listenerCalidad);

        binding.imagenBotonCalidad.setOnClickListener(listenerCalidad);

        binding.radioGroupCalidad.setOnCheckedChangeListener((group, checkedId) -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);
            b = (RadioButton) getView().findViewById(checkedId);
            String text15 = getString(R.string.calidad) + " <b>" + b.getText() + "</b>";
            binding.buttonCardviewCalidad.setText(Html.fromHtml(text15));
        });

        binding.radioButtonBlue.setOnCheckedChangeListener((compoundButton, b1) -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);

            String text15 = getString(R.string.enable_pikku_buttons)  + " <b>" + getString(R.string.button_blue) + "</b>";

            if (b1 && binding.radioButtonRed.isChecked()){
                String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.two_buttons) + "</b>";
                binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
                String text142 = "<b>" + getString(R.string.two_buttons) + "</b>";
                binding.buttons.setText(Html.fromHtml(text142));
            } else if (!b1 && binding.radioButtonRed.isChecked()){
                String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.button_red) + "</b>";
                binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
                String text142 = "<b>" + "("+getString(R.string.button_red)+")" + "</b>";
                binding.buttons.setText(Html.fromHtml(text142));
            } else if (b1 && !binding.radioButtonRed.isChecked()){
                String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.button_blue) + "</b>";
                binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
                String text142 = "<b>" + "("+getString(R.string.button_blue)+")" + "</b>";
                binding.buttons.setText(Html.fromHtml(text142));
            } else {
                String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.two_buttons) + "</b>";
                binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
                String text142 = "<b>" + getString(R.string.two_buttons) + "</b>";
                binding.buttons.setText(Html.fromHtml(text142));
            }
        });

        binding.radioButtonRed.setOnCheckedChangeListener((compoundButton, b1) -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);
            String text15 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.button_red) + "</b>";
            if (b1 && binding.radioButtonBlue.isChecked()){
                String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.two_buttons) + "</b>";
                binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
                String text142 = "<b>" + getString(R.string.two_buttons) + "</b>";
                binding.buttons.setText(Html.fromHtml(text142));
            } else if (!b1 && binding.radioButtonBlue.isChecked()){
                String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.button_blue) + "</b>";
                binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
                String text142 = "<b>" + "(" + getString(R.string.button_blue)+")" + "</b>";
                binding.buttons.setText(Html.fromHtml(text142));
            } else if (b1 && !binding.radioButtonBlue.isChecked()){
                String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.button_red) + "</b>";
                binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
                String text142 = "<b>" + "("+getString(R.string.button_red)+")" + "</b>";
                binding.buttons.setText(Html.fromHtml(text142));
            } else {
                String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.two_buttons) + "</b>";
                binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
                String text142 = "<b>" + getString(R.string.two_buttons) + "</b>";
                binding.buttons.setText(Html.fromHtml(text142));
            }
        });

        View.OnClickListener listenerDuracion = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.hiddenViewDuracion.getVisibility() == View.VISIBLE) {
                    // The transition of the hiddenView is carried out
                    //  by the TransitionManager class.
                    // Here we use an object of the AutoTransition
                    // Class to create a default transition.
                    TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                            new AutoTransition());
                    binding.hiddenViewDuracion.setVisibility(View.GONE);
                    binding.imagenBotonDuracion.setImageResource(R.drawable.cardview_abierto);
                } else {
                    TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                            new AutoTransition());
                    binding.hiddenViewDuracion.setVisibility(View.VISIBLE);
                    binding.imagenBotonDuracion.setImageResource(R.drawable.cardview_cerrado);
                }
            }
        };

        text = getString(R.string.duracion) + " <b>" + binding.editTextDuracion.getText().toString() + getString(R.string.sigla_tiempo) + "</b>";

        binding.buttonCardviewDuracion.setText(Html.fromHtml(text));

        binding.buttonCardviewDuracion.setOnClickListener(listenerDuracion);

        binding.imagenBotonDuracion.setOnClickListener(listenerDuracion);

        binding.editTextDuracion.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    // the user is done typing.
                    changed = true;
                    viewModel.setSettingChangedLiveData(true);
                    String textoEdit = binding.editTextDuracion.getText().toString();
                    String text14 = getString(R.string.duracion) + " <b>" + textoEdit + getString(R.string.sigla_tiempo) + "</b>";
                    try {
                        if (Integer.parseInt(textoEdit) > 120) {
                            Toast.makeText(act, getString(R.string.duracion_maxima_info), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    } catch (final NumberFormatException e) {
                        Toast.makeText(act, getString(R.string.numero_invalido), Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    binding.buttonCardviewDuracion.setText(Html.fromHtml(text14));
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
            }
            return false;
        });

        View.OnClickListener listenerPublicidad = v -> {

            if (binding.hiddenViewPublicidad.getVisibility() == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewPublicidad.setVisibility(View.GONE);
                binding.imagenBotonPublicidad.setImageResource(R.drawable.cardview_abierto);
            } else {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewPublicidad.setVisibility(View.VISIBLE);
                binding.imagenBotonPublicidad.setImageResource(R.drawable.cardview_cerrado);
            }
        };

        b = getView().findViewById(binding.radioGroupPublicidad.getCheckedRadioButtonId());

        text = getString(R.string.publicidad) + " <b>" + b.getText() + "</b>";

        binding.buttonCardviewPublicidad.setText(Html.fromHtml(text));

        binding.buttonCardviewPublicidad.setOnClickListener(listenerPublicidad);

        binding.imagenBotonPublicidad.setOnClickListener(listenerPublicidad);

        binding.radioGroupPublicidad.setOnCheckedChangeListener((group, checkedId) -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);
            b = getView().findViewById(checkedId);
            String text13 = getString(R.string.publicidad) + " <b>" + b.getText() + "</b>";
            binding.buttonCardviewPublicidad.setText(Html.fromHtml(text13));
            if (b.isChecked() && b.equals(binding.radioButtonPublicidadActivado) && profileSettings.getAdvert_file() != null) {
                binding.imageViewSelectAdvert.setImageURI(Uri.parse(profileSettings.getAdvert_file()));
            } else {
                binding.imageViewSelectAdvert.setImageDrawable(null);
            }
        });

        View.OnClickListener listenerDrive = v -> {
            if (binding.hiddenViewDrive.getVisibility() == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewDrive.setVisibility(View.GONE);
                binding.imagenBotonDrive.setImageResource(R.drawable.cardview_abierto);
            } else {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewDrive.setVisibility(View.VISIBLE);
                binding.imagenBotonDrive.setImageResource(R.drawable.cardview_cerrado);
                if (GoogleSignIn.getLastSignedInAccount(act) != null) {
                    binding.textviewConfirmacionCuenta.setText(GoogleSignIn.getLastSignedInAccount(act).getEmail());
                }
            }
        };

        b = (RadioButton) getView().findViewById(binding.radioGroupDrive.getCheckedRadioButtonId());

        text = getString(R.string.guardado_drive) + " <b>" + b.getText() + "</b>";

        binding.buttonCardviewDrive.setText(Html.fromHtml(text));

        binding.buttonCardviewDrive.setOnClickListener(listenerDrive);

        binding.imagenBotonDrive.setOnClickListener(listenerDrive);

        binding.radioGroupDrive.setOnCheckedChangeListener((group, checkedId) -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);
            b = (RadioButton) getView().findViewById(checkedId);
            String text12 = getString(R.string.guardado_drive) + " <b>" + b.getText() + "</b>";
            binding.buttonCardviewDrive.setText(Html.fromHtml(text12));
        });

        View.OnClickListener listenerSubtitulo = v -> {
            if (binding.hiddenCardviewSubtitulo.getVisibility() == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenCardviewSubtitulo.setVisibility(View.GONE);
                binding.imagenBotonSubtitulo.setImageResource(R.drawable.cardview_abierto);
            } else {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenCardviewSubtitulo.setVisibility(View.VISIBLE);
                binding.imagenBotonSubtitulo.setImageResource(R.drawable.cardview_cerrado);
            }
        };

        b = getView().findViewById(binding.radioGroupSubtitulo.getCheckedRadioButtonId());

        text = getString(R.string.subtitulos) + " <b>" + b.getText() + "</b>";

        binding.buttonCardviewSubtitulo.setText(Html.fromHtml(text));

        binding.buttonCardviewSubtitulo.setOnClickListener(listenerSubtitulo);

        binding.imagenBotonSubtitulo.setOnClickListener(listenerSubtitulo);

        binding.radioGroupSubtitulo.setOnCheckedChangeListener((group, checkedId) -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);
            b = (RadioButton) getView().findViewById(checkedId);
            String text1 = getString(R.string.subtitulos) + " <b>" + b.getText() + "</b>";
            binding.buttonCardviewSubtitulo.setText(Html.fromHtml(text1));

            if (b.getText().toString().contains(getText(R.string.activado1))) {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewSubtitulo1.setVisibility(View.VISIBLE);
                binding.hiddenViewSubtitulo4.setVisibility(View.GONE);
            } else if (b.getText().toString().contains(getText(R.string.activado4))) {
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewSubtitulo1.setVisibility(View.GONE);
                binding.hiddenViewSubtitulo4.setVisibility(View.VISIBLE);
            } else {
                //Log.d(TAG, "NO CONDITION");
                TransitionManager.beginDelayedTransition(binding.cardviewsContainer,
                        new AutoTransition());
                binding.hiddenViewSubtitulo1.setVisibility(View.GONE);
                binding.hiddenViewSubtitulo4.setVisibility(View.GONE);
            }
        });

        binding.buttonSelectAdvert.setOnClickListener(v -> imageChooser());

        binding.buttonGoogleAccount.setOnClickListener(v -> {
            binding.textviewConfirmacionCuenta.setText(R.string.introducir_cuenta_google);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(Scopes.DRIVE_FILE))
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(act, gso);
            mGoogleSignInClient.signOut();

            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        binding.buttonAdd.setOnClickListener(v -> crearNuevoModo());

        binding.buttonSave.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(getString(R.string.confirmar_guardado));

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> saveSettings());
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        });

        binding.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileSettingsArray.size() != 1) {
                    editor.putInt("Selected_mode", 0);
                    editor.apply();
                    EliminarModo();
                } else {
                    Toast.makeText(act, getText(R.string.eliminar_ultimo), Toast.LENGTH_SHORT).show();
                }

            }
        });

        binding.imageViewBateria.setImageDrawable(secondFragment.binding.imageViewBateria.getDrawable());

        binding.imageViewConexion.setImageDrawable(secondFragment.binding.imageViewConexion.getDrawable());

        binding.buttonSlowMotion.setOnClickListener(v -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);
        });

        binding.buttonSound.setOnClickListener(v -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);
        });

        binding.buttonGreenScreen.setOnClickListener(v -> {
            changed = true;
            viewModel.setSettingChangedLiveData(true);
        });
    }

    private void saveSettings() {
        b = (RadioButton) getView().findViewById(binding.radioGroupCamara.getCheckedRadioButtonId());

        if (b.getText().equals(getString(R.string.frontal))) {
            profileSettings.setCamera(CameraCharacteristics.LENS_FACING_FRONT);
        } else {
            profileSettings.setCamera(CameraCharacteristics.LENS_FACING_BACK);

        }

        profileSettings.setSlowMotion(binding.buttonSlowMotion.isChecked());

        b = (RadioButton) getView().findViewById(binding.radioGroupCalidad.getCheckedRadioButtonId());

        if (b.getText().equals(getString(R.string.baja))) {
            profileSettings.setVideoQuality(0);
        } else if (b.getText().equals(getString(R.string.media))) {
            profileSettings.setVideoQuality(1);
        } else if (b.getText().equals(getString(R.string.alta))) {
            profileSettings.setVideoQuality(2);
        } else {
            profileSettings.setVideoQuality(3);
        }

        if (binding.radioButtonBlue.isChecked() && !binding.radioButtonRed.isChecked()) {
            profileSettings.setCameraButtons(0);
        } else if (!binding.radioButtonBlue.isChecked() && binding.radioButtonRed.isChecked()) {
            profileSettings.setCameraButtons(1);
        } else {
            profileSettings.setCameraButtons(2);
        }

        profileSettings.setVideo_duration(binding.editTextDuracion.getText().toString());

        b = (RadioButton) getView().findViewById(binding.radioGroupPublicidad.getCheckedRadioButtonId());

        if (b.getText().equals(getString(R.string.activado))) {
            if (binding.imageViewSelectAdvert.getDrawable() != null) {
                profileSettings.setShowPubli(true);
                if (advertPath != null) {
                    profileSettings.setAdvert_file(advertPath);
                }
            } else if (profileSettings.getAdvert_file() != null) {
                profileSettings.setShowPubli(true);
            } else {
                Toast.makeText(act, getString(R.string.publicidad_no_introducida), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            profileSettings.setShowPubli(false);
        }

        b = (RadioButton) getView().findViewById(binding.radioGroupDrive.getCheckedRadioButtonId());

        if (b.getText().equals(getString(R.string.activado)) && GoogleSignIn.getLastSignedInAccount(act) != null) {
            profileSettings.setUploadToDrive(true);
        } else if (b.getText().equals(getString(R.string.activado))) {
            Toast.makeText(act, getString(R.string.cuenta_no_introducida), Toast.LENGTH_LONG).show();
            incorrectData = true;
        } else {
            profileSettings.setUploadToDrive(false);
        }

        b = (RadioButton) getView().findViewById(binding.radioGroupSubtitulo.getCheckedRadioButtonId());
        if (b.getText().equals(getString(R.string.activado1))) {
            profileSettings.setSubtitle(binding.editTextSubtitle.getText().toString());
            profileSettings.setIsSubtitle(true);
            profileSettings.setSubtitleButtons(false);
        } else if (b.getText().equals(getString(R.string.activado4))) {
            profileSettings.setSubtitleButtons(true);
            profileSettings.setIsSubtitle(true);
            profileSettings.setButton1_short_subtitle(binding.editTextSubtitle1Short.getText().toString());
            profileSettings.setButton2_short_subtitle(binding.editTextSubtitle2Short.getText().toString());
            profileSettings.setButton1_long_subtitle(binding.editTextSubtitle1Long.getText().toString());
            profileSettings.setButton2_long_subtitle(binding.editTextSubtitle2Long.getText().toString());
        } else {
            profileSettings.setSubtitleButtons(false);
            profileSettings.setIsSubtitle(false);
        }

        profileSettings.setName(binding.spinnerModo.getSelectedItem().toString());
        //profileSettings.setMovimientos(binding.buttonMovements.isChecked());
        profileSettings.setSound(binding.buttonSound.isChecked());
        profileSettings.setGreenScreen(binding.buttonGreenScreen.isChecked());

        if (!incorrectData) {
            if (mBoundService != null) {
                mBoundService.profileSettings = profileSettings;
                mBoundService.isDriveEnabled = profileSettings.isUploadToDrive();
            }
            myExecutor.execute(() -> {
                profileSettingsDao.insert(profileSettings);
            });
            cameraFragment.assigned = false;
            cameraFragment.listenProfile.setValue(profileSettings);
            Toast.makeText(act, R.string.cambios_guardados, Toast.LENGTH_SHORT).show();
            changed = false;
            viewModel.setSettingChangedLiveData(false);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void loadActualData() {

        int camera = profileSettings.getCamera();
        if (camera == 0) {
            binding.radioButtonCamaraFrontal.setChecked(true);
            binding.radioButtonCamaraTrasera.setChecked(false);
        } else {
            binding.radioButtonCamaraFrontal.setChecked(false);
            binding.radioButtonCamaraTrasera.setChecked(true);
        }

        int quality = profileSettings.getVideoQuality();
        if (quality == 0) {
            binding.radioButtonCalidadAlta.setChecked(false);
            binding.radioButtonCalidadMedia.setChecked(false);
            binding.radioButtonCalidadBaja.setChecked(true);
        } else if (quality == 1) {
            binding.radioButtonCalidadAlta.setChecked(false);
            binding.radioButtonCalidadMedia.setChecked(true);
            binding.radioButtonCalidadBaja.setChecked(false);
        } else if(quality == 2){
            binding.radioButtonCalidadAlta.setChecked(true);
            binding.radioButtonCalidadMedia.setChecked(false);
            binding.radioButtonCalidadBaja.setChecked(false);
        }

        int multiCamSettings = profileSettings.getCameraButtons();

        if (multiCamSettings == 0) {
            binding.radioButtonBlue.setChecked(true);
            binding.radioButtonRed.setChecked(false);
            String text142 = "<b>" + "(" + getString(R.string.button_blue) +")" + "</b>";
            String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.button_blue) + "</b>";
            binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
            binding.buttons.setText(Html.fromHtml(text142));
        } else if (multiCamSettings == 1) {
            binding.radioButtonBlue.setChecked(false);
            binding.radioButtonRed.setChecked(true);
            String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.button_red) + "</b>";
            String text142 = "<b>" + "("+getString(R.string.button_red)+")" + "</b>";
            binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
            binding.buttons.setText(Html.fromHtml(text142));
        } else {
            binding.radioButtonBlue.setChecked(true);
            binding.radioButtonRed.setChecked(true);
            String text14 = getString(R.string.enable_pikku_buttons) + " <b>" + getString(R.string.two_buttons) + "</b>";
            binding.buttonCardviewMulticamera.setText(Html.fromHtml(text14));
            String text142 = "<b>" + getString(R.string.two_buttons) + "</b>";
            binding.buttons.setText(Html.fromHtml(text142));
        }

        String duration = profileSettings.getVideo_duration();
        binding.editTextDuracion.setText(duration);
        String text = getString(R.string.duracion) + " <b>" + duration + getString(R.string.sigla_tiempo) + "</b>";
        binding.buttonCardviewDuracion.setText(Html.fromHtml(text));

        if (profileSettings.isShowPubli()) {
            binding.radioButtonPublicidadActivado.setChecked(true);
            binding.radioButtonPublicidadDesactivado.setChecked(false);
            String img = profileSettings.getAdvert_file();
            if (img != null) {
                binding.imageViewSelectAdvert.setImageURI(Uri.parse(img));
            } else {
                binding.imageViewSelectAdvert.setImageDrawable(null);
            }
        } else {
            binding.imageViewSelectAdvert.setImageDrawable(null);
            binding.radioButtonPublicidadActivado.setChecked(false);
            binding.radioButtonPublicidadDesactivado.setChecked(true);
        }

        if (profileSettings.isUploadToDrive()) {
            binding.radioButtonDriveActivado.setChecked(true);
            binding.radioButtonDriveDesactivado.setChecked(false);
        } else {
            binding.radioButtonDriveActivado.setChecked(false);
            binding.radioButtonDriveDesactivado.setChecked(true);
        }

        if (profileSettings.isSubtitle() && !profileSettings.isSubtitleButtons()) {
            binding.radioButtonSubtituloActivado1.setChecked(true);
            binding.radioButtonSubtituloActivado4.setChecked(false);
            binding.radioButtonSubtituloDesactivado.setChecked(false);
            binding.editTextSubtitle.setText(profileSettings.getSubtitle());
        } else if (profileSettings.isSubtitleButtons()) {
            binding.radioButtonSubtituloActivado1.setChecked(false);
            binding.radioButtonSubtituloActivado4.setChecked(true);
            binding.radioButtonSubtituloDesactivado.setChecked(false);
            binding.editTextSubtitle1Long.setText(profileSettings.getButton1_long_subtitle());
            binding.editTextSubtitle1Short.setText(profileSettings.getButton1_short_subtitle());
            binding.editTextSubtitle2Short.setText(profileSettings.getButton2_short_subtitle());
            binding.editTextSubtitle2Long.setText(profileSettings.getButton2_long_subtitle());
        } else {

            binding.radioButtonSubtituloDesactivado.setChecked(true);
            binding.radioButtonSubtituloActivado1.setChecked(false);
            binding.radioButtonSubtituloActivado4.setChecked(false);
        }

        if (profileSettings.isGreenScreen()) {
            //Log.d(TAG,"HOLA");
            binding.buttonGreenScreen.setChecked(true);
        } else {
            binding.buttonGreenScreen.setChecked(false);
        }
        if (profileSettings.isSound()) {
            binding.buttonSound.setChecked(true);
        } else {
            binding.buttonSound.setChecked(false);
        }
        if (profileSettings.isSlowMotion()) {
            binding.buttonSlowMotion.setChecked(true);
        } else {
            binding.buttonSlowMotion.setChecked(false);
        }
        if (mBoundService != null) {
            mBoundService.profileSettings = profileSettings;
            mBoundService.isDriveEnabled = profileSettings.isUploadToDrive();
        }
        //binding.buttonMovements.setChecked(profileSettings.getMovimientos());
        changed = false;
        viewModel.setSettingChangedLiveData(false);
    }

    private void crearNuevoModo() {

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(getString(R.string.modo_nombre));

        // Set up the input
        final EditText input = new EditText(act);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine();
        FrameLayout container = new FrameLayout(act);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 20;
        params.rightMargin = 20;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {

            editor.putInt("Selected_mode", profileSettingsArray.size());
            editor.apply();
            String nombre = input.getText().toString();
            profileSettings = new ProfileSettings();
            profileSettings.setName(nombre);
            profileSettings.setSlowMotion(false);
            profileSettings.setSound(true);
            profileSettings.setGreenScreen(true);
            profileSettings.setCamera(0);
            profileSettings.setIsSubtitle(true);
            profileSettings.setVideo_duration("10");
            profileSettings.setSubtitle("subtitulo");
            profileSettings.setButton1_short_subtitle("subtitulo");
            profileSettings.setButton1_short_subtitle("subtitulo");
            profileSettings.setButton1_short_subtitle("subtitulo");
            profileSettings.setButton1_short_subtitle("subtitulo");
            profileSettings.setUploadToDrive(false);
            profileSettings.setShowPubli(false);
            profileSettings.setVideoQuality(1);
            profileSettings.setCameraButtons(2);
            profileSettings.setMovimientos(false);

            if (mBoundService != null) {
                mBoundService.profileSettings = profileSettings;
                mBoundService.isDriveEnabled = false;
            }

            myExecutor.execute(() -> {
                profileSettingsDao.insert(profileSettings);
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();

    }

    public void removeFragment() {
        if (changed) {
            final boolean[] answer = new boolean[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(getString(R.string.confirmar_salir));
            builder.setPositiveButton("OK", (dialog, which) -> {
                FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
                transaction.remove(thisFragment);
                transaction.show(secondFragment);
                transaction.commit();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
            });

            builder.show();

        } else {
            FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
            transaction.remove(thisFragment);
            transaction.show(secondFragment);
            transaction.commit();
        }
    }

    private void EliminarModo() {

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(getString(R.string.modo_eliminar, profileSettings.getName()));

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                myExecutor.execute(() -> {
                    profileSettingsDao.delete(profileSettings.getId());
                });

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void imageChooser() {
        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 200);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 200) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(act.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    advertPath = saveToInternalStorage(bitmap);
                    binding.imageViewSelectAdvert.setImageBitmap(bitmap);
                    changed = true;
                    viewModel.setSettingChangedLiveData(true);
                }
            }

            if (requestCode == RC_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }

        } else {

            Log.d(TAG, "Error");
            Log.d(TAG, String.valueOf(resultCode));

        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(act.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = new File(act.getFilesDir() + "/Pikkucam/AdvertisingFiles");
        // Create imageDir
        Log.d(TAG, act.getFilesDir() + "Pikkucam/AdvertisingFiles");
        File mypath = new File(directory, profileSettings.getName() + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            binding.textviewConfirmacionCuenta.setText(account.getEmail());
            changed = true;
            viewModel.setSettingChangedLiveData(true);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(act, getString(R.string.error_sign_in), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
