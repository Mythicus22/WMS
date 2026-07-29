package com.example.myapplication.shared.features.maintenance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.features.maintenance.domain.GetEnabledShuttlesForMaintenanceUseCase
import com.example.myapplication.shared.features.shuttle.model.Shuttle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaintenanceHomeUiState(
    val enabledShuttles: List<Shuttle> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MaintenanceViewModel(
    private val getEnabledShuttlesForMaintenanceUseCase: GetEnabledShuttlesForMaintenanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceHomeUiState(isLoading = true))
    val uiState: StateFlow<MaintenanceHomeUiState> = _uiState.asStateFlow()

    init {
        loadShuttles()
    }

    private fun loadShuttles() {
        viewModelScope.launch {
            try {
                getEnabledShuttlesForMaintenanceUseCase().collect { shuttles ->
                    _uiState.update { curr ->
                        curr.copy(
                            enabledShuttles = shuttles,
                            isLoading = false,
                            errorMessage = if (shuttles.isEmpty()) "No enabled shuttles found in database." else null
                        )
                    }
                }
            } catch (ex: Exception) {
                _uiState.update { curr ->
                    curr.copy(
                        isLoading = false,
                        errorMessage = "Error loading shuttles: ${ex.message}"
                    )
                }
            }
        }
    }
}
