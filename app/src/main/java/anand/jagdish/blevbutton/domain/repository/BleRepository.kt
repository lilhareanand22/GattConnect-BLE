package anand.jagdish.blevbutton.domain.repository

import anand.jagdish.blevbutton.domain.model.BleDevice
import anand.jagdish.blevbutton.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the core Bluetooth Low Energy (BLE) operations for the application.
 * This repository serves as the single source of truth for BLE device discovery,
 * connection management, and data communication.
 */
interface BleRepository {
    /**
     * A [Flow] emitting the list of discovered BLE devices during a scan.
     * Includes real-time RSSI updates and connection status.
     */
    val scannedDevices: Flow<List<BleDevice>>

    /**
     * A [Flow] representing the current [ConnectionState] of the active GATT connection.
     */
    val connectionState: Flow<ConnectionState>

    /**
     * A [Flow] of incoming messages or events received from the connected BLE peripheral.
     */
    val receivedMessages: Flow<String>

    /**
     * A [Flow] representing whether the BLE scanner is currently active.
     */
    val isScanning: Flow<Boolean>

    /**
     * Starts scanning for nearby BLE devices.
     */
    fun startScanning()

    /**
     * Stops the active BLE scan to save battery and resources.
     */
    fun stopScanning()

    /**
     * Initiates a GATT connection to a device with the specified MAC address.
     * @param address The hardware MAC address of the target BLE device.
     */
    fun connect(address: String)

    /**
     * Disconnects and cleans up the current active GATT connection.
     */
    fun disconnect()

    /**
     * Manually sends an acknowledgment (0x01) to the V.BTTN peripheral.
     * This confirms receipt of a long-press or fall event.
     */
    fun acknowledgeLongPress()

    /**
     * Sends a clear signal (0x00) to the V.BTTN peripheral to stop the red-green LED flashing.
     */
    fun clearBlinking()
}
