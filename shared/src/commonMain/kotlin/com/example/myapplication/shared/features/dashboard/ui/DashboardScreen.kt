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
import androidx.compose.material.icons.filled.Person
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
import org.jetbrains.compose.resources.painterResource
import myapplication.shared.generated.resources.Res
import myapplication.shared.generated.resources.antonomous_logo
import com.example.myapplication.shared.presentation.localization.tr

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
            androidx.compose.foundation.Image(
                painter = painterResource(Res.drawable.antonomous_logo),
                contentDescription = "Antonomous Logo",
                modifier = Modifier.height(24.dp)
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                currentState.currentUser?.let { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${user.username} (${user.role.displayName})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

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
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimensions.spacing24)
        ) {
            Text(
                text = "Warehouse Overview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(AppDimensions.spacing16))

            // System Overview Metric Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing16)
            ) {
                MetricCard(
                    title = "TOTAL SHUTTLES",
                    value = currentState.totalDiscoveredDevices.toString(),
                    icon = Icons.Default.OpenInFull,
                    iconTint = Color.White,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "ACTIVE SHUTTLE ID",
                    value = currentState.activeDeviceId ?: "None",
                    icon = Icons.Default.PrecisionManufacturing,
                    iconTint = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing32))

            Text(
                text = "Available Modules",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
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
            color = AppColors.Primary.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp)
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.tr(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.spacing16),
            verticalAlignment = Alignment.CenterVertically
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

            Spacer(modifier = Modifier.width(AppDimensions.spacing16))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.displayName.tr(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppDimensions.spacing4))
                Text(
                    text = feature.description.tr(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2
                )
            }
        }
    }
}
