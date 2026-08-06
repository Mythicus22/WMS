package com.example.myapplication.shared.features.maintenance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.features.maintenance.domain.GetMaintenanceTestsUseCase
import com.example.myapplication.shared.features.maintenance.model.TestDefinition
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

data class MaintenanceConsoleUiState(
    val isLoading: Boolean = true,
    val shuttle: DiscoveredDevice? = null,
    val tests: List<TestDefinition> = emptyList(),
    val errorMessage: String? = null,
    val isMockData: Boolean = false
)

class MaintenanceConsoleViewModel(
    private val communicationService: com.example.myapplication.shared.communication.service.CommunicationService,
    private val getMaintenanceTestsUseCase: GetMaintenanceTestsUseCase
) : ViewModel(), KoinComponent {

    private val _uiState = MutableStateFlow(MaintenanceConsoleUiState())
    val uiState: StateFlow<MaintenanceConsoleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(tests = getMaintenanceTestsUseCase()) }
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
                    _uiState.update { curr -> curr.copy(shuttle = fakeShuttle, isLoading = false, isMockData = false) }
                } else {
                    val fakeShuttle = DiscoveredDevice(
                        deviceId = "MOCK-001",
                        displayName = "Mock Shuttle",
                        serialNumber = "SN-MOCK",
                        protocolVersion = "1.0",
                        firmwareVersion = "1.0",
                        hardwareVersion = "mock",
                        manufacturer = "mock",
                        status = "ONLINE",
                        lastSeenAt = 0L
                    )
                    _uiState.update { curr -> curr.copy(shuttle = fakeShuttle, isLoading = false, isMockData = true) }
                }
            }
        }
    }

    fun refreshData() {
        val currentShuttle = _uiState.value.shuttle
        if (currentShuttle == null || _uiState.value.isMockData) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                kotlinx.coroutines.delay(1000) // Simulate fetch time for static tests
                _uiState.update { it.copy(tests = getMaintenanceTestsUseCase()) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
