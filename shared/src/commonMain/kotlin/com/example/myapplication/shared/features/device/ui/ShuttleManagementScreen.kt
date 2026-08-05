package com.example.myapplication.shared.features.device.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.communication.service.ConnectionState
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.viewmodel.ShuttleUiEffect
import com.example.myapplication.shared.features.device.viewmodel.ShuttleUiEvent
import com.example.myapplication.shared.features.device.viewmodel.ShuttleUiState
import com.example.myapplication.shared.features.device.viewmodel.ShuttleManagementViewModel
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuttleManagementScreen(
    navigator: Navigator,
    viewModel: ShuttleManagementViewModel,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val state by viewModel.uiState.collectAsState()
    val currentState = state ?: ShuttleUiState()
    
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ShuttleUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppToolbar(
                title = "Shuttle Management",
                onNavigationClick = { navigator.goBack() }
            )
            
            // Refresh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimensions.spacing16, vertical = AppDimensions.spacing8),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.onEvent(ShuttleUiEvent.OnManualDiscovery) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh")
                }
            }
            
            Spacer(modifier = Modifier.height(AppDimensions.spacing16))

            if (currentState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppDimensions.spacing16),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing16)
                ) {
                    if (currentState.registeredShuttles.isNotEmpty()) {
                        item {
                            Text(
                                "Registered Shuttles",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        val rows = currentState.registeredShuttles.chunked(2)
                        items(rows) { rowShuttles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
                            ) {
                                for (shuttle in rowShuttles) {
                                    ShuttleItemCard(
                                        shuttle = shuttle,
                                        isRegistered = true,
                                        isActive = currentState.activeDeviceId == shuttle.deviceId,
                                        onAction = { viewModel.onEvent(ShuttleUiEvent.OnUnregisterShuttle(shuttle.deviceId)) },
                                        onConnect = { viewModel.onEvent(ShuttleUiEvent.OnConnectShuttle(shuttle.deviceId)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowShuttles.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (currentState.discoveredShuttles.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(AppDimensions.spacing8))
                            Text(
                                "Discovered via MQTT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        val rows = currentState.discoveredShuttles.chunked(2)
                        items(rows) { rowShuttles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
                            ) {
                                for (shuttle in rowShuttles) {
                                    ShuttleItemCard(
                                        shuttle = shuttle,
                                        isRegistered = false,
                                        isActive = currentState.activeDeviceId == shuttle.deviceId,
                                        onAction = { viewModel.onEvent(ShuttleUiEvent.OnRegisterShuttle(shuttle)) },
                                        onConnect = { viewModel.onEvent(ShuttleUiEvent.OnConnectShuttle(shuttle.deviceId, shuttle.serialNumber.removePrefix("WS-DIRECT-"))) }, // hack: serial was set to IP in DirectTransport.
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowShuttles.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (currentState.registeredShuttles.isEmpty() && currentState.discoveredShuttles.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No Shuttles Found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // Diagnostics Section
                    item {
                        Spacer(modifier = Modifier.height(AppDimensions.spacing16))
                        Text(
                            "Communication Diagnostics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Status:", style = MaterialTheme.typography.labelMedium)
                                    val statusColor = when (currentState.connectionState) {
                                        ConnectionState.CONNECTED, ConnectionState.READY -> AppColors.Success
                                        ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.error
                                        else -> AppColors.Warning
                                    }
                                    Text(
                                        text = currentState.connectionState.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Incoming Msgs:", style = MaterialTheme.typography.labelMedium)
                                    Text("${currentState.incomingMessageCount}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Outgoing Msgs:", style = MaterialTheme.typography.labelMedium)
                                    Text("${currentState.outgoingMessageCount}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Reconnects:", style = MaterialTheme.typography.labelMedium)
                                    Text("${currentState.reconnectCount}", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (currentState.lastError.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Last Error:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                                    Text(currentState.lastError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Recent Logs:", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(currentState.diagnosticsLog) { log ->
                                            Text(log, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.8f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShuttleItemCard(
    shuttle: DiscoveredDevice,
    isRegistered: Boolean,
    isActive: Boolean,
    onAction: () -> Unit,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = shuttle.nameToDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${shuttle.deviceId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isOnline = shuttle.status == "ONLINE"
                    Text(
                        text = if (isOnline) "ACTIVE" else "DISABLED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOnline) AppColors.Success else MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(
                                color = (if (isOnline) AppColors.Success else MaterialTheme.colorScheme.error).copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing8))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Serial: ${shuttle.serialNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing8))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(AppDimensions.spacing4))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Text("Active", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                } else {
                    OutlinedButton(
                        onClick = onConnect,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Connect", style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                if (isRegistered) {
                    IconButton(onClick = onAction, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Unregister Device", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                } else {
                    Button(
                        onClick = onAction,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Add Device", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
