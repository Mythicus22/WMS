package com.example.myapplication.shared.features.device.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.UiEffect
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.repository.DiscoveryRepository
import com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepository
import com.example.myapplication.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.example.myapplication.shared.communication.service.CommunicationService



data class ShuttleUiState(
    val discoveredShuttles: List<DiscoveredDevice> = emptyList(),
    val registeredShuttles: List<DiscoveredDevice> = emptyList(),
    val activeDeviceId: String? = null,
    val isLoading: Boolean = false,
    val connectionState: com.example.myapplication.shared.communication.service.ConnectionState = com.example.myapplication.shared.communication.service.ConnectionState.DISCONNECTED,
    val diagnosticsLog: List<String> = emptyList(),
    val lastConnectedTime: Long = 0L,
    val lastError: String = "",
    val reconnectCount: Int = 0,
    val incomingMessageCount: Long = 0L,
    val outgoingMessageCount: Long = 0L
)

sealed interface ShuttleUiEvent : UiEvent {
    object OnManualDiscovery : ShuttleUiEvent
    data class OnRegisterShuttle(val device: DiscoveredDevice) : ShuttleUiEvent
    data class OnUnregisterShuttle(val id: String) : ShuttleUiEvent
    data class OnConnectShuttle(val deviceId: String, val ipAddress: String? = null) : ShuttleUiEvent
}

sealed interface ShuttleUiEffect : UiEffect {
    data class ShowError(val message: String) : ShuttleUiEffect
}

class ShuttleManagementViewModel(
    private val discoveryRepository: DiscoveryRepository,
    private val registeredShuttleRepository: RegisteredShuttleRepository,
    private val communicationService: CommunicationService
) : BaseViewModel<ShuttleUiState, ShuttleUiEvent, ShuttleUiEffect>() {

    init {
        setState(ShuttleUiState())
        observeShuttles()
        onEvent(ShuttleUiEvent.OnManualDiscovery)
    }

    private fun observeShuttles() {
        discoveryRepository.getAllDevices().onEach { shuttles ->
            val currentState = uiState.value ?: ShuttleUiState()
            setState(currentState.copy(discoveredShuttles = shuttles))
        }.launchIn(viewModelScope)

        registeredShuttleRepository.getAllRegisteredShuttles().onEach { shuttles ->
            val currentState = uiState.value ?: ShuttleUiState()
            setState(currentState.copy(registeredShuttles = shuttles))
        }.launchIn(viewModelScope)

        communicationService.activeDevice.onEach { activeId ->
            val currentState = uiState.value ?: ShuttleUiState()
            setState(currentState.copy(activeDeviceId = activeId))
        }.launchIn(viewModelScope)

        communicationService.connectionState.onEach { state ->
            setState(uiState.value?.copy(connectionState = state) ?: ShuttleUiState())
        }.launchIn(viewModelScope)

        communicationService.diagnosticsLog.onEach { log ->
            setState(uiState.value?.copy(diagnosticsLog = log) ?: ShuttleUiState())
        }.launchIn(viewModelScope)

        communicationService.lastConnectedTime.onEach { time ->
            setState(uiState.value?.copy(lastConnectedTime = time) ?: ShuttleUiState())
        }.launchIn(viewModelScope)

        communicationService.lastError.onEach { error ->
            setState(uiState.value?.copy(lastError = error) ?: ShuttleUiState())
        }.launchIn(viewModelScope)

        communicationService.reconnectCount.onEach { count ->
            setState(uiState.value?.copy(reconnectCount = count) ?: ShuttleUiState())
        }.launchIn(viewModelScope)

        communicationService.incomingMessageCount.onEach { count ->
            setState(uiState.value?.copy(incomingMessageCount = count) ?: ShuttleUiState())
        }.launchIn(viewModelScope)

        communicationService.outgoingMessageCount.onEach { count ->
            setState(uiState.value?.copy(outgoingMessageCount = count) ?: ShuttleUiState())
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: ShuttleUiEvent) {
        val currentState = uiState.value ?: return
        when (event) {
            is ShuttleUiEvent.OnManualDiscovery -> {
                setState(currentState.copy(isLoading = true))
                viewModelScope.launch {
                    try {
                        discoveryRepository.requestDiscovery()
                        kotlinx.coroutines.delay(2000) // Show loader for at least 2 seconds
                    } catch (e: Exception) {
                        emitEffect(ShuttleUiEffect.ShowError(e.message ?: "Failed to request discovery"))
                    } finally {
                        setState(uiState.value?.copy(isLoading = false) ?: ShuttleUiState())
                    }
                }
            }
            is ShuttleUiEvent.OnRegisterShuttle -> {
                viewModelScope.launch {
                    try {
                        registeredShuttleRepository.registerShuttle(event.device)
                        discoveryRepository.forgetDevice(event.device.deviceId)
                    } catch (e: Exception) {
                        emitEffect(ShuttleUiEffect.ShowError(e.message ?: "Failed to register shuttle"))
                    }
                }
            }
            is ShuttleUiEvent.OnUnregisterShuttle -> {
                viewModelScope.launch {
                    try {
                        registeredShuttleRepository.unregisterShuttle(event.id)
                        if (currentState.activeDeviceId == event.id) {
                            communicationService.setActiveDevice(null)
                        }
                    } catch (e: Exception) {
                        emitEffect(ShuttleUiEffect.ShowError(e.message ?: "Failed to unregister shuttle"))
                    }
                }
            }
            is ShuttleUiEvent.OnConnectShuttle -> {
                viewModelScope.launch {
                    try {
                        communicationService.setActiveDevice(event.deviceId, event.ipAddress)
                    } catch (e: Exception) {
                        emitEffect(ShuttleUiEffect.ShowError(e.message ?: "Failed to connect to shuttle"))
                    }
                }
            }
        }
    }
}
