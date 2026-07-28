package com.example.myapplication.shared.features.shuttle.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.UiEffect
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.features.shuttle.model.Shuttle
import com.example.myapplication.shared.features.shuttle.repository.ShuttleRepository
import com.example.myapplication.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class ShuttleSortType { NAME, ID }

data class ShuttleUiState(
    val shuttles: List<Shuttle> = emptyList(),
    val filteredShuttles: List<Shuttle> = emptyList(),
    val searchQuery: String = "",
    val sortType: ShuttleSortType = ShuttleSortType.NAME
)

sealed interface ShuttleUiEvent : UiEvent {
    data class OnSearchQueryChanged(val query: String) : ShuttleUiEvent
    data class OnSortTypeChanged(val type: ShuttleSortType) : ShuttleUiEvent
    data class OnDeleteShuttle(val id: String) : ShuttleUiEvent
    data class OnToggleShuttleStatus(val id: String, val isEnabled: Boolean) : ShuttleUiEvent
}

sealed interface ShuttleUiEffect : UiEffect {
    data class ShowError(val message: String) : ShuttleUiEffect
}

class ShuttleViewModel(
    private val repository: ShuttleRepository
) : BaseViewModel<ShuttleUiState, ShuttleUiEvent, ShuttleUiEffect>() {

    init {
        setState(ShuttleUiState())
        observeShuttles()
    }

    private fun observeShuttles() {
        repository.getAllShuttles().onEach { shuttles ->
            val currentState = uiState.value ?: ShuttleUiState()
            setState(currentState.copy(shuttles = shuttles))
            applyFilters(currentState.searchQuery, currentState.sortType, shuttles)
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: ShuttleUiEvent) {
        val currentState = uiState.value ?: return
        when (event) {
            is ShuttleUiEvent.OnSearchQueryChanged -> {
                setState(currentState.copy(searchQuery = event.query))
                applyFilters(event.query, currentState.sortType, currentState.shuttles)
            }
            is ShuttleUiEvent.OnSortTypeChanged -> {
                setState(currentState.copy(sortType = event.type))
                applyFilters(currentState.searchQuery, event.type, currentState.shuttles)
            }
            is ShuttleUiEvent.OnDeleteShuttle -> {
                viewModelScope.launch {
                    try {
                        repository.deleteShuttle(event.id)
                    } catch (e: Exception) {
                        emitEffect(ShuttleUiEffect.ShowError(e.message ?: "Failed to delete shuttle"))
                    }
                }
            }
            is ShuttleUiEvent.OnToggleShuttleStatus -> {
                viewModelScope.launch {
                    try {
                        repository.updateShuttleStatus(event.id, event.isEnabled)
                    } catch (e: Exception) {
                        emitEffect(ShuttleUiEffect.ShowError(e.message ?: "Failed to update shuttle"))
                    }
                }
            }
        }
    }

    private fun applyFilters(query: String, sortType: ShuttleSortType, allShuttles: List<Shuttle>) {
        var result = allShuttles
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            result = result.filter { 
                it.name.lowercase().contains(lowerQuery) || it.id.lowercase().contains(lowerQuery)
            }
        }
        
        result = when (sortType) {
            ShuttleSortType.NAME -> result.sortedBy { it.name.lowercase() }
            ShuttleSortType.ID -> result.sortedBy { it.id.lowercase() }
        }
        
        setState(uiState.value?.copy(filteredShuttles = result) ?: ShuttleUiState())
    }
}
