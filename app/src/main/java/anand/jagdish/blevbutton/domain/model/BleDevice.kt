package anand.jagdish.blevbutton.domain.model

data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
    val connectionState: ConnectionState = ConnectionState.Disconnected
)

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Failed(val message: String) : ConnectionState()
}
