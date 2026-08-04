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



data class ShuttleUiState(
    val discoveredShuttles: List<DiscoveredDevice> = emptyList(),
    val registeredShuttles: List<DiscoveredDevice> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface ShuttleUiEvent : UiEvent {
    object OnManualDiscovery : ShuttleUiEvent
    data class OnRegisterShuttle(val device: DiscoveredDevice) : ShuttleUiEvent
    data class OnUnregisterShuttle(val id: String) : ShuttleUiEvent
}

sealed interface ShuttleUiEffect : UiEffect {
    data class ShowError(val message: String) : ShuttleUiEffect
}

class DeviceManagementViewModel(
    private val discoveryRepository: DiscoveryRepository,
    private val registeredShuttleRepository: RegisteredShuttleRepository
) : BaseViewModel<ShuttleUiState, ShuttleUiEvent, ShuttleUiEffect>() {

    init {
        setState(ShuttleUiState())
        observeShuttles()
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
                    } catch (e: Exception) {
                        emitEffect(ShuttleUiEffect.ShowError(e.message ?: "Failed to unregister shuttle"))
                    }
                }
            }
        }
    }
}
