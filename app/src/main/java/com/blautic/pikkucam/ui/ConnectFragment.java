package com.blautic.pikkucam.ui;

import static android.content.Context.LOCATION_SERVICE;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blautic.pikkucam.ble.BleAdapter;
import com.blautic.pikkucam.databinding.FragmentConnectBinding;
import com.blautic.pikkucam.db.Devices;
import com.blautic.pikkucam.db.DevicesDao;
import com.blautic.pikkucam.service.FullService;
import com.blautic.pikkucam.viewmodel.MainViewModel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConnectFragment extends Fragment {

    boolean mIsBound = false;
    FullService mBoundService;
    String TAG = "ConnectFragment";
    //Bluetooth
    BleAdapter bleAd = null;
    //Receiver
    IntentFilter localFt = new IntentFilter();
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
    private FragmentConnectBinding binding;
    private Devices devices;
    private DevicesDao devicesDao;
    private String mac;
    private boolean isBle5;
    private boolean pikku1_on = false;
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
                    GetDevice(target, ble5);
                    //CHANGE MAC
                }


            } else if (intent.getAction().equals(BleAdapter.BLE_ADAPTER_SCAN_FINISHED)) {
                keepScanning();
            }

        }

    };
    private NewMainActivity act;

    protected LocationManager locationManager;
    // Flag for GPS status
    boolean isGPSEnabled = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentConnectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        act = (NewMainActivity) getActivity();
        doBindService();
        MainViewModel viewModel = new ViewModelProvider(act).get(MainViewModel.class);
        devicesDao = viewModel.getDatabase().devicesDao();

        if (devices == null) {
            devices = new Devices("-");
        }

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

        binding.imageViewOk.setOnClickListener(v -> {
            Executor myExecutor = Executors.newSingleThreadExecutor();
            myExecutor.execute(() -> {
                SecondFragment secondFragment = (SecondFragment) getParentFragment().getChildFragmentManager().findFragmentByTag("SecondFragment");
                if (!devicesDao.getMac1().equals(mac)) {
                    secondFragment.macChanged = true;
                    // mBoundService.reconnect = false;
                    mBoundService.changeMAC(mac);
                    //mBoundService.closeBleTargets();
                }
                devicesDao.setMac1(mac, isBle5);

                //mBoundService.reconnect = true;
                Log.d(TAG, mac);

                act.runOnUiThread(() -> {
                            secondFragment.onResume();
                            FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
                            transaction.remove(ConnectFragment.this);
                            transaction.show(secondFragment);
                            transaction.commit();
                        }
                );
            });

        });

    }

    @Override
    public void onPause() {
        bleAd.cancelScan();
        super.onPause();
    }

    private void GetDevice(String mymac, boolean isLeCodedPhySupported) {

        if (pikku1_on) {

            if (!devices.getMac1().contentEquals(mymac)) {
                devices.setMac1(mymac);
                devices.setBle5(isLeCodedPhySupported);
            }

            binding.textviewFirst.setVisibility(View.INVISIBLE);
            binding.imageViewOk.setVisibility(View.VISIBLE);

            Executor myExecutor = Executors.newSingleThreadExecutor();
            myExecutor.execute(() -> {
                devicesDao.update(devices);
            });

            try {
                bleAd.cancelScan();
                bleAd.finalize();

            } catch (Throwable t) {
                t.printStackTrace();
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
        localFt.addAction(BleAdapter.BLE_ADAPTER_SCAN_STARTED);
        localFt.addAction(BleAdapter.BLE_ADAPTER_SCAN_FINISHED);

        LocalBroadcastManager.getInstance(act).registerReceiver(localReceiver, localFt);
    }

    private void keepScanning() {
        boolean scan = false;
        if (!pikku1_on) scan = true;

        if (scan) {
            try {
                bleAd.scanAllDevices();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        bleAd.cancelScan();
        try {
            LocalBroadcastManager.getInstance(act).unregisterReceiver(localReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        super.onDestroyView();

    }

    private void doBindService() {
        act.bindService(new Intent(act, FullService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void removeFragment() {
        Log.d(TAG, getParentFragment().getClass().getName());
        SecondFragment secondFragment = (SecondFragment) getParentFragment().getChildFragmentManager().findFragmentByTag("SecondFragment");
        SettingsFragment settingsFragment = (SettingsFragment) getParentFragment().getChildFragmentManager().findFragmentByTag("SettingsFragment");
        FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
        if (settingsFragment != null) {
            transaction.show(settingsFragment);
        } else if (secondFragment != null) {
            transaction.show(secondFragment);
        }
        transaction.remove(ConnectFragment.this);
        transaction.commit();
    }


}