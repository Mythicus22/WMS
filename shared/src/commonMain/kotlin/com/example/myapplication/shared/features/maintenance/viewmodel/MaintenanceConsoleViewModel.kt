package com.example.myapplication.shared.features.maintenance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.features.maintenance.domain.GetMaintenanceTestsUseCase
import com.example.myapplication.shared.features.maintenance.model.TestDefinition
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.repository.DiscoveryRepository
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
    val errorMessage: String? = null
)

class MaintenanceConsoleViewModel(
    private val discoveryRepository: DiscoveryRepository,
    private val getMaintenanceTestsUseCase: GetMaintenanceTestsUseCase
) : ViewModel(), KoinComponent {

    private val _uiState = MutableStateFlow(MaintenanceConsoleUiState())
    val uiState: StateFlow<MaintenanceConsoleUiState> = _uiState.asStateFlow()

    fun loadShuttle(shuttleId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, tests = getMaintenanceTestsUseCase()) }
            try {
                val shuttleObj = discoveryRepository.getDeviceById(shuttleId)
                _uiState.update { curr -> curr.copy(shuttle = shuttleObj, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { curr -> curr.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }
}
