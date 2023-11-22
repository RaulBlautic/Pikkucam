package com.blautic.pikkucam.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import timber.log.Timber

class BleAdapter {
    private val TAG = "BleAdapter"
    private var _context: Context? = null
    private var _bluetoothAdapter: BluetoothAdapter? = null

    private var macAddress: String? = null
        private set

    var isBusy = false
        private set

    private val scanHandler = Handler()
    private var mLEScanner: BluetoothLeScanner? = null
    private var mScanCallback: BLEScanCallback? = null
    private val found = false
    private var receiverRegistered = false

    fun checkBluetooth(): Boolean {
        // check if the bluetooth has Low Energy Feature
        if (!_context!!.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(_context, "DEVICE_NOT_SUPORTED", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun initialize(_context: Context) {
        this._context = _context
        _bluetoothAdapter =
            (_context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        macAddress = _bluetoothAdapter?.address
        mLEScanner = _bluetoothAdapter?.bluetoothLeScanner
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (!receiverRegistered) {
            _context.registerReceiver(mReceiver, filter)
            receiverRegistered = true
        }
        Timber.tag(TAG).d(_bluetoothAdapter?.isEnabled().toString() + "")
    }

    @Throws(Throwable::class)
    fun finalize() {
        if (receiverRegistered) {
            _context!!.unregisterReceiver(mReceiver)
            receiverRegistered = false
        }
    }

    @SuppressLint("MissingPermission")
    fun turnOffBlueAdapter() {
        _bluetoothAdapter!!.disable()
    }

    /* SCAN ********************************************************************************/
    private fun buildScanSettings(): ScanSettings {
        val builder = ScanSettings.Builder()
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        return builder.build()
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        mScanCallback = BLEScanCallback()
        mLEScanner!!.startScan(null, buildScanSettings(), mScanCallback)
    }

    fun scanAllDevices(): Boolean {
        Log.d("BleAdapter", "Scanning BLE ...")
        scanHandler.removeCallbacks(stopScan)
        scanHandler.postDelayed(stopScan, 5000)
        isBusy = true
        startScanning()
        return isBusy
    }

    @SuppressLint("MissingPermission")
    fun cancelScan() {
        if (mLEScanner != null && _bluetoothAdapter!!.isEnabled) {
            mLEScanner!!.stopScan(mScanCallback)
        }
        isBusy = false
    }

    fun stopReceiver() {
        _context!!.unregisterReceiver(mReceiver)
    }

    val isEnabled: Boolean
        get() = _bluetoothAdapter!!.isEnabled

    fun bluetoothDeviceFactory(mac: String?): BluetoothDevice {
        return _bluetoothAdapter!!.getRemoteDevice(mac)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun checkIfTarget(mac: String, scan: ByteArray) {
        if ((scan[bleUUID.POS_ID_DEVICE] == bleUUID.TAG_ID_DEVICE || scan[bleUUID.POS_ID_DEVICE] == bleUUID.TAG_ID_BL5) && (scan[bleUUID.POS_ID_DEVICE + 1] == bleUUID.INT_FRAME_ID_SCORE || scan[bleUUID.POS_ID_DEVICE + 1] == bleUUID.INT_FRAME_ID_SCORE_BL5 || scan[bleUUID.POS_ID_DEVICE + 1] == bleUUID.INT_FRAME_ID_SCORE_PIKKU_ACADEMY)) {
            var conc = ""
            for (value in scan) conc = conc + " " + (value.toInt() and 0xff)
            Log.d("CONC", conc)
            if (scan[bleUUID.POS_TAG_BTN] == bleUUID.INT_DEVICE_BTN) {
                //BTN1
                val btn1 = scan[bleUUID.POS_TAG_BTN + 2].toInt() and 0xFF
                val btn2 = 0
                if (btn1 == 1) {
                    val isBle5 = scan[bleUUID.POS_ID_DEVICE] == bleUUID.TAG_ID_BL5
                    val intent = Intent(BLE_ADAPTER_DEVICE_CLICK)
                    intent.putExtra("mac", mac)
                    intent.putExtra("ble5", isBle5)
                    intent.putExtra("btn1", btn1)
                    intent.putExtra("btn2", btn2)
                    //intent.putExtra("version",version);
                    LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
                }
            }
        }
    }

    /* UTILS ******************************************************************************/
    private fun keepScanning() {
        if (!found) {
            val scan = true
            if (scan) {
                try {
                    scanAllDevices()
                } catch (e: Exception) {
                }
            }
        }
    }

    private inner class BLEScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            checkIfTarget(result.device.address, result.scanRecord!!.bytes)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (result in results) {
                checkIfTarget(result.device.address, result.scanRecord!!.bytes)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "Scan FAILED ")
        }
    }

    private val stopScan: Runnable = object : Runnable {
        override fun run() {
            Log.d("BleAdapter", "STOP...")
            if (isBusy) {
                cancelScan()
                LocalBroadcastManager.getInstance(_context!!).sendBroadcast(
                    Intent(
                        BLE_ADAPTER_SCAN_FINISHED
                    )
                )
                keepScanning()
            }
        }
    }

    /* BLUETOOTH BROADCAST RECEIVER **************************************************************************/
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> Log.d(TAG, "Bluetooth off")
                    BluetoothAdapter.STATE_TURNING_OFF -> Log.d(TAG, "Turning Bluetooth off...")
                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "Bluetooth on")
                        keepScanning()
                        LocalBroadcastManager.getInstance(_context!!)
                            .sendBroadcast(Intent(BlueREMDevice.BLE_DISC))
                    }

                    BluetoothAdapter.STATE_TURNING_ON -> Log.d(TAG, "Turning Bluetooth on...")
                }
            }
        }
    }

    /* Initialize adapter ******************************************************************/
    init {
        if (BluetoothAdapter.getDefaultAdapter() == null) throw Exception("Device does not support bluetooth")
    }

    companion object {
        const val BLE_ADAPTER_SCAN_FINISHED = "SCAN_FINISHED"
        const val BLE_ADAPTER_SCAN_STARTED = "SCAN_STARTED"
        const val BLE_ADAPTER_DEVICE_CLICK = "DEVICE_CLICK"
    }
}