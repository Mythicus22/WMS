package com.example.myapplication.shared.features.shuttle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.features.shuttle.viewmodel.AddEditShuttleUiEffect
import com.example.myapplication.shared.features.shuttle.viewmodel.AddEditShuttleUiEvent
import com.example.myapplication.shared.features.shuttle.viewmodel.AddEditShuttleUiState
import com.example.myapplication.shared.features.shuttle.viewmodel.AddEditShuttleViewModel
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.theme.AppDimensions
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditShuttleScreen(
    navigator: Navigator,
    viewModel: AddEditShuttleViewModel,
    shuttleId: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val state by viewModel.uiState.collectAsState()
    val currentState = state ?: AddEditShuttleUiState()

    LaunchedEffect(shuttleId) {
        viewModel.onEvent(AddEditShuttleUiEvent.LoadShuttle(shuttleId))
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is AddEditShuttleUiEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is AddEditShuttleUiEffect.NavigateBack -> navigator.navigate(com.example.myapplication.shared.core.navigation.Screen.Shuttle)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (currentState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AppToolbar(
                    title = if (currentState.isEditing) "Edit Shuttle" else "Add New Shuttle",
                    onNavigationClick = { navigator.goBack() }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(AppDimensions.spacing16)
                ) {

                    // ID
                    OutlinedTextField(
                        value = currentState.id,
                        onValueChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnIdChanged(it)) },
                        label = { Text("Shuttle ID") },
                        isError = currentState.errors.containsKey("id"),
                        enabled = !currentState.isEditing, // Cannot change ID once created
                        modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.spacing12),
                        supportingText = { currentState.errors["id"]?.let { Text(it) } }
                    )

                    // Name
                    OutlinedTextField(
                        value = currentState.name,
                        onValueChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnNameChanged(it)) },
                        label = { Text("Name") },
                        isError = currentState.errors.containsKey("name"),
                        modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.spacing12),
                        supportingText = { currentState.errors["name"]?.let { Text(it) } }
                    )

                    // Description
                    OutlinedTextField(
                        value = currentState.description,
                        onValueChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnDescriptionChanged(it)) },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.spacing12)
                    )

                    // IP Address
                    OutlinedTextField(
                        value = currentState.plcIpAddress,
                        onValueChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnIpAddressChanged(it)) },
                        label = { Text("PLC IP Address") },
                        isError = currentState.errors.containsKey("ip"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.spacing12),
                        supportingText = { currentState.errors["ip"]?.let { Text(it) } }
                    )

                    // MQTT Topics
                    OutlinedTextField(
                        value = currentState.mqttPublishTopic,
                        onValueChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnPublishTopicChanged(it)) },
                        label = { Text("MQTT Publish Topic") },
                        isError = currentState.errors.containsKey("publish"),
                        modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.spacing12),
                        supportingText = { currentState.errors["publish"]?.let { Text(it) } }
                    )

                    OutlinedTextField(
                        value = currentState.mqttSubscribeTopic,
                        onValueChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnSubscribeTopicChanged(it)) },
                        label = { Text("MQTT Subscribe Topic") },
                        isError = currentState.errors.containsKey("subscribe"),
                        modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.spacing12),
                        supportingText = { currentState.errors["subscribe"]?.let { Text(it) } }
                    )

                    // Timeout & Heartbeat
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.spacing12),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
                    ) {
                        OutlinedTextField(
                            value = currentState.communicationTimeout,
                            onValueChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnTimeoutChanged(it)) },
                            label = { Text("Timeout (ms)") },
                            isError = currentState.errors.containsKey("timeout"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = currentState.heartbeatInterval,
                            onValueChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnHeartbeatChanged(it)) },
                            label = { Text("Heartbeat (ms)") },
                            isError = currentState.errors.containsKey("heartbeat"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (currentState.errors.containsKey("timeout") || currentState.errors.containsKey("heartbeat")) {
                        Text(
                            text = "Invalid timeout or heartbeat", 
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = AppDimensions.spacing12)
                        )
                    }

                    // Firmware
                    OutlinedTextField(
                        value = currentState.firmwareVersion,
                        onValueChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnFirmwareChanged(it)) },
                        label = { Text("Firmware Version") },
                        isError = currentState.errors.containsKey("firmware"),
                        modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.spacing12),
                        supportingText = { currentState.errors["firmware"]?.let { Text(it) } }
                    )

                    // Enabled Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = AppDimensions.spacing8),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable Shuttle", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = currentState.isEnabled,
                            onCheckedChange = { viewModel.onEvent(AddEditShuttleUiEvent.OnEnabledChanged(it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.spacing32))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { navigator.navigate(com.example.myapplication.shared.core.navigation.Screen.Shuttle) }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(AppDimensions.spacing16))
                        Button(onClick = { viewModel.onEvent(AddEditShuttleUiEvent.OnSaveClicked) }) {
                            Text("Save Shuttle")
                        }
                    }
                }
            }
        }
    }
}
