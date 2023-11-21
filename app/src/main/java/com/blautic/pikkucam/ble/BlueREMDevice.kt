package com.blautic.pikkucam.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blautic.pikkucam.sns.SensorDevice
import com.blautic.trainingapp.sensor.mpu.Mpu
import timber.log.Timber
import java.util.Calendar
import java.util.UUID

@SuppressLint("MissingPermission")
class BlueREMDevice(n: Int) {

    var battery = 0
    var closeBGatt = false
    private var _device: BluetoothDevice? = null
    private var cfgOk = true
    private var avgBat: MutableList<Int> = ArrayList()
    private var lastAdcBat = 0
    private val lastUpdate: Calendar
    private var isBle5 = false
    private var mBluetoothGatt: BluetoothGatt? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private var number = 1
    var isConnected = false
        private set

    private var _context: Context? = null

    private val _mac: String? = null
    private var processSettingUp = false
    private val uuid = bleUUID.UUID_SERVICE
    private var writingErrors = 0

    lateinit var sensorDevice: SensorDevice

    private var longRange = false

    var firmwareVersion = 10

    private val mBluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Timber.tag("ESTATE: ").d("%s", newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true
                Timber.tag("Start services")
                    .i("n:" + number + " Attempting to start service discovery " + gatt.discoverServices())
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false
                val intent = Intent(BLE_DISC)
                intent.putExtra("device", number)
                Timber.tag("DISCONECT NUMBER").d("%s", number)
                LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                isConnected = true
                cfgMPU()
                enableNotifications()
                changePriorityAndPhy()
                Log.d("CONNECT TRUE", mBluetoothGatt!!.device.address)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                this@BlueREMDevice.characteristic = characteristic
                var conc = ""
                for (value in characteristic.value) conc =
                    conc + " " + (value.toInt() and 0xff).toString()
                Log.i("onCharacteristicRead", "n:$number $conc")
                if (characteristic.uuid.toString().contains(bleUUID.UUID_PIKKU_POW)) {
                    val adcValue =
                        characteristic.value[1].toInt() and 0xFF shl 8 or (characteristic.value[0].toInt() and 0xFF)
                    setPowerVal(adcValue)
                    val intent = Intent(BLE_READ_POW)
                    intent.putExtra("device", number)
                    intent.putExtra("data", battery)
                    LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
                } else if (characteristic.uuid.toString().contains(bleUUID.UUID_PIKKU_FW_VERS)) {
                    firmwareVersion = characteristic.value[0].toInt() and 0xFF
                    val intent = Intent(BLE_FIRMWARE_VERSION)
                    intent.putExtra("firmware", firmwareVersion)
                    intent.putExtra("ble5", isBle5)
                    LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                var conc = ""
                for (value in characteristic.value) conc =
                    conc + " " + Integer.toHexString(value.toInt() and 0xff)
                Log.i("BT Write", "n:$number $conc")
                sensorDevice.snsCfg.setOrderResult(true)
            } else {
                writingErrors++
            }
            if (processSettingUp) cfgMPU()
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            manageOperations(characteristic)
        }
    }

    init {
        number = n
        sensorDevice = SensorDevice(number)
        lastUpdate = Calendar.getInstance()
    }

    private fun changePriorityAndPhy() {
        mBluetoothGatt!!.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
        SystemClock.sleep(150)
        longRange =
            (_context!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isLeCodedPhySupported
        Log.d("BLE5", isBle5.toString())
        if (longRange) {
            val intent = Intent(BLE_PHY)
            intent.putExtra("phy", true)
            LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
        }
        if (isBle5 && longRange) {
            mBluetoothGatt!!.setPreferredPhy(
                BluetoothDevice.PHY_LE_CODED_MASK,
                BluetoothDevice.PHY_LE_CODED_MASK,
                BluetoothDevice.PHY_OPTION_S8
            )
            SystemClock.sleep(150)
        }
    }

    fun reconnected() {
        Handler().postDelayed(object : Runnable {
            override fun run() {
                if (!isConnected) {
                    connect()
                    reconnected()
                }
            }
        }, 15000)
    }

    fun initDevice(bldevice: BluetoothDevice?, isBle5: Boolean) {
        this.isBle5 = isBle5
        _device = bldevice
    }

    fun connect() {
        if (!isConnected) {
            mBluetoothGatt = _device!!.connectGatt(
                _context,
                false,
                mBluetoothGattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
            Timber.tag("CONNECT").d(mBluetoothGatt?.device?.address ?: "")
        }
    }

    fun initContextDevice(_context: Context?) {
        this._context = _context
        sensorDevice.setContext(_context)
    }

    fun enableDataFromMPU(data: Byte) {
        val order = byteArrayOf(data)
        writeChar(bleUUID.UUID_PIKKU_OPER, order)
        SystemClock.sleep(150)
    }


    fun disconnect() {
        mBluetoothGatt!!.disconnect()
    }


    private fun readFirmwareVersion() {
        mBluetoothGatt!!.readCharacteristic(
            mBluetoothGatt!!.getService(UUID.fromString(bleUUID.aux.replace("XXXX", uuid)))
                .getCharacteristic(
                    UUID.fromString(bleUUID.aux.replace("XXXX", bleUUID.UUID_PIKKU_FW_VERS))
                )
        )
    }

    private fun enableNotification(uuidNot: String?) {
        if (!isConnected) return
        try {
            val uuid1 = UUID.fromString(bleUUID.aux.replace("XXXX", uuidNot!!))
            mBluetoothGatt!!.setCharacteristicNotification(
                mBluetoothGatt!!.getService(
                    UUID.fromString(
                        bleUUID.aux.replace("XXXX", uuid)
                    )
                ).getCharacteristic(uuid1), true
            )
            for (descriptor in mBluetoothGatt!!.getService(
                UUID.fromString(
                    bleUUID.aux.replace(
                        "XXXX",
                        uuid
                    )
                )
            ).getCharacteristic(uuid1).descriptors) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            }
        } catch (e: Exception) {
            Log.e("BlueRemDevice", "n:$number Error writing value ", e)
        }
    }


    fun writeChar(uuidchar: String, value: ByteArray): Boolean {
        if (!isConnected) return false
        var uuidChar = "$uuidchar "
        for (v in value) uuidChar = uuidChar + " " + Integer.toHexString(v.toInt())
        Log.i("BT Writing", "n:$number $uuidChar")
        mBluetoothGatt!!.getService(UUID.fromString((bleUUID.aux).replace("XXXX", uuid)))
            .getCharacteristic(
                UUID.fromString(
                    (bleUUID.aux).replace(
                        "XXXX",
                        uuidchar
                    )
                )
            ).value = value
        return mBluetoothGatt!!.writeCharacteristic(
            mBluetoothGatt!!.getService(
                UUID.fromString((bleUUID.aux).replace("XXXX", uuid))
            ).getCharacteristic(UUID.fromString((bleUUID.aux).replace("XXXX", uuidchar)))
        )
    }

    fun setPowerVal(adc: Int) {
        if (adc <= 0 || adc == lastAdcBat) { return }
        var adcValue = adc
        if (isBle5) { adcValue /= 2 }

        lastAdcBat = adcValue

        val bat = when {
            adcValue >= powerAdc[0] -> 100
            adcValue <= powerAdc[NINTERVALS_ADC - 1] -> 0
            else -> powerPerc.firstOrNull { adcValue > it } ?: 0
        }

        if (avgBat.size >= NSAMPLES_AVG_BATT) { avgBat.removeAt(0) }

        avgBat.add(bat)

        val builder = avgBat.joinToString(separator = " ") { "%02d".format(it) }

        battery = (avgBat.average() / 5.0).toInt() * 5

        Timber.tag("Datos AvgBat").i(builder)
        Timber.tag("calc Battery").i("ADC:$adcValue CurrentBat:$bat Size:${avgBat.size} bateria:$battery")
    }

    private fun manageOperations(characteristic: BluetoothGattCharacteristic) {
        if (characteristic.uuid.toString().contains(bleUUID.UUID_PIKKU_BTN1)) {
            var conc = ""
            for (value in characteristic.value) {
                conc = conc + " " + Integer.toHexString(value.toInt() and 0xff)
            }
            var `val` = 0
            `val` = characteristic.value[0].toInt() and 0xFF
            val dur = characteristic.value[2].toInt() and 0xFF shl 8 or (characteristic.value[1].toInt() and 0xFF)
            val intent = Intent(BLE_READ)
            intent.putExtra("team", number)
            intent.putExtra("btn", 1)
            intent.putExtra("data", `val`)
            intent.putExtra("dur", dur)
            LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
        } else if (characteristic.uuid.toString().contains(bleUUID.UUID_PIKKU_BTN2)) {
            var conc = ""
            for (value in characteristic.value) conc =
                conc + " " + Integer.toHexString(value.toInt() and 0xff)
            val value: Int = characteristic.value[0].toInt() and 0xFF
            val dur = characteristic.value[2].toInt() and 0xFF shl 8 or (characteristic.value[1].toInt() and 0xFF)
            val intent = Intent(BLE_READ)
            intent.putExtra("team", number)
            intent.putExtra("btn", 2)
            intent.putExtra("data", value)
            intent.putExtra("dur", dur)
            LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
        } else if (characteristic.uuid.toString().contains(bleUUID.UUID_PIKKU_DATAMPU)) {
            sensorDevice.receiveData(characteristic.value)
            val mpu = Mpu(
                sensorDevice.ncurrentsample,
                sensorDevice.getValor(0),
                sensorDevice.getValor(1),
                sensorDevice.getValor(2),
                sensorDevice.getValor(3),
                sensorDevice.getValor(4),
                sensorDevice.getValor(5),
                sensorDevice.ndevice
            )
            val intent = Intent(BLE_READ_SENSOR)
            intent.putExtra("data", mpu)
            LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
        } else if (characteristic.uuid.toString().contains(bleUUID.UUID_PIKKU_OPER)) {
            val x = characteristic.value[2].toInt() and 0xFF shl 8 or (characteristic.value[1].toInt() and 0xFF)
            setPowerVal(x)
            Log.d(TAG, "n:$number Battery:$battery")
            val intent = Intent(BLE_STATUS)
            intent.putExtra("batt", battery)
            intent.putExtra("device", number)
            LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
        }
    }

    fun reconnect() {
        if (mBluetoothGatt != null && !isConnected) {
            Log.d("BT RECONNECT", "n:" + number + " " + "device: " + _device + " /UUID: " + uuid)
            mBluetoothGatt!!.connect()
        } else if (mBluetoothGatt == null && !isConnected) connect()
    }

    fun close() {
        Log.i("CLOSE", "n:$number ********* Close BlueRemDevice")
        val intent = Intent(BLE_DISC)
        intent.putExtra("device", number)
        LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
        if (mBluetoothGatt != null) {
            Log.d("DISCONNECT", mBluetoothGatt!!.device.address)
            mBluetoothGatt!!.disconnect()
            mBluetoothGatt!!.close()
            mBluetoothGatt = null
        }
    }

    private fun enableNotifications() {
        enableNotification(bleUUID.UUID_PIKKU_BTN1)
        SystemClock.sleep(150)
        enableNotification(bleUUID.UUID_PIKKU_BTN2)
        SystemClock.sleep(150)
        enableNotification(bleUUID.UUID_PIKKU_DATAMPU)
        SystemClock.sleep(150)
        enableNotification(bleUUID.UUID_PIKKU_OPER)
        SystemClock.sleep(150)
    }

    fun cfgMPU() {
        Log.d(TAG, "n:$number Configuring MPU ******** ")
        if (!processSettingUp) {
            sensorDevice.setModoCaptura()
            sensorDevice.setScale()
            processSettingUp = true
            writingErrors = 0
            cfgOk = true
        }
        if (sensorDevice.snsCfg.isAnyOrderPending) {
            if (writingErrors >= TH_ERRORS || !writeChar(
                    sensorDevice.snsCfg.nextOrder.getUuid(),
                    sensorDevice.snsCfg.nextOrder.getOrder()
                )
            ) {
                val intent = Intent(BLE_ERROR)
                intent.putExtra("device", number)
                LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
                cfgOk = false
            }
        } else {
            readFirmwareVersion()
            processSettingUp = false
            cfgOk = true
            val intent = Intent(BLE_READY)
            intent.putExtra("device", number)
            intent.putExtra("ble5", isBle5)
            LocalBroadcastManager.getInstance(_context!!).sendBroadcast(intent)
            sensorDevice.snsCfg.restoreList()
        }
    }

    companion object {
        const val TAG = "BluRem"
        const val BLE_DISC = "PIKKU_BLE_DISC"
        const val BLE_READ = "PIKKU_BLE_READ"
        const val BLE_READ_SENSOR = "PIKKU_BLE_READ_SENSOR"
        const val BLE_READ_POW = "PIKKU_BLE_READ_POW"
        const val BLE_READY = "PIKKU_BLE_READY"
        const val BLE_FIRMWARE_VERSION = "BLE_FIRMWARE_VERSION"
        const val BLE_STATUS = "PIKKU_BLE_STATUS"
        const val BLE_ERROR = "PIKKU_BLE_ERROR"
        const val BLE_PHY = "PIKKU_BLE_PHY"
        const val BLE_NOTIFY = "BTCSCORE_BLE_NOTIFY"
        private const val NSAMPLES_AVG_BATT = 3
        private const val NINTERVALS_ADC = 21
        private val powerAdc = intArrayOf(
            1417, 1407, 1396, 1386, 1375, 1365, 1354, 1344, 1334, 1323,
            1313, 1302, 1292, 1281, 1271, 1260, 1250, 1194, 1180, 1166, 1093
        )
        private val powerPerc = intArrayOf(
            100, 95, 90, 85, 80, 75, 70, 65, 60, 55,
            50, 45, 40, 35, 30, 25, 20, 15, 10, 5, 0
        )
        private const val TH_ERRORS = 5
    }
}