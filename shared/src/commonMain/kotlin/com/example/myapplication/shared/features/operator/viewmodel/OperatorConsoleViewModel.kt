package com.example.myapplication.shared.features.operator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.operator.domain.GetShuttleFaultsUseCase
import com.example.myapplication.shared.features.operator.domain.GetShuttleLiveStatusUseCase
import com.example.myapplication.shared.features.operator.domain.SendShuttleCommandUseCase
import com.example.myapplication.shared.features.operator.model.ShuttleCommandType
import com.example.myapplication.shared.features.operator.model.ShuttleFault
import com.example.myapplication.shared.features.operator.model.ShuttleLiveStatus
import com.example.myapplication.shared.features.shuttle.model.Shuttle
import com.example.myapplication.shared.features.shuttle.repository.ShuttleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OperatorConsoleUiState(
    val shuttleId: String = "",
    val shuttle: Shuttle? = null,
    val liveStatus: ShuttleLiveStatus = ShuttleLiveStatus(shuttleId = ""),
    val activeFaults: List<ShuttleFault> = emptyList(),
    val isCommandExecuting: Boolean = false,
    val commandFeedback: String? = null
)

sealed class OperatorConsoleEvent {
    data class ExecuteCommand(val command: ShuttleCommandType) : OperatorConsoleEvent()
    data class ClearFault(val faultId: String) : OperatorConsoleEvent()
    object DismissFeedback : OperatorConsoleEvent()
}

class OperatorConsoleViewModel(
    private val shuttleRepository: ShuttleRepository,
    private val getShuttleLiveStatusUseCase: GetShuttleLiveStatusUseCase,
    private val getShuttleFaultsUseCase: GetShuttleFaultsUseCase,
    private val sendShuttleCommandUseCase: SendShuttleCommandUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorConsoleUiState())
    val uiState: StateFlow<OperatorConsoleUiState> = _uiState.asStateFlow()

    fun initialize(shuttleId: String) {
        _uiState.update { it.copy(shuttleId = shuttleId) }

        // Fetch shuttle DB info
        viewModelScope.launch {
            val shuttleObj = shuttleRepository.getShuttleById(shuttleId)
            _uiState.update { it.copy(shuttle = shuttleObj) }
        }

        // Observe Live Status telemetry
        viewModelScope.launch {
            getShuttleLiveStatusUseCase(shuttleId).collect { status ->
                _uiState.update { it.copy(liveStatus = status) }
            }
        }

        // Observe Active Faults
        viewModelScope.launch {
            getShuttleFaultsUseCase(shuttleId).collect { faults ->
                _uiState.update { it.copy(activeFaults = faults) }
            }
        }
    }

    fun onEvent(event: OperatorConsoleEvent) {
        when (event) {
            is OperatorConsoleEvent.ExecuteCommand -> executeCommand(event.command)
            is OperatorConsoleEvent.ClearFault -> {
                // Future clear fault handling
            }
            is OperatorConsoleEvent.DismissFeedback -> {
                _uiState.update { it.copy(commandFeedback = null) }
            }
        }
    }

    private fun executeCommand(command: ShuttleCommandType) {
        val currentShuttleId = _uiState.value.shuttleId
        if (currentShuttleId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommandExecuting = true, commandFeedback = null) }
            when (val result = sendShuttleCommandUseCase(currentShuttleId, command)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isCommandExecuting = false,
                            commandFeedback = "Command '${command.displayName}' dispatched successfully."
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isCommandExecuting = false,
                            commandFeedback = "Failed to dispatch command: ${result.exception.message}"
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
}
