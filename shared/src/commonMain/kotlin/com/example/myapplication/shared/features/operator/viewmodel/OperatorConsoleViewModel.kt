package com.example.myapplication.shared.features.operator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.operator.domain.GetDiscoveredDeviceFaultsUseCase
import com.example.myapplication.shared.features.operator.domain.GetDiscoveredDeviceLiveStatusUseCase
import com.example.myapplication.shared.features.operator.domain.SendDiscoveredDeviceCommandUseCase
import com.example.myapplication.shared.features.operator.model.ShuttleCommandType
import com.example.myapplication.shared.features.operator.model.ShuttleFault
import com.example.myapplication.shared.features.operator.model.ShuttleLiveStatus
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OperatorConsoleUiState(
    val shuttleId: String = "",
    val shuttle: DiscoveredDevice? = null,
    val liveStatus: ShuttleLiveStatus = ShuttleLiveStatus(shuttleId = ""),
    val isCommandExecuting: Boolean = false,
    val commandFeedback: String? = null,
    val isLoading: Boolean = false,
    val isMockData: Boolean = false,
    val activeFaults: List<ShuttleFault> = emptyList()
)

sealed class OperatorConsoleEvent {
    data class ExecuteCommand(val command: ShuttleCommandType) : OperatorConsoleEvent()
    data class ClearFault(val faultId: String) : OperatorConsoleEvent()
    object DismissFeedback : OperatorConsoleEvent()
    object RefreshData : OperatorConsoleEvent()
}

class OperatorConsoleViewModel(
    private val communicationService: com.example.myapplication.shared.communication.service.CommunicationService,
    private val getDiscoveredDeviceLiveStatusUseCase: GetDiscoveredDeviceLiveStatusUseCase,
    private val getDiscoveredDeviceFaultsUseCase: GetDiscoveredDeviceFaultsUseCase,
    private val sendDiscoveredDeviceCommandUseCase: SendDiscoveredDeviceCommandUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorConsoleUiState())
    val uiState: StateFlow<OperatorConsoleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            communicationService.activeDevice.collect { deviceId ->
                if (deviceId != null) {
                    val fakeShuttle = DiscoveredDevice(
                        deviceId = deviceId,
                        displayName = "Shuttle $deviceId",
                        serialNumber = "SN-$deviceId",
                        protocolVersion = "1.0",
                        firmwareVersion = "1.0",
                        hardwareVersion = "mock",
                        manufacturer = "mock",
                        status = "ONLINE",
                        lastSeenAt = 0L
                    )
                    _uiState.update { it.copy(shuttleId = deviceId, shuttle = fakeShuttle, isMockData = false) }
                    // Do not auto-observe, wait for explicit refresh
                } else {
                    // Fallback Mock Data
                    _uiState.update { 
                        it.copy(
                            shuttleId = "MOCK-001",
                            isMockData = true,
                            liveStatus = ShuttleLiveStatus(
                                shuttleId = "MOCK-001",
                                isOnline = true,
                                currentState = "IDLE",
                                batteryPercent = 85,
                                speed = "0.0 m/s",
                                direction = "NONE",
                                isEmergencyStopActive = false,
                                rackPosition = "Aisle 02 / Bay 14 / Tier 03",
                                liftPosition = "DOWN",
                                commStatus = "ONLINE",
                                currentMission = "NONE"
                            ),
                            activeFaults = emptyList()
                        )
                    }
                }
            }
        }
    }

    private var deviceJobs = mutableListOf<kotlinx.coroutines.Job>()

    private fun observeDevice(shuttleId: String) {
        deviceJobs.forEach { it.cancel() }
        deviceJobs.clear()

        val statusJob = viewModelScope.launch {
            getDiscoveredDeviceLiveStatusUseCase(shuttleId).collect { status ->
                _uiState.update { it.copy(liveStatus = status) }
            }
        }
        val faultsJob = viewModelScope.launch {
            getDiscoveredDeviceFaultsUseCase(shuttleId).collect { faults ->
                _uiState.update { it.copy(activeFaults = faults) }
            }
        }
        deviceJobs.add(statusJob)
        deviceJobs.add(faultsJob)
    }


    fun onEvent(event: OperatorConsoleEvent) {
        when (event) {
            is OperatorConsoleEvent.ExecuteCommand -> executeCommand(event.command)
            is OperatorConsoleEvent.ClearFault -> {
                // Future clear fault handling
            }
            is OperatorConsoleEvent.DismissFeedback -> {
                _uiState.update { it.copy(commandFeedback = null) }
            }
            is OperatorConsoleEvent.RefreshData -> refreshData()
        }
    }

    private fun refreshData() {
        val currentDiscoveredDeviceId = _uiState.value.shuttleId
        if (currentDiscoveredDeviceId.isBlank() || _uiState.value.isMockData) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                kotlinx.coroutines.withTimeout(5000) {
                    observeDevice(currentDiscoveredDeviceId)
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                // Ignore timeout, we just stop the loader
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun executeCommand(command: ShuttleCommandType) {
        val currentDiscoveredDeviceId = _uiState.value.shuttleId
        if (currentDiscoveredDeviceId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommandExecuting = true, commandFeedback = null) }
            when (val result = sendDiscoveredDeviceCommandUseCase(currentDiscoveredDeviceId, command)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isCommandExecuting = false,
                            commandFeedback = "Command '${command.displayName}' dispatched successfully."
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isCommandExecuting = false,
                            commandFeedback = "Failed to dispatch command: ${result.exception.message}"
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
}
