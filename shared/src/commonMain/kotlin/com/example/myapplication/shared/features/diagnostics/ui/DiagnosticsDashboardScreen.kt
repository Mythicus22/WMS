package com.example.myapplication.shared.features.diagnostics.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.features.diagnostics.model.*
import com.example.myapplication.shared.features.diagnostics.viewmodel.DiagnosticsDashboardViewModel
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsDashboardScreen(
    navigator: Navigator,
    viewModel: DiagnosticsDashboardViewModel,
    shuttleId: String,
    shuttleName: String
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppToolbar(
                title = "Diagnostics: $shuttleName",
                onNavigationClick = { navigator.goBack() },
                actions = {
                    IconButton(onClick = { /* Export */ }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Diagnostics")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading || uiState.data == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val data = uiState.data!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = AppDimensions.spacing16),
                    contentPadding = PaddingValues(vertical = AppDimensions.spacing16),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing16)
                ) {
                    // Filter & Search
                    item {
                        SearchAndFilterRow(
                            searchQuery = uiState.searchQuery,
                            onSearchChange = viewModel::setSearchQuery,
                            filterType = uiState.filterType,
                            onFilterChange = viewModel::setFilterType
                        )
                    }

                    // Summary KPI Cards
                    item {
                        SummaryKpiSection(data.summary)
                    }

                    // Motor Diagnostics
                    item {
                        ExpandableSectionCard(
                            title = "Motor Diagnostics",
                            icon = Icons.Default.PrecisionManufacturing,
                            status = data.motors.map { it.state }.getWorstState()
                        ) {
                            val chunkedMotors = data.motors.chunked(2)
                            chunkedMotors.forEach { rowMotors ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowMotors.forEach { motor ->
                                        MotorCard(motor, Modifier.weight(1f))
                                    }
                                    if (rowMotors.size == 1) Spacer(Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }

                    // Battery Diagnostics
                    item {
                        ExpandableSectionCard(
                            title = "Battery & Power",
                            icon = Icons.Default.BatteryFull,
                            status = data.battery.state
                        ) {
                            BatteryCard(data.battery)
                        }
                    }

                    // PLC Diagnostics
                    item {
                        ExpandableSectionCard(
                            title = "PLC Diagnostics",
                            icon = Icons.Default.Memory,
                            status = data.plc.state
                        ) {
                            PLCCard(data.plc)
                        }
                    }

                    // Communication Diagnostics
                    item {
                        ExpandableSectionCard(
                            title = "Communication & Network",
                            icon = Icons.Default.Wifi,
                            status = data.communication.state
                        ) {
                            CommunicationCard(data.communication)
                        }
                    }

                    // Sensor Diagnostics
                    item {
                        ExpandableSectionCard(
                            title = "Sensor Diagnostics",
                            icon = Icons.Default.Sensors,
                            status = data.sensors.map { it.state }.getWorstState()
                        ) {
                            val chunkedSensors = data.sensors.chunked(2)
                            chunkedSensors.forEach { rowSensors ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowSensors.forEach { sensor ->
                                        SensorCard(sensor, Modifier.weight(1f))
                                    }
                                    if (rowSensors.size == 1) Spacer(Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }

                    // Relays & Others
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ExpandableSectionCard(
                                title = "Relays",
                                icon = Icons.Default.ElectricalServices,
                                status = data.relays.map { it.state }.getWorstState(),
                                modifier = Modifier.weight(1f)
                            ) {
                                data.relays.forEach { relay ->
                                    RelayCard(relay)
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                ExpandableSectionCard(
                                    title = "Radio",
                                    icon = Icons.Default.Router,
                                    status = data.radio.state
                                ) {
                                    RadioCard(data.radio)
                                }
                                Spacer(Modifier.height(16.dp))
                                ExpandableSectionCard(
                                    title = "Emergency Stop",
                                    icon = Icons.Default.Warning,
                                    status = data.emergencyStop.state
                                ) {
                                    EStopCard(data.emergencyStop)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HELPER COMPONENTS
// ---------------------------------------------------------------------------

fun List<DiagnosticsComponentState>.getWorstState(): DiagnosticsComponentState {
    return when {
        contains(DiagnosticsComponentState.FAULT) -> DiagnosticsComponentState.FAULT
        contains(DiagnosticsComponentState.WARNING) -> DiagnosticsComponentState.WARNING
        contains(DiagnosticsComponentState.OFFLINE) -> DiagnosticsComponentState.OFFLINE
        else -> DiagnosticsComponentState.HEALTHY
    }
}

@Composable
fun StateChip(state: DiagnosticsComponentState) {
    val (color, text) = when (state) {
        DiagnosticsComponentState.HEALTHY -> AppColors.Success to "HEALTHY"
        DiagnosticsComponentState.WARNING -> AppColors.Warning to "WARNING"
        DiagnosticsComponentState.FAULT -> AppColors.Error to "FAULT"
        DiagnosticsComponentState.ONLINE -> AppColors.Success to "ONLINE"
        DiagnosticsComponentState.OFFLINE -> AppColors.Error to "OFFLINE"
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SearchAndFilterRow(
    searchQuery: String, onSearchChange: (String) -> Unit,
    filterType: String, onFilterChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search components...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filterType == "All", onClick = { onFilterChange("All") }, label = { Text("All") })
            FilterChip(selected = filterType == "Healthy", onClick = { onFilterChange("Healthy") }, label = { Text("Healthy") })
            FilterChip(selected = filterType == "Warning", onClick = { onFilterChange("Warning") }, label = { Text("Warning") })
            FilterChip(selected = filterType == "Fault", onClick = { onFilterChange("Fault") }, label = { Text("Fault") })
        }
    }
}

@Composable
fun SummaryKpiSection(summary: DiagnosticsSummary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        KpiCard("System Health", summary.overallState.name, Icons.Default.HealthAndSafety, Modifier.weight(1f))
        KpiCard("Active Faults", summary.activeFaults.toString(), Icons.Default.ErrorOutline, Modifier.weight(1f))
        KpiCard("Warnings", summary.warningCount.toString(), Icons.Default.WarningAmber, Modifier.weight(1f))
        KpiCard("Online Nodes", "${summary.onlineComponents}", Icons.Default.Dns, Modifier.weight(1f))
    }
}

@Composable
fun KpiCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExpandableSectionCard(
    title: String, icon: ImageVector, status: DiagnosticsComponentState,
    modifier: Modifier = Modifier, initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    StateChip(status)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun DataItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

// ---------------------------------------------------------------------------
// SPECIFIC COMPONENT CARDS
// ---------------------------------------------------------------------------

@Composable
fun MotorCard(motor: MotorDiagnostics, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(motor.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                StateChip(motor.state)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                DataItem("Voltage", "${motor.voltage} V", Modifier.weight(1f))
                DataItem("Current", "${motor.current} A", Modifier.weight(1f))
                DataItem("Temp", "${motor.temperature} °C", Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                DataItem("RPM", "${motor.rpm}", Modifier.weight(1f))
                DataItem("Torque", "${motor.torque} Nm", Modifier.weight(1f))
                DataItem("Dir", motor.direction, Modifier.weight(1f))
            }
            if (motor.faultCode != null) {
                Spacer(Modifier.height(6.dp))
                Text("Fault: ${motor.faultCode}", color = AppColors.Error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BatteryCard(battery: BatteryDiagnostics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                DataItem("Voltage", "${battery.voltage} V", Modifier.weight(1f))
                DataItem("Current", "${battery.current} A", Modifier.weight(1f))
                DataItem("Temp", "${battery.temperature} °C", Modifier.weight(1f))
                DataItem("Charge", "${battery.percentage} %", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                DataItem("Capacity", "${battery.remainingCapacityAh} Ah", Modifier.weight(1f))
                DataItem("Runtime", "${battery.estimatedRuntimeMinutes} min", Modifier.weight(1f))
                DataItem("Cycles", "${battery.chargeCycles}", Modifier.weight(1f))
                DataItem("Health", "${battery.healthPercentage} %", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PLCCard(plc: PLCDiagnostics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                DataItem("Status", plc.statusText, Modifier.weight(1f))
                DataItem("CPU Util", "${plc.cpuUtilizationPercent}%", Modifier.weight(1f))
                DataItem("Scan Time", "${plc.scanTimeMs} ms", Modifier.weight(1f))
                DataItem("Memory", "${plc.memoryUsagePercent}%", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                DataItem("Program", plc.programStatus, Modifier.weight(1f))
                DataItem("Watchdog", plc.watchdogStatus, Modifier.weight(1f))
                DataItem("Comms", plc.communicationStatus, Modifier.weight(1f))
                DataItem("Uptime", "${plc.uptimeSeconds} s", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CommunicationCard(comms: CommunicationDiagnostics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                DataItem("MQTT", comms.mqttStatus, Modifier.weight(1f))
                DataItem("CAN Bus", comms.canStatus, Modifier.weight(1f))
                DataItem("WiFi", "${comms.wifiSignalStrengthDbm} dBm", Modifier.weight(1f))
                DataItem("Heartbeat", comms.heartbeatStatus, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                DataItem("Tx", "${comms.framesSent}", Modifier.weight(1f))
                DataItem("Rx", "${comms.framesReceived}", Modifier.weight(1f))
                DataItem("Loss", "${comms.packetLossPercent}%", Modifier.weight(1f))
                DataItem("Errors", "${comms.communicationErrors}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SensorCard(sensor: SensorDiagnostics, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(sensor.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                StateChip(sensor.state)
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                DataItem("Value", sensor.currentValue, Modifier.weight(1f))
                DataItem("Signal", sensor.signalStatus, Modifier.weight(1f))
                DataItem("Count", "${sensor.triggerCount}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RelayCard(relay: RelayDiagnostics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(relay.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row {
                    DataItem("Coil", if(relay.coilStatus) "ENERGIZED" else "OFF", Modifier.weight(1f))
                    DataItem("Contact", if(relay.contactStatus) "CLOSED" else "OPEN", Modifier.weight(1f))
                }
            }
            StateChip(relay.state)
        }
    }
}

@Composable
fun RadioCard(radio: RadioDiagnostics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                DataItem("Signal", "${radio.signalStrengthPercent}%", Modifier.weight(1f))
                DataItem("Status", radio.receiverStatus, Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                DataItem("Packets", "${radio.packetCount}", Modifier.weight(1f))
                DataItem("Loss", "${radio.packetLossCount}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun EStopCard(estop: EmergencyStopDiagnostics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if(estop.isTriggered) AppColors.Error.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if(estop.isTriggered) "TRIGGERED" else "NORMAL", color = if(estop.isTriggered) AppColors.Error else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                StateChip(estop.state)
            }
            Spacer(Modifier.height(6.dp))
            DataItem("Recovery", estop.recoveryStatus)
        }
    }
}
