package anand.jagdish.blevbutton.data.repository

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import anand.jagdish.blevbutton.domain.model.BleDevice
import anand.jagdish.blevbutton.domain.model.ConnectionState
import anand.jagdish.blevbutton.domain.repository.BleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [BleRepository] providing BLE scanning and GATT connection management
 * specifically tailored for the V.BTTN peripheral within the GattConnect-BLE application.
 *
 * This implementation handles:
 * 1. Scanning for devices with RSSI updates.
 * 2. Automated V.BTTN verification protocol.
 * 3. Configuration of detection events (Short/Long press, Fall detection, High-G alert).
 * 4. Handling of GATT notifications and manual ACKs.
 *
 * @property context The application context.
 * @property bluetoothAdapter The system Bluetooth adapter.
 */
@Singleton
@SuppressLint("MissingPermission")
class BleRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) : BleRepository {

    companion object {
        private val VSN_SERVICE_UUID = UUID.fromString("FFFFFFF0-00F7-4000-B000-000000000000")
        private val DETECTION_CONFIG_UUID = UUID.fromString("FFFFFFF2-00F7-4000-B000-000000000000")
        private val LONG_PRESS_ACK_UUID = UUID.fromString("FFFFFFF3-00F7-4000-B000-000000000000")
        private val NOTIFICATION_UUID = UUID.fromString("FFFFFFF4-00F7-4000-B000-000000000000")
        private val VERIFICATION_UUID = UUID.fromString("FFFFFFF5-00F7-4000-B000-000000000000")
        
        /** Key required by V.BTTN for verification within 30 seconds of connection. */
        private val VERIFICATION_KEY = byteArrayOf(0x80.toByte(), 0xBE.toByte(), 0xF5.toByte(), 0xAC.toByte(), 0xFF.toByte())
        private val CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val _scannedDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState = _connectionState.asStateFlow()

    private val _receivedMessages = MutableSharedFlow<String>()
    override val receivedMessages = _receivedMessages.asSharedFlow()

    private var bluetoothGatt: BluetoothGatt? = null
    private val scanner = bluetoothAdapter?.bluetoothLeScanner
    private var currentConnectingAddress: String? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val rssi = result.rssi
            val name = device.name ?: "Unknown"
            
//            // Filter by name to ensure we only interact with relevant hardware
//            if (!name.contains("V.BTTN", ignoreCase = true) &&
//                !name.contains("V.ALert", ignoreCase = true)) return
            
            // To show only V.ALert devices, uncomment the line below:
            // if (name != "V.ALert") return
            
            _scannedDevices.update { devices ->
                val existingDevice = devices.find { it.address == device.address }
                if (existingDevice != null) {
                    devices.map {
                        if (it.address == device.address) it.copy(rssi = rssi, name = name) else it
                    }
                } else {
                    devices + BleDevice(
                        name = name, 
                        address = device.address, 
                        rssi = rssi,
                        connectionState = if (device.address == currentConnectingAddress) _connectionState.value else ConnectionState.Disconnected
                    )
                }
            }
        }
    }

    override fun startScanning() {
        _scannedDevices.value = emptyList()
        scanner?.startScan(scanCallback)

//        val filters = listOf(
//            ScanFilter.Builder()
//                .setServiceUuid(ParcelUuid(VSN_SERVICE_UUID))
//                .build()
//        )
//
//        val settings = ScanSettings.Builder()
//            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//            .build()
//
//        scanner?.startScan(filters, settings, scanCallback)
    }

    override fun stopScanning() {
        scanner?.stopScan(scanCallback)
    }

    override fun connect(address: String) {
        stopScanning()
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: return
        
        disconnect()

        currentConnectingAddress = address
        updateConnectionState(ConnectionState.Connecting)
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    override fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        val prevAddress = currentConnectingAddress
        currentConnectingAddress = null
        updateConnectionState(ConnectionState.Disconnected, prevAddress)
    }

    override fun acknowledgeLongPress() {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(VSN_SERVICE_UUID)
        val ackChar = service?.getCharacteristic(LONG_PRESS_ACK_UUID)
        if (ackChar != null) {
            writeCharacteristic(gatt, ackChar, byteArrayOf(0x01.toByte()))
            emitMessage("Manual ACK Sent (0x01)")
        }
    }

    override fun clearBlinking() {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(VSN_SERVICE_UUID)
        val ackChar = service?.getCharacteristic(LONG_PRESS_ACK_UUID)
        if (ackChar != null) {
            writeCharacteristic(gatt, ackChar, byteArrayOf(0x00.toByte()))
            emitMessage("Blinking Cleared (0x00)")
        }
    }

    private fun updateConnectionState(state: ConnectionState, address: String? = currentConnectingAddress) {
        _connectionState.value = state
        _scannedDevices.update { devices ->
            devices.map {
                if (it.address == address) it.copy(connectionState = state)
                else it.copy(connectionState = ConnectionState.Disconnected)
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val address = gatt.device.address
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                updateConnectionState(ConnectionState.Connected, address)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                updateConnectionState(ConnectionState.Disconnected, address)
                if (address == currentConnectingAddress) {
                    currentConnectingAddress = null
                }
                gatt.close()
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                updateConnectionState(ConnectionState.Failed("GATT Error: $status"), address)
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val vsnService = gatt.getService(VSN_SERVICE_UUID)
                if (vsnService != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        setupVBttn(gatt, vsnService)
                    }
                } else {
                    val characteristic = findNotifyCharacteristic(gatt)
                    if (characteristic != null) {
                        enableNotifications(gatt, characteristic)
                    } else {
                        emitMessage("No compatible characteristic found")
                    }
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                val data = characteristic.value
                if (data != null) {
                    handleData(gatt, characteristic, data)
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleData(gatt, characteristic, value)
        }
    }

    /**
     * Executes the V.BTTN specific initialization sequence.
     * 1. Verification key write.
     * 2. Configuration of detection events.
     * 3. Enabling notifications.
     */
    private suspend fun setupVBttn(gatt: BluetoothGatt, service: BluetoothGattService) {
        emitMessage("V.BTTN Detected. Starting verification...")

        // 1. Verification
        val verificationChar = service.getCharacteristic(VERIFICATION_UUID)
        if (verificationChar != null) {
            writeCharacteristic(gatt, verificationChar, VERIFICATION_KEY)
            delay(500)
        }

        // 2. Detection Config (0x0F = Short, Long, Fall, High-G)
        val configChar = service.getCharacteristic(DETECTION_CONFIG_UUID)
        if (configChar != null) {
            writeCharacteristic(gatt, configChar, byteArrayOf(0x0F.toByte()))
            delay(500)
        }

        // 3. Enable Notifications
        val notifyChar = service.getCharacteristic(NOTIFICATION_UUID)
        if (notifyChar != null) {
            enableNotifications(gatt, notifyChar)
            emitMessage("V.BTTN Configured successfully")
        }
    }

    /**
     * Parses raw BLE notifications into human-readable messages.
     * Handles V.BTTN event codes and emits results via the messages flow.
     */
    private fun handleData(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, data: ByteArray) {
        if (characteristic.uuid == NOTIFICATION_UUID) {
            val code = if (data.isNotEmpty()) data[0].toInt() else -1
            val message = when (code) {
                0x00 -> "Button Released"
                0x01 -> "Button Pressed"
                0x03 -> {
                    "Long Button Press Detected"
                }
                0x04 -> "Fall Event Detected"
                0x05 -> "High-G Event Detected"
                else -> "Unknown Event: ${data.joinToString { it.toString() }}"
            }
            emitMessage(message)
        } else {
            emitMessage(String(data))
        }
    }

    /** Sends an automatic ACK for long press events. */
    private fun sendLongPressAck(gatt: BluetoothGatt) {
        val service = gatt.getService(VSN_SERVICE_UUID)
        val ackChar = service?.getCharacteristic(LONG_PRESS_ACK_UUID)
        if (ackChar != null) {
            writeCharacteristic(gatt, ackChar, byteArrayOf(0x01.toByte()))
        }
    }

    private fun emitMessage(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _receivedMessages.emit(message)
        }
    }

    /** Helper to write data to a characteristic with API version safety. */
    private fun writeCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(characteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        } else {
            @Suppress("DEPRECATION")
            characteristic.value = value
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(characteristic)
        }
    }

    /** Generic helper to find any notifying characteristic if V.BTTN specific ones aren't found. */
    private fun findNotifyCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
        for (service in gatt.services) {
            for (characteristic in service.characteristics) {
                if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 ||
                    (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
                ) {
                    return characteristic
                }
            }
        }
        return null
    }

    /** Enables notifications for a characteristic and writes the CCCD descriptor. */
    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
        if (descriptor != null) {
            val value = if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else {
                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, value)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = value
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
        }
    }
}
