package com.example.myapplication.shared.features.diagnostics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.features.diagnostics.model.DiagnosticsData
import com.example.myapplication.shared.features.diagnostics.repository.DiagnosticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiagnosticsDashboardUiState(
    val isLoading: Boolean = true,
    val data: DiagnosticsData? = null,
    val errorMessage: String? = null,
    val filterType: String = "All",
    val searchQuery: String = "",
    val shuttleId: String = "",
    val shuttleName: String = "",
    val isMockData: Boolean = false
)

class DiagnosticsDashboardViewModel(
    private val communicationService: com.example.myapplication.shared.communication.service.CommunicationService,
    private val repository: DiagnosticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsDashboardUiState())
    val uiState: StateFlow<DiagnosticsDashboardUiState> = _uiState.asStateFlow()

    private var diagJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            communicationService.activeDevice.collect { deviceId ->
                if (deviceId != null) {
                    _uiState.update { it.copy(shuttleId = deviceId, shuttleName = "Shuttle $deviceId", isMockData = false, isLoading = true) }
                    observeDevice(deviceId)
                } else {
                    _uiState.update { 
                        it.copy(
                            shuttleId = "MOCK-001", 
                            shuttleName = "Mock Shuttle", 
                            isMockData = true,
                            isLoading = false,
                            data = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsData(
                                shuttleId = "MOCK-001",
                                timestamp = kotlinx.datetime.Clock.System.now(),
                                summary = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsSummary(
                                    overallState = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsComponentState.ONLINE,
                                    activeFaults = 0, warningCount = 0, onlineComponents = 0, offlineComponents = 0,
                                    communicationState = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsComponentState.ONLINE,
                                    batteryState = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsComponentState.ONLINE,
                                    plcState = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsComponentState.ONLINE
                                ),
                                motors = emptyList(),
                                battery = com.example.myapplication.shared.features.diagnostics.model.BatteryDiagnostics(
                                    state = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsComponentState.ONLINE,
                                    voltage = 0f, current = 0f, temperature = 0f, percentage = 0f, remainingCapacityAh = 0f,
                                    estimatedRuntimeMinutes = 0, chargeCycles = 0, isCharging = false, healthPercentage = 0f
                                ),
                                plc = com.example.myapplication.shared.features.diagnostics.model.PLCDiagnostics(
                                    state = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsComponentState.ONLINE,
                                    statusText = "RUN", cpuUtilizationPercent = 0f, scanTimeMs = 0f, memoryUsagePercent = 0f,
                                    programStatus = "OK", watchdogStatus = "OK", communicationStatus = "OK", uptimeSeconds = 0L
                                ),
                                communication = com.example.myapplication.shared.features.diagnostics.model.CommunicationDiagnostics(
                                    state = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsComponentState.ONLINE,
                                    mqttStatus = "OK", canStatus = "OK", radioReceiverStatus = "OK", wifiSignalStrengthDbm = 0,
                                    framesSent = 0L, framesReceived = 0L, packetLossPercent = 0f, communicationErrors = 0,
                                    busLoadPercent = 0f, heartbeatStatus = "OK"
                                ),
                                sensors = emptyList(),
                                relays = emptyList(),
                                radio = com.example.myapplication.shared.features.diagnostics.model.RadioDiagnostics(
                                    state = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsComponentState.ONLINE,
                                    signalStrengthPercent = 0f, receiverStatus = "OK", packetCount = 0L, packetLossCount = 0L,
                                    lastPacketReceivedTime = null
                                ),
                                emergencyStop = com.example.myapplication.shared.features.diagnostics.model.EmergencyStopDiagnostics(
                                    state = com.example.myapplication.shared.features.diagnostics.model.DiagnosticsComponentState.ONLINE,
                                    isTriggered = false, lastTriggerTime = null, recoveryStatus = "READY"
                                )
                            )
                        ) 
                    }
                }
            }
        }
    }

    private fun observeDevice(shuttleId: String) {
        diagJob?.cancel()
        diagJob = viewModelScope.launch {
            repository.getDiagnostics(shuttleId).collect { data ->
                _uiState.update { it.copy(isLoading = false, data = data) }
            }
        }
    }

    fun setFilterType(type: String) {
        _uiState.update { it.copy(filterType = type) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
