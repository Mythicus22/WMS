package com.example.myapplication.shared.features.operator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.features.operator.domain.GetEnabledDiscoveredDevicesUseCase
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OperatorUiState(
    val enabledDiscoveredDevices: List<DiscoveredDevice> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class OperatorViewModel(
    private val getEnabledDiscoveredDevicesUseCase: GetEnabledDiscoveredDevicesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorUiState(isLoading = true))
    val uiState: StateFlow<OperatorUiState> = _uiState.asStateFlow()

    init {
        loadEnabledDiscoveredDevices()
    }

    private fun loadEnabledDiscoveredDevices() {
        viewModelScope.launch {
            try {
                getEnabledDiscoveredDevicesUseCase().collect { shuttles ->
                    _uiState.update {
                        it.copy(
                            enabledDiscoveredDevices = shuttles,
                            isLoading = false,
                            errorMessage = if (shuttles.isEmpty()) "No enabled shuttles found in database." else null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error loading shuttles: ${e.message}"
                    )
                }
            }
        }
    }
}
