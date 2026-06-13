package anand.jagdish.blevbutton.ui.permissions

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionWrapper(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter

    val permissionState = rememberMultiplePermissionsState(
        permissions = BluetoothPermissionHandler.permissions
    )

    var isBluetoothEnabled by remember {
        mutableStateOf(bluetoothAdapter?.isEnabled ?: false)
    }

    // Update state when activity result comes back
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
    }

    // Re-check Bluetooth status whenever permissions are granted
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
        }
    }

    if (permissionState.allPermissionsGranted) {
        if (isBluetoothEnabled) {
            content()
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Bluetooth is disabled")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(intent)
                }) {
                    Text("Enable Bluetooth")
                }
            }
        }
    } else {
        LaunchedEffect(Unit) {
            permissionState.launchMultiplePermissionRequest()
        }
        
        // Optional: Show rationale if permissions are denied
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permissions required for BLE scanning")
        }
    }
}
