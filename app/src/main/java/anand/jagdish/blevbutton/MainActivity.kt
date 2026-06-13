package anand.jagdish.blevbutton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import anand.jagdish.blevbutton.domain.model.BleDevice
import anand.jagdish.blevbutton.domain.model.ConnectionState
import anand.jagdish.blevbutton.ui.BleUiState
import anand.jagdish.blevbutton.ui.BleViewModel
import anand.jagdish.blevbutton.ui.MessageLog
import anand.jagdish.blevbutton.ui.permissions.PermissionWrapper
import anand.jagdish.blevbutton.ui.theme.BleVButtonTheme
import anand.jagdish.blevbutton.ui.theme.ErrorRed
import anand.jagdish.blevbutton.ui.theme.SuccessGreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DeviceCardPreview() {
    BleVButtonTheme {
        DeviceCard(
            device = BleDevice(
                name = "Smart Watch",
                address = "11:22:33:44:55:66",
                rssi = -68,
                connectionState = ConnectionState.Connected
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MessagePanelPreview() {
    BleVButtonTheme {
        MessagePanel(
            messages = listOf(
                MessageLog("12:30:12", "Temperature=25.4"),
                MessageLog("12:30:15", "Humidity=62%"),
                MessageLog("12:31:02", "Battery=88%")
            ),
            onClear = {}
        )
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BleVButtonTheme {
                PermissionWrapper {
                    BleScannerScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleScannerScreen(
    viewModel: BleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GattConnect-BLE", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScanButton(
                isScanning = uiState.isScanning,
                onClick = { viewModel.toggleScan() }
            )

            // Top half: Device List
            DeviceListSection(
                devices = uiState.devices,
                onDeviceClick = { viewModel.connect(it.address) },
                modifier = Modifier.weight(1f)
            )

            // Bottom half: Controls and Messages
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.connectionState is ConnectionState.Connected) {
                    ControlPanel(
                        isAckPending = uiState.isAckPending,
                        onAck = { viewModel.acknowledgeLongPress() },
                        onClearBlinking = { viewModel.clearBlinking() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                MessagePanel(
                    messages = uiState.messages,
                    onClear = { viewModel.clearMessages() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ScanButton(isScanning: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = if (isScanning) Icons.Default.Refresh else Icons.Default.RadioButtonChecked,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isScanning) "Scanning..." else "Scan Devices",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun DeviceListSection(
    devices: List<BleDevice>,
    onDeviceClick: (BleDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "FOUND DEVICES (${devices.size})",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* Filter */ }) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.Gray)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(devices) { device ->
                DeviceCard(device = device, onClick = { onDeviceClick(device) })
            }
        }
    }
}

@Composable
fun DeviceCard(device: BleDevice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator Dot
            val dotColor = when (device.connectionState) {
                is ConnectionState.Connected -> SuccessGreen
                is ConnectionState.Failed -> ErrorRed
                else -> Color.LightGray
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.name ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (device.connectionState is ConnectionState.Connected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp).padding(start = 4.dp)
                        )
                    } else if (device.connectionState is ConnectionState.Failed) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp).padding(start = 4.dp)
                        )
                    }
                }
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status Badge
                StatusBadge(device.connectionState)
            }

            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = Icons.Rounded.SignalCellularAlt,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${device.rssi} dBm",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ControlPanel(isAckPending: Boolean, onAck: () -> Unit, onClearBlinking: () -> Unit) {
    val ackButtonColor by animateColorAsState(
        targetValue = if (isAckPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
        animationSpec = if (isAckPending) {
            infiniteRepeatable(
                animation = tween(durationMillis = 500),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(durationMillis = 500)
        },
        label = "ackButtonColor"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "V.BTTN CONTROLS",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAck,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ackButtonColor)
            ) {
                Icon(
                    imageVector = if (isAckPending) Icons.Rounded.CheckCircle else Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("ACK", fontSize = 14.sp)
            }
            Button(
                onClick = onClearBlinking,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StatusBadge(state: ConnectionState) {
    val (text, color, bgColor) = when (state) {
        is ConnectionState.Connected -> Triple("CONNECTED", SuccessGreen, SuccessGreen.copy(alpha = 0.2f))
        is ConnectionState.Connecting -> Triple("CONNECTING", Color.Blue, Color.Blue.copy(alpha = 0.2f))
        is ConnectionState.Failed -> Triple("CONNECTION FAILED", ErrorRed, ErrorRed.copy(alpha = 0.2f))
        else -> Triple("NOT CONNECTED", Color.Gray, Color.LightGray.copy(alpha = 0.3f))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MessagePanel(
    messages: List<MessageLog>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE9EBEE))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Rounded.List, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RECEIVED MESSAGES",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Gray)
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        }

        if (expanded) {
            HorizontalDivider(color = Color.LightGray)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(messages) { log ->
                    Row(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = log.timestamp,
                            color = Color(0xFF3F51B5),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "  |  ",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = log.content,
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}
