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
    val searchQuery: String = ""
)

class DiagnosticsDashboardViewModel(
    val shuttleId: String,
    val shuttleName: String,
    private val repository: DiagnosticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsDashboardUiState())
    val uiState: StateFlow<DiagnosticsDashboardUiState> = _uiState.asStateFlow()

    init {
        fetchDiagnostics()
    }

    private fun fetchDiagnostics() {
        viewModelScope.launch {
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
