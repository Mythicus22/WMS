package com.example.myapplication.shared.features.dashboard.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.UiEffect
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.domain.model.FeaturePermission
import com.example.myapplication.shared.domain.model.User
import com.example.myapplication.shared.domain.usecase.GetCurrentUserUseCase
import com.example.myapplication.shared.domain.usecase.LogoutUseCase
import com.example.myapplication.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class DashboardUiState(
    val currentUser: User? = null,
    val availableFeatures: List<FeaturePermission> = emptyList(),
    val totalDiscoveredDevices: Long = 0,
    val onlineDiscoveredDevices: Long = 0,
    val offlineDiscoveredDevices: Long = 0,
    val activeDeviceId: String? = null
)

sealed interface DashboardUiEvent : UiEvent {
    object OnLogoutClicked : DashboardUiEvent
}

sealed interface DashboardUiEffect : UiEffect {
    object NavigateToLogin : DashboardUiEffect
}

class DashboardViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val registeredShuttleRepository: com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepository,
    private val communicationService: com.example.myapplication.shared.communication.service.CommunicationService,
    private val getSettingsUseCase: com.example.myapplication.shared.features.settings.domain.GetSettingsUseCase
) : BaseViewModel<DashboardUiState, DashboardUiEvent, DashboardUiEffect>() {

    init {
        setState(DashboardUiState())
        observeCurrentUser()
        observeDeviceCounts()
        observeActiveDevice()
        autoConnect()
    }

    private fun autoConnect() {
        viewModelScope.launch {
            try {
                // If it is already CONNECTED or CONNECTING, no need to connect
                if (communicationService.connectionState.value == com.example.myapplication.shared.communication.service.ConnectionState.DISCONNECTED) {
                    val settings = getSettingsUseCase().first()
                    communicationService.connect(settings.communication)
                }
            } catch (e: Exception) {
                io.github.aakira.napier.Napier.e("Failed to auto-connect on startup: ${e.message}")
            }
        }
    }

    private fun observeActiveDevice() {
        communicationService.activeDevice.onEach { deviceId ->
            val currentState = uiState.value ?: DashboardUiState()
            setState(currentState.copy(activeDeviceId = deviceId))
        }.launchIn(viewModelScope)
    }

    private fun observeDeviceCounts() {
        registeredShuttleRepository.getRegisteredShuttleCounts().onEach { counts ->
            val currentState = uiState.value ?: DashboardUiState()
            setState(currentState.copy(
                totalDiscoveredDevices = counts.total,
                onlineDiscoveredDevices = counts.online,
                offlineDiscoveredDevices = counts.offline
            ))
        }.launchIn(viewModelScope)
    }

    private fun observeCurrentUser() {
        getCurrentUserUseCase.currentUser.onEach { user ->
            val features = if (user != null) {
                FeaturePermission.entries.filter { user.grantedFeatures.contains(it) }
            } else {
                emptyList()
            }

            setState(DashboardUiState(currentUser = user, availableFeatures = features))
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.OnLogoutClicked -> {
                logoutUseCase()
                viewModelScope.launch {
                    emitEffect(DashboardUiEffect.NavigateToLogin)
                }
            }
        }
    }
}
