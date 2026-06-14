package anand.jagdish.blevbutton.ui.permissions

import android.Manifest
import android.os.Build

object BluetoothPermissionHandler {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val list = mutableListOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }
}
