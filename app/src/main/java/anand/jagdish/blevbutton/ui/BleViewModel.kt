package anand.jagdish.blevbutton.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import anand.jagdish.blevbutton.domain.model.BleDevice
import anand.jagdish.blevbutton.domain.model.ConnectionState
import anand.jagdish.blevbutton.domain.repository.BleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Data model for messages displayed in the terminal-style UI.
 * @property timestamp Formatted time string (HH:mm:ss).
 * @property content The message or event text.
 */
data class MessageLog(
    val timestamp: String,
    val content: String
)

/**
 * State object representing the UI data for the BLE Scanner screen.
 * @property devices List of found BLE devices.
 * @property connectionState Current status of the BLE connection.
 * @property isScanning Boolean indicating if the scanner is active.
 * @property messages History of received events and status updates.
 */
data class BleUiState(
    val devices: List<BleDevice> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val isScanning: Boolean = false,
    val messages: List<MessageLog> = emptyList(),
    val isAckPending: Boolean = false
)

/**
 * ViewModel for managing the BLE scanner and communication logic for GattConnect-BLE.
 * Acts as a bridge between the [BleRepository] and the Compose UI.
 */
@HiltViewModel
class BleViewModel @Inject constructor(
    private val repository: BleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BleUiState())
    
    /**
     * UI state flow consumed by the Compose screen.
     */
    val uiState: StateFlow<BleUiState> = _uiState.asStateFlow()

    init {
        // Collect scanned devices and update UI
        repository.scannedDevices
            .onEach { devices ->
                _uiState.update { it.copy(devices = devices) }
            }
            .launchIn(viewModelScope)

        // Collect connection state changes
        repository.connectionState
            .onEach { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
            .launchIn(viewModelScope)

        // Collect incoming BLE messages and add to log
        repository.receivedMessages
            .onEach { content ->
                addMessage(content)
                if (content.contains("Long Button Press") || 
                    content.contains("Fall Event") || 
                    content.contains("High-G Event")) {
                    _uiState.update { it.copy(isAckPending = true) }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Toggles the BLE scanning process.
     */
    fun toggleScan() {
        if (_uiState.value.isScanning) {
            repository.stopScanning()
            _uiState.update { it.copy(isScanning = false) }
        } else {
            repository.startScanning()
            _uiState.update { it.copy(isScanning = true, devices = emptyList()) }
        }
    }

    /**
     * Initiates a connection to a specific device.
     * @param address MAC address of the target device.
     */
    fun connect(address: String) {
        repository.connect(address)
        _uiState.update { it.copy(isScanning = false) }
    }

    /**
     * Disconnects the current active device.
     */
    fun disconnect() {
        repository.disconnect()
    }

    /**
     * Clears the terminal message history.
     */
    fun clearMessages() {
        _uiState.update { it.copy(messages = emptyList()) }
    }

    /**
     * Manually acknowledges a V.BTTN event.
     */
    fun acknowledgeLongPress() {
        repository.acknowledgeLongPress()
        _uiState.update { it.copy(isAckPending = false) }
    }

    /**
     * Commands the V.BTTN to stop its visual alert (blinking).
     */
    fun clearBlinking() {
        repository.clearBlinking()
    }

    private fun addMessage(content: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _uiState.update { 
            it.copy(messages = listOf(MessageLog(timestamp, content)) + it.messages)
        }
    }
}
