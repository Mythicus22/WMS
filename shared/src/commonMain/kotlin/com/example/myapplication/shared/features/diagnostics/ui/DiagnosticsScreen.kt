package com.example.myapplication.shared.features.diagnostics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.repository.DiscoveryRepository
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun DiagnosticsScreen(navigator: Navigator) {
    val discoveryRepository: DiscoveryRepository = remember { getKoin().get() }
    val shuttles by discoveryRepository.getAllDevices().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppToolbar(
            title = "Diagnostics Module",
            onNavigationClick = { navigator.goBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(AppDimensions.spacing16),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
        ) {
            item {
                Text(
                    "Select Diagnostic Target",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Select a specific shuttle to view its engineering dashboard, or choose All Shuttles to view system-wide diagnostics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
            }

            // ALL SHUTTLES CARD
            item {
                AllShuttlesDiagnosticsCard(onClick = {
                    navigator.navigateTo(Screen.DiagnosticsDashboard(shuttleId = "ALL"))
                })
            }

            item {
                Text(
                    "INDIVIDUAL SHUTTLES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            val activeShuttles = shuttles.filter { it.status == "ONLINE" }
            activeShuttles.chunked(2).forEach { rowShuttles ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
                    ) {
                        rowShuttles.forEach { shuttle ->
                            DiagnosticShuttleCard(
                                shuttle = shuttle,
                                onClick = { navigator.navigateTo(Screen.DiagnosticsDashboard(shuttleId = shuttle.deviceId)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowShuttles.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (shuttles.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Loading shuttles...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllShuttlesDiagnosticsCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "All Shuttles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "System-wide health, fault aggregation, and overall diagnostics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DiagnosticShuttleCard(
    shuttle: DiscoveredDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                }
                Box(
                    modifier = Modifier.background(AppColors.Success.copy(alpha = 0.12f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Active", style = MaterialTheme.typography.labelSmall, color = AppColors.Success, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(shuttle.nameToDisplay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text("ID: ${shuttle.deviceId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
    }
}
