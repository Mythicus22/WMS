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
                    _uiState.update { it.copy(shuttleId = deviceId, shuttleName = "Shuttle $deviceId", isMockData = false, isLoading = false) }
                    observeDevice(deviceId)
                } else {
                    _uiState.update { 
                        it.copy(
                            shuttleId = "", 
                            shuttleName = "No Shuttle Connected", 
                            isMockData = false,
                            isLoading = false,
                            data = null
                        )
                    }
                }
            }
        }
    }

    private fun observeDevice(shuttleId: String) {
        diagJob?.cancel()
        diagJob = viewModelScope.launch {
            try {
                kotlinx.coroutines.withTimeout(5000) {
                    repository.getDiagnostics(shuttleId).collect { data ->
                        _uiState.update { it.copy(isLoading = false, data = data) }
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                // Ignore timeout, stop loading
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refreshData() {
        if (_uiState.value.shuttleId.isBlank() || _uiState.value.isMockData) return
        _uiState.update { it.copy(isLoading = true) }
        observeDevice(_uiState.value.shuttleId)
    }

    fun setFilterType(type: String) {
        _uiState.update { it.copy(filterType = type) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
