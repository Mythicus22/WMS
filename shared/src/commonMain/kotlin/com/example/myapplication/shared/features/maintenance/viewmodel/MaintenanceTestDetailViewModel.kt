package com.example.myapplication.shared.features.maintenance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.maintenance.domain.GetTestDetailUseCase
import com.example.myapplication.shared.features.maintenance.domain.ResetMaintenanceTestUseCase
import com.example.myapplication.shared.features.maintenance.domain.RunMaintenanceTestUseCase
import com.example.myapplication.shared.features.maintenance.model.TestDefinition
import com.example.myapplication.shared.features.maintenance.model.TestExecutionState
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.repository.DiscoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaintenanceTestDetailUiState(
    val shuttleId: String = "",
    val testId: String = "",
    val shuttle: DiscoveredDevice? = null,
    val definition: TestDefinition? = null,
    val executionState: TestExecutionState = TestExecutionState(testId = "", shuttleId = ""),
    val isRunning: Boolean = false,
    val actionMessage: String? = null
)

sealed class MaintenanceTestEvent {
    object RunTest : MaintenanceTestEvent()
    object ResetTest : MaintenanceTestEvent()
    object DismissMessage : MaintenanceTestEvent()
}

class MaintenanceTestDetailViewModel(
    private val discoveryRepository: DiscoveryRepository,
    private val getTestDetailUseCase: GetTestDetailUseCase,
    private val runMaintenanceTestUseCase: RunMaintenanceTestUseCase,
    private val resetMaintenanceTestUseCase: ResetMaintenanceTestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceTestDetailUiState())
    val uiState: StateFlow<MaintenanceTestDetailUiState> = _uiState.asStateFlow()

    fun initialize(shuttleId: String, testId: String) {
        val def = getTestDetailUseCase(testId)
        _uiState.update { curr -> curr.copy(shuttleId = shuttleId, testId = testId, definition = def) }

        viewModelScope.launch {
            val shuttleObj = discoveryRepository.getDeviceById(shuttleId)
            _uiState.update { curr -> curr.copy(shuttle = shuttleObj) }
        }

        viewModelScope.launch {
            getTestDetailUseCase.observeExecution(shuttleId, testId).collect { exec ->
                _uiState.update { curr -> curr.copy(executionState = exec) }
            }
        }
    }

    fun onEvent(event: MaintenanceTestEvent) {
        when (event) {
            MaintenanceTestEvent.RunTest -> executeRun()
            MaintenanceTestEvent.ResetTest -> executeReset()
            MaintenanceTestEvent.DismissMessage -> {
                _uiState.update { curr -> curr.copy(actionMessage = null) }
            }
        }
    }

    private fun executeRun() {
        val sId = _uiState.value.shuttleId
        val tId = _uiState.value.testId
        if (sId.isBlank() || tId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { curr -> curr.copy(isRunning = true, actionMessage = "Running test routine...") }
            when (val res = runMaintenanceTestUseCase(sId, tId)) {
                is Result.Success -> {
                    _uiState.update { curr -> curr.copy(isRunning = false, actionMessage = "Test execution completed successfully.") }
                }
                is Result.Error -> {
                    _uiState.update { curr -> curr.copy(isRunning = false, actionMessage = "Test execution failed: ${res.exception.message}") }
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun executeReset() {
        val sId = _uiState.value.shuttleId
        val tId = _uiState.value.testId
        if (sId.isBlank() || tId.isBlank()) return

        viewModelScope.launch {
            resetMaintenanceTestUseCase(sId, tId)
            _uiState.update { curr -> curr.copy(actionMessage = "Test state reset to Idle.") }
        }
    }
}
