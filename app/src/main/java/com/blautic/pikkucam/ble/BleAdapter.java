package com.blautic.pikkucam.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

public class BleAdapter {

    public static final String BLE_ADAPTER_SCAN_FINISHED = "SCAN_FINISHED";
    public static final String BLE_ADAPTER_SCAN_STARTED = "SCAN_STARTED";
    public static final String BLE_ADAPTER_DEVICE_CLICK = "DEVICE_CLICK";
    private final String TAG = "BleAdapter";
    private Context _context;
    private BluetoothAdapter _bluetoothAdapter;
    private String macAddress;
    private Boolean scanning = false;
    private final Handler scanHandler = new Handler();
    private BluetoothLeScanner mLEScanner;
    private BLEScanCallback mScanCallback;
    private final boolean finded = false;
    private boolean receiverRegistered = false;

    /* Initialize adapter ******************************************************************/

    public BleAdapter() throws Exception {
        if (BluetoothAdapter.getDefaultAdapter() == null)
            throw new Exception("Device does not support bluetooth");

    }

    public Boolean checkBluetooth() {
        // check if the bluetooth has Low Energy Feature
        if (!this._context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this._context, "DEVICE_NOT_SUPORTED", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    @SuppressLint("MissingPermission")
    public void initialize(Context _context) {
        this._context = _context;
        this._bluetoothAdapter = ((BluetoothManager) _context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        this.macAddress = this._bluetoothAdapter.getAddress();

        mLEScanner = _bluetoothAdapter.getBluetoothLeScanner();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

        if (!receiverRegistered) {
            _context.registerReceiver(mReceiver, filter);
            receiverRegistered = true;
        }
        Log.d(TAG, _bluetoothAdapter.isEnabled() + "");
    }


    @Override
    public void finalize() throws Throwable {
        super.finalize();
        if (receiverRegistered) {
            _context.unregisterReceiver(mReceiver);
            receiverRegistered = false;
        }
    }


    @SuppressLint("MissingPermission")
    public void turnOffBlueAdapter() {
        _bluetoothAdapter.disable();
    }
    /* SCAN ********************************************************************************/

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }

    @SuppressLint("MissingPermission")
    private void startScanning() {
        mScanCallback = new BLEScanCallback();
        mLEScanner.startScan(null, buildScanSettings(), mScanCallback);
    }

    public Boolean scanAllDevices() throws Exception {
        Log.d("BleAdapter", "Scanning BLE ...");
        scanHandler.removeCallbacks(stopScan);
        scanHandler.postDelayed(stopScan, 5000);
        scanning = true;
        startScanning();
        return scanning;
    }

    @SuppressLint("MissingPermission")
    public void cancelScan() {
        if (mLEScanner != null && _bluetoothAdapter.isEnabled()) {
            mLEScanner.stopScan(mScanCallback);
        }
        scanning = false;

    }

    public void stopReceiver() {
        _context.unregisterReceiver(mReceiver);
    }

    public Boolean isBusy() {
        return scanning;
    }

    public Boolean isEnabled() {
        return this._bluetoothAdapter.isEnabled();
    }

    public BluetoothDevice bluetoothDeviceFactory(String mac) {
        return this._bluetoothAdapter.getRemoteDevice(mac);
    }

    public String getMacAddress() {
        return macAddress;
    }

    private void checkIfTarget(String mac, byte[] scan) {
        if ((scan[bleUUID.POS_ID_DEVICE] == bleUUID.TAG_ID_DEVICE ||
                scan[bleUUID.POS_ID_DEVICE] == bleUUID.TAG_ID_BL5) && (
                scan[bleUUID.POS_ID_DEVICE + 1] == bleUUID.INT_FRAME_ID_SCORE ||
                        scan[bleUUID.POS_ID_DEVICE + 1] == bleUUID.INT_FRAME_ID_SCORE_BL5 ||
                        scan[bleUUID.POS_ID_DEVICE + 1] == bleUUID.INT_FRAME_ID_SCORE_PIKKU_ACADEMY)) {

            String conc = "";
            for (byte value : scan)
                conc = conc.concat(" " + (value & 0xff));
                Log.d("CONC", conc);
            if (scan[bleUUID.POS_TAG_BTN] == bleUUID.INT_DEVICE_BTN) {
                //BTN1
                int btn1 = (scan[bleUUID.POS_TAG_BTN + 2] & 0xFF);
                int btn2 = 0;

                if (btn1 == 1) {
                    boolean isBle5 = scan[bleUUID.POS_ID_DEVICE] == bleUUID.TAG_ID_BL5;
                    Intent intent = new Intent(BLE_ADAPTER_DEVICE_CLICK);
                    intent.putExtra("mac", mac);
                    intent.putExtra("ble5", isBle5);
                    intent.putExtra("btn1", btn1);
                    intent.putExtra("btn2", btn2);
                    //intent.putExtra("version",version);
                    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                }
            }
        }
    }

    /* UTILS ******************************************************************************/
    private void keepScanning() {
        if (!finded) {
            boolean scan = true;
            if (scan) {
                try {
                    scanAllDevices();
                } catch (Exception e) {

                }
            }
        }
    }

    private class BLEScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            checkIfTarget(result.getDevice().getAddress(), result.getScanRecord().getBytes());

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult result : results) {
                checkIfTarget(result.getDevice().getAddress(), result.getScanRecord().getBytes());
            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "Scan FAILED ");
        }
    }

    private final Runnable stopScan = new Runnable() {
        public void run() {
            Log.d("BleAdapter", "STOP...");
            if (isBusy()) {
                cancelScan();
                LocalBroadcastManager.getInstance(_context).sendBroadcast(new Intent(BLE_ADAPTER_SCAN_FINISHED));
                keepScanning();
            }
        }
    };


    /* BLUETOOTH BROADCAST RECEIVER **************************************************************************/
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "Bluetooth off");
                        //_bluetoothAdapter.enable();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "Bluetooth on");
                        keepScanning();
                        LocalBroadcastManager.getInstance(_context).sendBroadcast(new Intent(BlueREMDevice.BLE_DISC));
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "Turning Bluetooth on...");
                        break;
                }
            }
        }
    };
}