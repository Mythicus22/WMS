package com.example.myapplication.shared.features.operator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.features.operator.model.FaultSeverity
import com.example.myapplication.shared.features.operator.model.ShuttleCommandType
import com.example.myapplication.shared.features.operator.model.ShuttleFault
import com.example.myapplication.shared.features.operator.model.ShuttleLiveStatus
import com.example.myapplication.shared.features.operator.viewmodel.OperatorConsoleEvent
import com.example.myapplication.shared.features.operator.viewmodel.OperatorConsoleViewModel
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.components.ChipStatus
import com.example.myapplication.shared.presentation.components.StatusChip
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions

@Composable
fun OperatorConsoleScreen(
    navigator: Navigator,
    viewModel: OperatorConsoleViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val shuttleTitle = state.shuttle?.nameToDisplay ?: "Active Shuttle"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppToolbar(
            title = "Console: $shuttleTitle",
            onNavigationClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimensions.spacing16)
        ) {
            if (state.isMockData) {
                Row(modifier = Modifier.fillMaxWidth().background(com.example.myapplication.shared.presentation.theme.AppColors.Warning.copy(alpha = 0.2f)).padding(8.dp), horizontalArrangement = Arrangement.Center) {
                    Text("No Active Shuttle - Showing Mock Data", color = com.example.myapplication.shared.presentation.theme.AppColors.Warning, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(AppDimensions.spacing12))
            }
            // Command Feedback Banner
            state.commandFeedback?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = AppDimensions.spacing12, vertical = AppDimensions.spacing8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(AppDimensions.spacing8))
                        Text(text = msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { viewModel.onEvent(OperatorConsoleEvent.DismissFeedback) }) {
                        Text("OK", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(AppDimensions.spacing12))
            }

            // Row 1: Status & Faults
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing16)) {
                LiveStatusPanel(status = state.liveStatus, modifier = Modifier.weight(1f))
                LiveFaultPanel(faults = state.activeFaults, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing16))

            // Row 2: Automatic & Manual Controls
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing16)) {
                AutomaticControlsPanel(
                    onExecute = { cmd -> viewModel.onEvent(OperatorConsoleEvent.ExecuteCommand(cmd)) },
                    isEnabled = !state.isCommandExecuting,
                    modifier = Modifier.weight(1f)
                )
                ManualControlsPanel(
                    onExecute = { cmd -> viewModel.onEvent(OperatorConsoleEvent.ExecuteCommand(cmd)) },
                    isEnabled = !state.isCommandExecuting,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LiveStatusPanel(status: ShuttleLiveStatus, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE TELEMETRY & STATUS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusChip(
                    text = if (status.isEmergencyStopActive) "E-STOP ACTIVE" else if (status.isOnline) "ONLINE" else "OFFLINE",
                    status = if (status.isEmergencyStopActive) ChipStatus.ERROR else if (status.isOnline) ChipStatus.SUCCESS else ChipStatus.INFO
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            // 2-Column Equal Grid for Telemetry (5 rows)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryItem(label = "CURRENT MISSION", value = status.currentMission, modifier = Modifier.weight(1f))
                    TelemetryItem(label = "CURRENT STATE", value = status.currentState, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryItem(label = "SPEED", value = status.speed, modifier = Modifier.weight(1f))
                    TelemetryItem(label = "DIRECTION", value = status.direction, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryItem(label = "RACK POSITION", value = status.rackPosition, modifier = Modifier.weight(1f))
                    TelemetryItem(label = "LIFT POSITION", value = status.liftPosition, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryItem(label = "BATTERY", value = "${status.batteryPercent}%", modifier = Modifier.weight(1f))
                    TelemetryItem(
                        label = "EMERGENCY STOP",
                        value = if (status.isEmergencyStopActive) "TRIGGERED" else "NORMAL",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryItem(label = "COMMUNICATION", value = status.commStatus, modifier = Modifier.weight(1f))
                    TelemetryItem(
                        label = "SYSTEM STATUS",
                        value = if (status.isOnline) "READY / ACTIVE" else "DISCONNECTED",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryItem(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AutomaticControlsPanel(onExecute: (ShuttleCommandType) -> Unit, isEnabled: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Text(
                text = "AUTOMATIC CONTROLS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AutoControlButton(
                    label = "Auto Store",
                    icon = Icons.Default.Input,
                    onClick = { onExecute(ShuttleCommandType.AUTO_STORE) },
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f)
                )
                AutoControlButton(
                    label = "Auto Retrieve",
                    icon = Icons.Default.Output,
                    onClick = { onExecute(ShuttleCommandType.AUTO_RETRIEVE) },
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AutoControlButton(
                    label = "Compact Push",
                    icon = Icons.Default.Compress,
                    onClick = { onExecute(ShuttleCommandType.COMPACT_PUSH) },
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f)
                )
                AutoControlButton(
                    label = "Compact Pull",
                    icon = Icons.Default.Expand,
                    onClick = { onExecute(ShuttleCommandType.COMPACT_PULL) },
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AutoControlButton(
                label = "Count Items (Cycle Count)",
                icon = Icons.Default.FormatListNumbered,
                onClick = { onExecute(ShuttleCommandType.COUNT_ITEMS) },
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AutoControlButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = modifier.height(48.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ManualControlsPanel(onExecute: (ShuttleCommandType) -> Unit, isEnabled: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Text(
                text = "MANUAL CONTROLS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            // Prominent STOP Button at top of manual section
            Button(
                onClick = { onExecute(ShuttleCommandType.STOP) },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "EMERGENCY / ACTION STOP", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onExecute(ShuttleCommandType.MOVE_FORWARD) },
                    enabled = isEnabled,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Move Forward", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onExecute(ShuttleCommandType.MOVE_REVERSE) },
                    enabled = isEnabled,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.FastRewind, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Move Reverse", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onExecute(ShuttleCommandType.LIFT_UP) },
                    enabled = isEnabled,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Lift Up", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onExecute(ShuttleCommandType.LIFT_DOWN) },
                    enabled = isEnabled,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Lift Down", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun LiveFaultPanel(faults: List<ShuttleFault>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE FAULTS & WARNINGS (${faults.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (faults.isEmpty()) AppColors.Success else AppColors.Warning
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            if (faults.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AppColors.Success, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(AppDimensions.spacing8))
                    Text(text = "No active faults or system alerts detected.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            } else {
                faults.forEach { fault ->
                    FaultItemCard(fault = fault)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FaultItemCard(fault: ShuttleFault) {
    val (bgColor, contentColor) = when (fault.severity) {
        FaultSeverity.FAULT -> Pair(AppColors.Error.copy(alpha = 0.1f), AppColors.Error)
        FaultSeverity.WARNING -> Pair(AppColors.Warning.copy(alpha = 0.15f), AppColors.Warning)
        FaultSeverity.INFO -> Pair(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.primary)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .padding(AppDimensions.spacing12)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = fault.faultName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
                }
                Text(text = fault.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = fault.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "SUGGESTED RECOVERY ACTION: ${fault.suggestedAction}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
