package com.example.myapplication.shared.features.shuttle.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.UiEffect
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.features.shuttle.model.Shuttle
import com.example.myapplication.shared.features.shuttle.repository.ShuttleRepository
import com.example.myapplication.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import com.example.myapplication.shared.core.utils.getCurrentTimeMillis

data class AddEditShuttleUiState(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val plcIpAddress: String = "",
    val mqttPublishTopic: String = "",
    val mqttSubscribeTopic: String = "",
    val communicationTimeout: String = "5000",
    val heartbeatInterval: String = "1000",
    val firmwareVersion: String = "",
    val isEnabled: Boolean = true,
    
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

sealed interface AddEditShuttleUiEvent : UiEvent {
    data class LoadShuttle(val id: String?) : AddEditShuttleUiEvent
    data class OnIdChanged(val value: String) : AddEditShuttleUiEvent
    data class OnNameChanged(val value: String) : AddEditShuttleUiEvent
    data class OnDescriptionChanged(val value: String) : AddEditShuttleUiEvent
    data class OnIpAddressChanged(val value: String) : AddEditShuttleUiEvent
    data class OnPublishTopicChanged(val value: String) : AddEditShuttleUiEvent
    data class OnSubscribeTopicChanged(val value: String) : AddEditShuttleUiEvent
    data class OnTimeoutChanged(val value: String) : AddEditShuttleUiEvent
    data class OnHeartbeatChanged(val value: String) : AddEditShuttleUiEvent
    data class OnFirmwareChanged(val value: String) : AddEditShuttleUiEvent
    data class OnEnabledChanged(val value: Boolean) : AddEditShuttleUiEvent
    object OnSaveClicked : AddEditShuttleUiEvent
}

sealed interface AddEditShuttleUiEffect : UiEffect {
    object NavigateBack : AddEditShuttleUiEffect
    data class ShowError(val message: String) : AddEditShuttleUiEffect
}

class AddEditShuttleViewModel(
    private val repository: ShuttleRepository
) : BaseViewModel<AddEditShuttleUiState, AddEditShuttleUiEvent, AddEditShuttleUiEffect>() {

    init {
        setState(AddEditShuttleUiState())
    }

    override fun onEvent(event: AddEditShuttleUiEvent) {
        val currentState = uiState.value ?: AddEditShuttleUiState()
        when (event) {
            is AddEditShuttleUiEvent.LoadShuttle -> {
                if (event.id != null) {
                    setState(currentState.copy(isLoading = true, isEditing = true))
                    viewModelScope.launch {
                        val shuttle = repository.getShuttleById(event.id)
                        if (shuttle != null) {
                            setState(
                                currentState.copy(
                                    id = shuttle.id,
                                    name = shuttle.name,
                                    description = shuttle.description ?: "",
                                    plcIpAddress = shuttle.plcIpAddress,
                                    mqttPublishTopic = shuttle.mqttPublishTopic,
                                    mqttSubscribeTopic = shuttle.mqttSubscribeTopic,
                                    communicationTimeout = shuttle.communicationTimeout.toString(),
                                    heartbeatInterval = shuttle.heartbeatInterval.toString(),
                                    firmwareVersion = shuttle.firmwareVersion,
                                    isEnabled = shuttle.isEnabled,
                                    isLoading = false,
                                    isEditing = true
                                )
                            )
                        } else {
                            setState(currentState.copy(isLoading = false))
                            emitEffect(AddEditShuttleUiEffect.ShowError("Shuttle not found"))
                            emitEffect(AddEditShuttleUiEffect.NavigateBack)
                        }
                    }
                }
            }
            is AddEditShuttleUiEvent.OnIdChanged -> setState(currentState.copy(id = event.value))
            is AddEditShuttleUiEvent.OnNameChanged -> setState(currentState.copy(name = event.value))
            is AddEditShuttleUiEvent.OnDescriptionChanged -> setState(currentState.copy(description = event.value))
            is AddEditShuttleUiEvent.OnIpAddressChanged -> setState(currentState.copy(plcIpAddress = event.value))
            is AddEditShuttleUiEvent.OnPublishTopicChanged -> setState(currentState.copy(mqttPublishTopic = event.value))
            is AddEditShuttleUiEvent.OnSubscribeTopicChanged -> setState(currentState.copy(mqttSubscribeTopic = event.value))
            is AddEditShuttleUiEvent.OnTimeoutChanged -> setState(currentState.copy(communicationTimeout = event.value))
            is AddEditShuttleUiEvent.OnHeartbeatChanged -> setState(currentState.copy(heartbeatInterval = event.value))
            is AddEditShuttleUiEvent.OnFirmwareChanged -> setState(currentState.copy(firmwareVersion = event.value))
            is AddEditShuttleUiEvent.OnEnabledChanged -> setState(currentState.copy(isEnabled = event.value))
            is AddEditShuttleUiEvent.OnSaveClicked -> validateAndSave(currentState)
        }
    }

    private fun validateAndSave(state: AddEditShuttleUiState) {
        val errors = mutableMapOf<String, String>()
        
        if (state.id.isBlank()) errors["id"] = "ID is required"
        if (state.name.isBlank()) errors["name"] = "Name is required"
        
        val ipRegex = Regex("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}\$")
        if (state.plcIpAddress.isBlank()) {
            errors["ip"] = "IP Address is required"
        } else if (!ipRegex.matches(state.plcIpAddress)) {
            errors["ip"] = "Invalid IP Address format"
        }

        if (state.mqttPublishTopic.isBlank()) errors["publish"] = "Publish topic is required"
        if (state.mqttSubscribeTopic.isBlank()) errors["subscribe"] = "Subscribe topic is required"
        
        val timeout = state.communicationTimeout.toIntOrNull()
        if (timeout == null || timeout <= 0) errors["timeout"] = "Invalid timeout"
        
        val heartbeat = state.heartbeatInterval.toIntOrNull()
        if (heartbeat == null || heartbeat <= 0) errors["heartbeat"] = "Invalid heartbeat"
        
        if (state.firmwareVersion.isBlank()) errors["firmware"] = "Firmware is required"

        if (errors.isNotEmpty()) {
            setState(state.copy(errors = errors))
            return
        }

        viewModelScope.launch {
            try {
                if (!state.isEditing) {
                    val existing = repository.getShuttleById(state.id)
                    if (existing != null) {
                        setState(state.copy(errors = mapOf("id" to "Shuttle ID already exists")))
                        return@launch
                    }
                }

                val now = getCurrentTimeMillis()
                val existingShuttle = if (state.isEditing) repository.getShuttleById(state.id) else null

                val shuttle = Shuttle(
                    id = state.id,
                    name = state.name,
                    description = state.description.takeIf { it.isNotBlank() },
                    plcIpAddress = state.plcIpAddress,
                    mqttPublishTopic = state.mqttPublishTopic,
                    mqttSubscribeTopic = state.mqttSubscribeTopic,
                    communicationTimeout = timeout ?: 5000,
                    heartbeatInterval = heartbeat ?: 1000,
                    firmwareVersion = state.firmwareVersion,
                    isEnabled = state.isEnabled,
                    createdAt = existingShuttle?.createdAt ?: now,
                    updatedAt = now
                )

                if (state.isEditing) {
                    repository.updateShuttle(shuttle)
                } else {
                    repository.addShuttle(shuttle)
                }
                
                emitEffect(AddEditShuttleUiEffect.NavigateBack)
            } catch (e: Exception) {
                emitEffect(AddEditShuttleUiEffect.ShowError(e.message ?: "Failed to save shuttle"))
            }
        }
    }
}
