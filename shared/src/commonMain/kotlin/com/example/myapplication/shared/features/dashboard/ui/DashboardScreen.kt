package com.example.myapplication.shared.features.dashboard.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.domain.model.FeaturePermission
import com.example.myapplication.shared.features.dashboard.viewmodel.DashboardUiEffect
import com.example.myapplication.shared.features.dashboard.viewmodel.DashboardUiEvent
import com.example.myapplication.shared.features.dashboard.viewmodel.DashboardViewModel
import com.example.myapplication.shared.presentation.components.ChipStatus
import com.example.myapplication.shared.presentation.components.StatusChip
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions

@Composable
fun DashboardScreen(
    navigator: Navigator,
    viewModel: DashboardViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val currentState = state ?: com.example.myapplication.shared.features.dashboard.viewmodel.DashboardUiState()

    val showNoShuttleDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DashboardUiEffect.NavigateToLogin -> {
                    navigator.navigateTo(Screen.Login)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = AppDimensions.spacing16, vertical = AppDimensions.spacing12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Warehouse Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                currentState.currentUser?.let { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = AppDimensions.spacing4)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AppColors.Success, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.spacing4))
                        Text(
                            text = "${user.username} (${user.role.displayName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Logout Action Button
            IconButton(
                onClick = { viewModel.onEvent(DashboardUiEvent.OnLogoutClicked) }
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = AppColors.Error
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimensions.spacing16)
        ) {
            // System Overview Metric Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
            ) {
                MetricCard(
                    title = "TOTAL SHUTTLES",
                    value = currentState.totalDiscoveredDevices.toString(),
                    icon = Icons.Default.OpenInFull,
                    iconTint = AppColors.Primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "ONLINE",
                    value = currentState.onlineDiscoveredDevices.toString(),
                    icon = Icons.Default.CheckCircle,
                    iconTint = AppColors.Success,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
            ) {
                MetricCard(
                    title = "OFFLINE",
                    value = currentState.offlineDiscoveredDevices.toString(),
                    icon = Icons.Default.MonitorHeart,
                    iconTint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "ACTIVE FAULTS",
                    value = "2",
                    icon = Icons.Default.Build,
                    iconTint = AppColors.Warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing16))

            // System Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimensions.spacing16),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SYSTEM STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.spacing4))
                        StatusChip(text = "HEALTHY", status = ChipStatus.SUCCESS)
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing24))

            Text(
                text = "AVAILABLE MODULES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            // Filtered Module Cards List
            if (currentState.availableFeatures.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimensions.spacing32),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No modules granted for your account. Please contact an Admin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                currentState.availableFeatures.chunked(2).forEach { rowFeatures ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
                    ) {
                        rowFeatures.forEach { feature ->
                            FeatureModuleCard(
                                feature = feature,
                                onClick = {
                                    when (feature) {
                                        FeaturePermission.OPERATOR -> {
                                            if (currentState.activeDeviceId != null && currentState.isConnected) navigator.navigateTo(Screen.OperatorConsole)
                                            else showNoShuttleDialog.value = true
                                        }
                                        FeaturePermission.DIAGNOSTICS -> {
                                            if (currentState.activeDeviceId != null && currentState.isConnected) navigator.navigateTo(Screen.DiagnosticsDashboard)
                                            else showNoShuttleDialog.value = true
                                        }
                                        FeaturePermission.MAINTENANCE -> {
                                            if (currentState.activeDeviceId != null && currentState.isConnected) navigator.navigateTo(Screen.MaintenanceConsole)
                                            else showNoShuttleDialog.value = true
                                        }
                                        FeaturePermission.REPORTS -> {
                                            if (currentState.activeDeviceId != null && currentState.isConnected) navigator.navigateTo(Screen.ReportsShuttleSelect)
                                            else showNoShuttleDialog.value = true
                                        }
                                        FeaturePermission.SETTINGS -> navigator.navigateTo(Screen.Settings)
                                        FeaturePermission.USER_MANAGEMENT -> navigator.navigateTo(Screen.UserManagement)
                                        FeaturePermission.SHUTTLE_MANAGEMENT -> navigator.navigateTo(Screen.Shuttle)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowFeatures.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(AppDimensions.spacing12))
                }
            }
        }
    }

    if (showNoShuttleDialog.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNoShuttleDialog.value = false },
            title = { androidx.compose.material3.Text("No Active Shuttle") },
            text = { androidx.compose.material3.Text("You are not connected to any active shuttle. Please select a shuttle in Shuttle Management first.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showNoShuttleDialog.value = false }) {
                    androidx.compose.material3.Text("OK")
                }
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.spacing16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(AppDimensions.spacing8))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeatureModuleCard(
    feature: FeaturePermission,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (feature) {
        FeaturePermission.OPERATOR -> Icons.Default.OpenInFull
        FeaturePermission.DIAGNOSTICS -> Icons.Default.MonitorHeart
        FeaturePermission.MAINTENANCE -> Icons.Default.Build
        FeaturePermission.REPORTS -> Icons.Default.Analytics
        FeaturePermission.SETTINGS -> Icons.Default.Settings
        FeaturePermission.USER_MANAGEMENT -> Icons.Default.Group
        FeaturePermission.SHUTTLE_MANAGEMENT -> Icons.Default.PrecisionManufacturing
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.spacing16),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = feature.displayName,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            Text(
                text = feature.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(AppDimensions.spacing4))
            Text(
                text = feature.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2
            )
        }
    }
}
