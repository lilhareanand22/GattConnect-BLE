package anand.jagdish.blevbutton.ui.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionWrapper(
    content: @Composable () -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = BluetoothPermissionHandler.permissions
    )

    if (permissionState.allPermissionsGranted) {
        content()
    } else {
        LaunchedEffect(Unit) {
            permissionState.launchMultiplePermissionRequest()
        }
        // You could show a rationale UI here if needed
    }
}
