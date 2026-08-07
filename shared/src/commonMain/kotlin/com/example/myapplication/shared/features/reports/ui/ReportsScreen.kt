package com.example.myapplication.shared.features.reports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepository
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions
import org.koin.mp.KoinPlatform.getKoin
import com.example.myapplication.shared.presentation.localization.tr

// ---------------------------------------------------------------------------
// SHUTTLE SELECTION SCREEN
// ---------------------------------------------------------------------------
@Composable
fun ReportsScreen(navigator: Navigator) {
    val registeredShuttleRepository: RegisteredShuttleRepository = remember { getKoin().get() }
    val shuttles by registeredShuttleRepository.getAllRegisteredShuttles().collectAsState(initial = emptyList())
    var refreshTrigger by remember { mutableStateOf(0) }

    val communicationService: com.example.myapplication.shared.communication.service.CommunicationService = remember { getKoin().get() }
    val activeDeviceId by communicationService.activeDevice.collectAsState(initial = null)
    
    val activeShuttles = remember(shuttles, activeDeviceId) {
        val list = shuttles.filter { it.status.equals("ONLINE", ignoreCase = true) }.toMutableList()
        if (activeDeviceId != null && list.none { it.deviceId == activeDeviceId }) {
            list.add(
                DiscoveredDevice(
                    deviceId = activeDeviceId!!,
                    serialNumber = "LIVE",
                    displayName = "Active Shuttle",
                    protocolVersion = "Unknown",
                    firmwareVersion = "Unknown",
                    hardwareVersion = "Unknown",
                    manufacturer = "Unknown",
                    status = "ONLINE",
                    lastSeenAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                )
            )
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppToolbar(
            title = "Reports & Analytics",
            onNavigationClick = { navigator.goBack() },
            actions = {
                IconButton(onClick = { refreshTrigger++ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(AppDimensions.spacing16),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
        ) {
            item {
                Text(
                    "Select Data Source".tr(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Choose a shuttle to view its reports, or select All Shuttles for aggregated data.".tr(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
            }

            // ALL SHUTTLES CARD (highlighted)
            item {
                AllShuttlesCard(onClick = {
                    navigator.navigateTo(Screen.Reports(shuttleId = "ALL"))
                })
            }

            item {
                Text(
                    "INDIVIDUAL SHUTTLES".tr(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            activeShuttles.chunked(2).forEach { rowShuttles ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
                    ) {
                        rowShuttles.forEach { shuttle ->
                            ShuttleSelectionCard(
                                shuttle = shuttle,
                                isActiveConnected = (shuttle.deviceId == activeDeviceId),
                                onClick = { navigator.navigateTo(Screen.Reports(shuttleId = shuttle.deviceId)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowShuttles.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (activeShuttles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No individual online shuttles active. Select 'All Shuttles' above for system reports.".tr(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllShuttlesCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "All Shuttles".tr(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Aggregated reports and analytics across all operational shuttles".tr(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatChip("Reports".tr(), Icons.Default.Description)
                    StatChip("Analytics".tr(), Icons.Default.BarChart)
                    StatChip("Export".tr(), Icons.Default.FileDownload)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ShuttleSelectionCard(
    shuttle: DiscoveredDevice,
    isActiveConnected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                if (isActiveConnected) 1.5.dp else 1.dp,
                if (isActiveConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(44.dp)
                        .background(
                            if (isActiveConnected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = if (isActiveConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (isActiveConnected) AppColors.Success.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (isActiveConnected) "CONNECTED" else "AVAILABLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActiveConnected) AppColors.Success else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(shuttle.nameToDisplay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text("ID: ${shuttle.deviceId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun StatChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}
