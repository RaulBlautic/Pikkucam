package com.blautic.pikkucam.ble;

import static android.bluetooth.BluetoothDevice.PHY_LE_2M;
import static android.bluetooth.BluetoothDevice.PHY_LE_CODED;
import static android.bluetooth.BluetoothDevice.PHY_LE_CODED_MASK;
import static android.bluetooth.BluetoothDevice.PHY_OPTION_NO_PREFERRED;
import static android.bluetooth.BluetoothDevice.PHY_OPTION_S2;
import static android.bluetooth.BluetoothDevice.PHY_OPTION_S8;
import static android.bluetooth.BluetoothDevice.TRANSPORT_AUTO;
import static android.bluetooth.BluetoothDevice.TRANSPORT_BREDR;
import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blautic.pikkucam.cfg.CfgVals;
import com.blautic.pikkucam.sns.SensorDevice;
import com.blautic.pikkucam.sns.SnsCfg;
import com.blautic.trainingapp.sensor.mpu.Mpu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class BlueREMDevice {

    public static final String TAG = "BluRem";

    public static final String BLE_DISC = "PIKKU_BLE_DISC";
    public static final String BLE_READ = "PIKKU_BLE_READ";
    public static final String BLE_READ_SENSOR = "PIKKU_BLE_READ_SENSOR";
    public static final String BLE_READ_POW = "PIKKU_BLE_READ_POW";
    public static final String BLE_READY = "PIKKU_BLE_READY";
    public static final String BLE_FIRMWARE_VERSION = "BLE_FIRMWARE_VERSION";
    public static final String BLE_STATUS = "PIKKU_BLE_STATUS";
    public static final String BLE_ERROR = "PIKKU_BLE_ERROR";
    public static final String BLE_PHY = "PIKKU_BLE_PHY";
    public static final String BLE_NOTIFY = "BTCSCORE_BLE_NOTIFY";

    private static final int NSAMPLES_AVG_BATT = 3;
    private static final int NINTERVALS_ADC = 21;
    private static final int[] powerAdc = new int[]{1417, 1407, 1396, 1386, 1375, 1365, 1354, 1344, 1334, 1323,
            1313, 1302, 1292, 1281, 1271, 1260, 1250, 1194, 1180, 1166, 1093};
    private static final int[] powerPerc = new int[]{100, 95, 90, 85, 80, 75, 70, 65, 60, 55,
            50, 45, 40, 35, 30, 25, 20, 15, 10, 5, 0};
    private static int TH_ERRORS = 5;
    public int battery;
    public boolean closeBGatt = false;
    public BluetoothDevice _device = null;
    public SnsCfg snsCfg = new SnsCfg();
    public boolean cfg_ok = true;
    List<Integer> avgBat = new ArrayList<Integer>();
    private int last_adc_bat = 0;
    private String Name;
    private Calendar last_update;
    private boolean isBle5 = false;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic Characterisc;
    private int number = 1;
    private boolean connected = false;
    private Context _context;
    private String _mac;
    private boolean process_setting_up = false;
    private String uuid = bleUUID.UUID_SERVICE;
    private int writing_errors = 0;
    private boolean postProcessValues = false;
    private SensorDevice sensorDevice;
    private boolean longRange = false;


    int firmwareVersion = 10;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d("ESTADP: ", newState + "");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connected = true;
                Log.i("Start services", "n:" + number + " Attempting to start service discovery " + gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connected = false;
                Intent intent = new Intent(BLE_DISC);
                intent.putExtra("device", number);
                Log.d("DISCONECT NUMBER", number + "");
                LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                //reconnected();
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                connected = true;
                cfgMPU();
                enableNotifications();
                //changePriorityAndPhy();

                Log.d("CONNECT TRUE", mBluetoothGatt.getDevice().getAddress());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Characterisc = characteristic;
                String conc = "";
                for (byte value : characteristic.getValue())
                    conc = conc.concat(" " + Integer.toString(value & 0xff));
                Log.i("onCharacteristicRead", "n:" + number + " " + conc);
                if (characteristic.getUuid().toString().contains(bleUUID.UUID_PIKKU_POW)) {
                    int x = (characteristic.getValue()[1] & 0xFF) << 8 | characteristic.getValue()[0] & 0xFF;
                    setPowerVal(x);
                    Intent intent = new Intent(BLE_READ_POW);
                    intent.putExtra("device", number);
                    intent.putExtra("data", battery);
                    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                } else if (characteristic.getUuid().toString().contains(bleUUID.UUID_PIKKU_FW_VERS)) {
                    firmwareVersion = characteristic.getValue()[0] & 0xFF;
                    Intent intent = new Intent(BLE_FIRMWARE_VERSION);
                    intent.putExtra("firmware",  firmwareVersion);
                    intent.putExtra("ble5", isBle5);
                    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                String conc = "";
                for (byte value : characteristic.getValue())
                    conc = conc.concat(" " + Integer.toHexString(value & 0xff));
                Log.i("BT Write", "n:" + number + " " + conc);
                sensorDevice.snsCfg.setOrderResult(true);
            } else {
                writing_errors++;
            }
            if (process_setting_up) cfgMPU();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            mngOper(characteristic, "onCharacteristicChanged");
        }
    };

    public BlueREMDevice(int n) {
        this.connected = false;
        number = n;
        sensorDevice = new SensorDevice(number);
        last_update = Calendar.getInstance();
    }

    public SensorDevice getSensorDevice() {
        return sensorDevice;
    }

    @SuppressLint("MissingPermission")
    private void changePriorityAndPhy() {
        mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        SystemClock.sleep(150);

        longRange = ((BluetoothManager) _context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isLeCodedPhySupported();

        Log.d("BLE5", String.valueOf(isBle5));


        if (longRange) {
            Intent intent = new Intent(BLE_PHY);
            intent.putExtra("phy", true);
            LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
        }

        if (isBle5 && longRange) {
            mBluetoothGatt.setPreferredPhy(PHY_LE_CODED_MASK, PHY_LE_CODED_MASK, PHY_OPTION_S8);
            SystemClock.sleep(150);
        }
    }

    public void reconnected() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!connected) {
                    connect();
                    reconnected();
                }
            }
        }, 15000);
    }

    public void initDevice(BluetoothDevice bldevice, boolean isBle5) {
        this.isBle5 = isBle5;
        this._device = bldevice;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void connect() {
        if (!connected) {
            mBluetoothGatt = _device.connectGatt(_context, false, mBluetoothGattCallback, TRANSPORT_LE);
            Log.d("CONNECT", mBluetoothGatt.getDevice().getAddress());
        }
    }

    public void initContextDevice(Context _context) {
        this._context = _context;
        sensorDevice.setContext(_context);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void enableDataFromMPU(byte data) {
        byte[] order = new byte[]{data};
        writeChar(bleUUID.UUID_PIKKU_OPER, order);
        SystemClock.sleep(150);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void disconnect() {
        mBluetoothGatt.disconnect();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean readChar(String uuidchar) {
        if (!connected) return false;
        else {
            Log.i("Servicio", new String(bleUUID.aux).replace("XXXX", uuid));
            Log.i("Characteristic", new String(bleUUID.aux).replace("XXXX", uuidchar));

            mBluetoothGatt.readCharacteristic(mBluetoothGatt.getService(UUID.
                    fromString(new String(bleUUID.aux).replace("XXXX", uuid))).getCharacteristic(UUID.
                    fromString(new String(bleUUID.aux).replace("XXXX", uuidchar))));
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public void readFirmwareVersion() {
        mBluetoothGatt.readCharacteristic(mBluetoothGatt.getService(UUID.
                fromString(bleUUID.aux.replace("XXXX", uuid))).getCharacteristic(UUID.
                fromString(bleUUID.aux.replace("XXXX", bleUUID.UUID_PIKKU_FW_VERS))));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public Boolean enableNotification(String uuidNot) {
        // check if we're connected
        if (!this.connected) return false;
        try {
            mBluetoothGatt.setCharacteristicNotification(mBluetoothGatt.getService(UUID
                    .fromString(new String(bleUUID.aux).replace("XXXX", uuid))).getCharacteristic(UUID
                    .fromString(new String(bleUUID.aux).replace("XXXX", uuidNot))), true);

            for (BluetoothGattDescriptor descriptor : mBluetoothGatt.getService(UUID.fromString(new String(bleUUID.aux)
                    .replace("XXXX", uuid))).getCharacteristic(UUID.fromString(new String(bleUUID.aux)
                    .replace("XXXX", uuidNot))).getDescriptors()) {

                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                if (mBluetoothGatt.writeDescriptor(descriptor)) {
                    //Log.d("Notifi_Enable", "OK_check: " + uuidNoti);
                }/* else {
					Log.d("NotificactionERR", "ERROR_chek");
				}*/
            }
            return true;
        } catch (Exception e) {
            Log.e("BlueRemDevice", "n:" + number + " " + "Error writing value ", e);
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public Boolean writeChar(String uuidchar, byte[] value) {
        if (!connected) return false;

        String conc = uuidchar + " ";
        for (byte v : value)
            conc = conc.concat(" " + Integer.toHexString(v));
        Log.i("BT Writing", "n:" + number + " " + conc);

        mBluetoothGatt.getService(UUID.fromString(new String(bleUUID.aux).replace("XXXX", uuid)))
                .getCharacteristic(UUID.fromString(new String(bleUUID.aux).replace("XXXX", uuidchar))).setValue(value);

        return mBluetoothGatt.writeCharacteristic(mBluetoothGatt.getService(UUID.fromString(new String(bleUUID.aux)
                .replace("XXXX", uuid))).getCharacteristic(UUID.fromString(new String(bleUUID.aux).replace("XXXX", uuidchar))));

    }

    public void setPowerVal(int adc) {

        if (adc > 0 && adc != last_adc_bat) {

            if (isBle5) adc /= 2; //parche jsf por ser distinto el adc con un bit más;
            last_adc_bat = adc;
            double ac = 0;
            int bat = 0;
            String Builder = new String();

            //Calculamos el valor de batería desde adc
            if (adc >= powerAdc[0]) bat = 100;
            else if (adc <= powerAdc[NINTERVALS_ADC - 1]) bat = 0;
            else {
                for (int i = 1; i < NINTERVALS_ADC; i++) {
                    if (adc > powerAdc[i]) {
                        bat = powerPerc[i - 1];
                        break;
                    }
                }
            }

            //Acumulamos
            if (avgBat.size() >= NSAMPLES_AVG_BATT) avgBat.remove(0);

            avgBat.add(bat);

            //Calculamos la media actual
            for (int i = 0; i < avgBat.size(); i++) {
                Builder = Builder.concat((String.format("%02d ", avgBat.get(i))));
                ac += avgBat.get(i);
            }
            this.battery = (int) (ac / avgBat.size());

            //Redondeamos para mostrar
            this.battery = (int) (Math.ceil(battery / 5d) * 5);

            Log.i("Datos AvgBat", Builder);
            Log.i("calc Battery", "ADC:" + adc + " CurrentBat:" + bat + " Size:" + avgBat.size() + " bateria:" + battery);
        }
    }

    /****************** FINAL CALLBACK ***********************************************************************/

    /**
     * Muestra y Parsea los datos recibidos y los almacena en las variables que correspondan
     */

    private void mngOper(BluetoothGattCharacteristic charac, String tag) {
        if (charac.getUuid().toString().contains(bleUUID.UUID_PIKKU_BTN1)) {
            String conc = "";

            for (byte value : charac.getValue()) {
                conc = conc.concat(" " + Integer.toHexString(value & 0xff));
            }

            int val = 0;
            val = charac.getValue()[0] & 0xFF;
            int dur = ((charac.getValue()[2] & 0xFF) << 8 | charac.getValue()[1] & 0xFF);
            Intent intent = new Intent(BLE_READ);
            intent.putExtra("team", number);
            intent.putExtra("btn", 1);
            intent.putExtra("data", val);
            intent.putExtra("dur", dur);
            LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);

        } else if (charac.getUuid().toString().contains(bleUUID.UUID_PIKKU_BTN2)) {
            String conc = "";
            for (byte value : charac.getValue())
                conc = conc.concat(" " + Integer.toHexString(value & 0xff));

            //Log.i("onCharacteristicChange", charac.getUuid().toString() + " " + conc);

            int val = 0;
            val = charac.getValue()[0] & 0xFF;
            int dur = ((charac.getValue()[2] & 0xFF) << 8 | charac.getValue()[1] & 0xFF);
            Intent intent = new Intent(BLE_READ);
            intent.putExtra("team", number);
            intent.putExtra("btn", 2);
            intent.putExtra("data", val);
            intent.putExtra("dur", dur);
            LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);

        } else if (charac.getUuid().toString().contains(bleUUID.UUID_PIKKU_DATAMPU)) {
            sensorDevice.receiveData(charac.getValue());
            //Timber.d("SAMPLE:" + sensorDevice.ncurrentsample);

            Mpu mpu = new Mpu(sensorDevice.ncurrentsample, sensorDevice.getValor(0), sensorDevice.getValor(1), sensorDevice.getValor(2),
                    sensorDevice.getValor(3), sensorDevice.getValor(4), sensorDevice.getValor(5), sensorDevice.getNdevice());
            Intent intent = new Intent(BLE_READ_SENSOR);
            intent.putExtra("data", mpu);

            LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
        } else if (charac.getUuid().toString().contains(bleUUID.UUID_PIKKU_OPER)) {
            int x = (charac.getValue()[2] & 0xFF) << 8 | charac.getValue()[1] & 0xFF;
            setPowerVal(x);
            Log.d(TAG, "n:" + number + " Battery:" + battery);

            Intent intent = new Intent(BLE_STATUS);
            intent.putExtra("batt", battery);
            intent.putExtra("device", number);
            LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
        }
    }

    public void reconnect() {
        if (mBluetoothGatt != null && !this.connected) {
            Log.d("BT RECONNECT", "n:" + number + " " + "device: " + this._device + " /UUID: " + this.uuid);
            mBluetoothGatt.connect();
        } else if (mBluetoothGatt == null && !this.connected) connect();
    }

    public void close() {
        Log.i("CLOSE", "n:" + number + " ********* Close BlueRemDevice");
        Intent intent = new Intent(BLE_DISC);
        intent.putExtra("device", number);
        LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
        if (mBluetoothGatt != null) {
            Log.d("DISCONNECT", mBluetoothGatt.getDevice().getAddress());
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    public String getMac() {
        return _device.getAddress();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void enableNotifications() {
        enableNotification(bleUUID.UUID_PIKKU_BTN1);
        SystemClock.sleep(150);

        enableNotification(bleUUID.UUID_PIKKU_BTN2);
        SystemClock.sleep(150);

        enableNotification(bleUUID.UUID_PIKKU_DATAMPU);
        SystemClock.sleep(150);

        enableNotification(bleUUID.UUID_PIKKU_OPER);
        SystemClock.sleep(150);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void cfgMPU() {
        Log.d(TAG, "n:" + number + " " + "Configuring MPU ******** ");
        if (!process_setting_up) {
            sensorDevice.setModoCaptura();
            sensorDevice.setScale();
            process_setting_up = true;
            writing_errors = 0;
            cfg_ok = true;
        }

        if (sensorDevice.snsCfg.isAnyOrderPending()) {

            if (writing_errors >= TH_ERRORS || !writeChar(sensorDevice.snsCfg.getNextOrder().getUuid(), sensorDevice.snsCfg.getNextOrder().getOrder())) {
                Intent intent = new Intent(BLE_ERROR);
                intent.putExtra("device", number);
                LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                cfg_ok = false;
            }

        } else {
            readFirmwareVersion();
            process_setting_up = false;
            cfg_ok = true;
            Intent intent = new Intent(BLE_READY);
            intent.putExtra("device", number);
            intent.putExtra("ble5", isBle5);
            LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
            sensorDevice.snsCfg.restoreList();
        }
    }

    public void setStoreInSampleList(boolean storeInSampleList) {
        //if(sensorBuffer == null && storeInSampleList) sensorBuffer = new SensorBuffer();
        sensorDevice.storeInSampleList = storeInSampleList;
        sensorDevice.last_nsample = 0;
    }

    public void configureTcargetDefault() {
        getSensorDevice().snsCfg.setMpuMode(bleUUID.OPER_ACEL_RAW);
        getSensorDevice().snsCfg.setSensorsCfg(CfgVals.BITS_ACEL, CfgVals.BITS_GYR, (byte) CfgVals.BITS_MAGNET, (byte) CfgVals.SCALE_ACC_4G,
                (byte) CfgVals.GYR_SCALE_1000, CfgVals.BASE_PERIOD, CfgVals.PACKS_PER_INTERVAL);
        getSensorDevice().snsCfg.createOrders();
    }

}
