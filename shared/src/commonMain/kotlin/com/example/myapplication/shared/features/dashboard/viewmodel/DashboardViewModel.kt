package com.example.myapplication.shared.features.dashboard.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.UiEffect
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.domain.model.FeaturePermission
import com.example.myapplication.shared.domain.model.User
import com.example.myapplication.shared.domain.usecase.GetCurrentUserUseCase
import com.example.myapplication.shared.domain.usecase.LogoutUseCase
import com.example.myapplication.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class DashboardUiState(
    val currentUser: User? = null,
    val availableFeatures: List<FeaturePermission> = emptyList(),
    val totalShuttles: Long = 0,
    val onlineShuttles: Long = 0,
    val offlineShuttles: Long = 0
)

sealed interface DashboardUiEvent : UiEvent {
    object OnLogoutClicked : DashboardUiEvent
}

sealed interface DashboardUiEffect : UiEffect {
    object NavigateToLogin : DashboardUiEffect
}

class DashboardViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val shuttleRepository: com.example.myapplication.shared.features.shuttle.repository.ShuttleRepository
) : BaseViewModel<DashboardUiState, DashboardUiEvent, DashboardUiEffect>() {

    init {
        setState(DashboardUiState())
        observeCurrentUser()
        observeShuttleCounts()
    }

    private fun observeShuttleCounts() {
        shuttleRepository.getShuttleCounts().onEach { counts ->
            val currentState = uiState.value ?: DashboardUiState()
            setState(currentState.copy(
                totalShuttles = counts.total,
                onlineShuttles = counts.enabled,
                offlineShuttles = counts.disabled
            ))
        }.launchIn(viewModelScope)
    }

    private fun observeCurrentUser() {
        getCurrentUserUseCase.currentUser.onEach { user ->
            val features = if (user != null) {
                FeaturePermission.entries.filter { user.grantedFeatures.contains(it) }
            } else {
                emptyList()
            }

            setState(DashboardUiState(currentUser = user, availableFeatures = features))
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.OnLogoutClicked -> {
                logoutUseCase()
                viewModelScope.launch {
                    emitEffect(DashboardUiEffect.NavigateToLogin)
                }
            }
        }
    }
}
