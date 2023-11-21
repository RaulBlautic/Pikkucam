package com.blautic.pikkucam.ui.userguide;

import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.ble.BleAdapter;
import com.blautic.pikkucam.ble.BlueREMDevice;
import com.blautic.pikkucam.databinding.PageFragmentStepOneBinding;
import com.blautic.pikkucam.db.Devices;
import com.blautic.pikkucam.db.DevicesDao;
import com.blautic.pikkucam.service.FullService;
import com.blautic.pikkucam.ui.NewMainActivity;
import com.blautic.pikkucam.viewmodel.MainViewModel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FirstPageFragment extends Fragment {
    int step;
    MainViewModel viewModel;
    PageFragmentStepOneBinding binding;
    BleAdapter bleAd = null;
    IntentFilter localFt = new IntentFilter();
    private NewMainActivity act;
    private boolean pikku1_on = false;
    private String mac;
    private boolean isBle5;
    private Devices devices;
    private DevicesDao devicesDao;

    protected LocationManager locationManager;
    // Flag for GPS status
    boolean isGPSEnabled = false;

    boolean mIsBound = false;

    FullService mBoundService;

    ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((FullService.LocalBinder) service).getService();
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }

    };

    BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BleAdapter.BLE_ADAPTER_DEVICE_CLICK)) {
                String target = intent.getStringExtra("mac");
                boolean ble5 = intent.getBooleanExtra("ble5", false);

                if (!pikku1_on) {
                    pikku1_on = true;
                    mac = target;
                    isBle5 = ble5;
                    viewModel.saveDevice(target);
                    GetDevice(target, ble5);
                    mBoundService.changeMAC(mac);

                }

            } else if (intent.getAction().equals(BlueREMDevice.BLE_READY)){
                viewModel.setStepLiveData(3);
            }

        }

    };

    public FirstPageFragment() {

    }

    public static FirstPageFragment newInstance(int step) {
        FirstPageFragment firstPageFragment = new FirstPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("step", step);
        firstPageFragment.setArguments(bundle);
        return firstPageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        step = getArguments().getInt("step", 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        act = (NewMainActivity) getActivity();
        binding = PageFragmentStepOneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        doBindService();
        binding.button.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.buttonImage.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        binding.logoImage.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.imagenPikku.setVisibility(step == 3 ? View.VISIBLE : View.GONE);


        MainViewModel viewModelMain = new ViewModelProvider(act).get(MainViewModel.class);

        devicesDao = viewModelMain.getDatabase().devicesDao();

        if (devices == null) {
            devices = new Devices("-");
            devices.setId(1);
        }

        binding.button.setOnClickListener(view1 -> {
            viewModel.setStepLiveData(step);
            step++;
        });

        binding.buttonImage.setOnClickListener(view12 -> {
            viewModel.setStepLiveData(step);
            step++;
        });


        switch (step) {
            case 1: {
                binding.layout.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            }
            case 2: {
                binding.layout.setGravity(Gravity.CENTER);
                binding.tittle.setText("Vincula tu dispositivo");
                binding.subtittle.setText("Pulsa el icono inferior");
                break;
            }
            case 3: {
                locationManager = (LocationManager) getContext()
                        .getSystemService(LOCATION_SERVICE);
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (!isGPSEnabled) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    // Setting Dialog Title
                    alertDialog.setTitle("GPS no Activado");
                    // Setting Dialog Message
                    alertDialog.setMessage("el GPS no está activado. ¿Quieres dirigirte al menu de ajustes?");
                    // On pressing the Settings button.
                    alertDialog.setPositiveButton("Ajustes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            getContext().startActivity(intent);
                        }
                    });
                    // On pressing the cancel button
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    // Showing Alert Message
                    alertDialog.show();
                }
                initBLE();
                setLocalReceiver();
                binding.tittle.setText("Vincula tu dispositivo");
                binding.subtittle.setText("Manten pulsado el botón del Pikku para enlazar");
                break;
            }
            case 4: {
                binding.tittle.setText("¡Listo!");
                binding.subtittle.setText("Ahora tu Pikku y móvil o tablet \n están conectados");
                binding.buttonImage.setVisibility(View.VISIBLE);
                binding.buttonImage.setBackgroundResource(R.drawable.recurso_conectar);
                break;
            }
            case 5: {
                binding.tittle.setText("Ajusta el modo de grabación");
                binding.subtittle.setText("Echa un ojo a las opciones y configura \n el tiempo de grabación");
                binding.buttonImage.setVisibility(View.VISIBLE);
                binding.buttonImage.setBackgroundResource(R.drawable.recurso_settings);
                break;
            }
            case 6: {
                binding.tittle.setText("");
                binding.subtittle.setText("");
                break;
            }
            case 7: {
                binding.tittle.setText("Graba tu primer vídeo");
                binding.subtittle.setText("Pulsa el icono de grabación");
                binding.buttonImage.setVisibility(View.VISIBLE);
                binding.buttonImage.setBackgroundResource(R.drawable.recurso_65);
                break;
            }
            case 8: {
                binding.tittle.setText("");
                binding.subtittle.setText("");
                binding.buttonImage.setVisibility(View.GONE);
                break;
            }
            case 9: {
                binding.tittle.setText("Previsualizar vídeos");
                binding.subtittle.setText("El icono de play te permite acceder a la \n videoteca en la que podrás previsualizar los vídeos");
                binding.buttonImage.setVisibility(View.VISIBLE);
                binding.buttonImage.setBackgroundResource(R.drawable.recurso_play);
                break;
            }
            case 10: {
                binding.tittle.setText("¡Listo! ");
                binding.subtittle.setText("Ahora ya sabes como usar PikkuCam \n mediante pulsadores");
                binding.buttonImage.setVisibility(View.VISIBLE);
                binding.buttonImage.setBackgroundResource(R.drawable.recurso_ok);
                break;
            }
            case 11: {
                binding.tittle.setText("¡Listo!");
                binding.subtittle.setText("Ahora ya sabes como usar PikkuCam \n mediante pulsadores");
                binding.buttonImage.setVisibility(View.VISIBLE);
                binding.buttonImage.setBackgroundResource(R.drawable.recurso_ok);
                break;
            }
            case 12: {
                binding.tittle.setText("¡Próximamente con reconocimiento \n de movimientos!");
                binding.subtittle.setText("Podrás grabar videos en base a movimientos \n " +
                        " previamente entrenados con Inteligencia Artificial");
                binding.buttonImage.setVisibility(View.VISIBLE);
                binding.buttonImage.setBackgroundResource(R.drawable.ic_recurso_74);
                break;
            }
        }
    }

    public void initBLE() {
        if (bleAd == null) {
            try {
                bleAd = new BleAdapter();
                bleAd.initialize(act);
                if (bleAd.checkBluetooth()) bleAd.scanAllDevices();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void setLocalReceiver() {
        localFt.addAction(BleAdapter.BLE_ADAPTER_DEVICE_CLICK);
        localFt.addAction(BlueREMDevice.BLE_READY);

        LocalBroadcastManager.getInstance(act).registerReceiver(localReceiver, localFt);
    }

    private void doBindService() {
        act.bindService(new Intent(act, FullService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void GetDevice(String mymac, boolean isLeCodedPhySupported) {
        if (pikku1_on) {
            if (!devices.getMac1().contentEquals(mymac)) {
                devices.setMac1(mymac);
                devices.setBle5(isLeCodedPhySupported);
            }

            Executor myExecutor = Executors.newSingleThreadExecutor();
            myExecutor.execute(() -> {
                devicesDao.update(devices);
                viewModel.setConnectionLiveData(true);
            });
            try {
                bleAd.cancelScan();
                bleAd.finalize();

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

}