package com.example.myapplication.shared.features.maintenance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.features.maintenance.domain.GetMaintenanceTestsUseCase
import com.example.myapplication.shared.features.maintenance.model.TestDefinition
import com.example.myapplication.shared.features.shuttle.model.Shuttle
import com.example.myapplication.shared.features.shuttle.repository.ShuttleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaintenanceConsoleUiState(
    val shuttleId: String = "",
    val shuttle: Shuttle? = null,
    val tests: List<TestDefinition> = emptyList()
)

class MaintenanceConsoleViewModel(
    private val shuttleRepository: ShuttleRepository,
    private val getMaintenanceTestsUseCase: GetMaintenanceTestsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceConsoleUiState())
    val uiState: StateFlow<MaintenanceConsoleUiState> = _uiState.asStateFlow()

    fun initialize(shuttleId: String) {
        _uiState.update { curr -> curr.copy(shuttleId = shuttleId, tests = getMaintenanceTestsUseCase()) }

        viewModelScope.launch {
            val shuttleObj = shuttleRepository.getShuttleById(shuttleId)
            _uiState.update { curr -> curr.copy(shuttle = shuttleObj) }
        }
    }
}
