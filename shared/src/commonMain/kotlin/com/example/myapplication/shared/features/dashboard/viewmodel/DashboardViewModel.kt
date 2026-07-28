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
    val availableFeatures: List<FeaturePermission> = emptyList()
)

sealed interface DashboardUiEvent : UiEvent {
    object OnLogoutClicked : DashboardUiEvent
}

sealed interface DashboardUiEffect : UiEffect {
    object NavigateToLogin : DashboardUiEffect
}

class DashboardViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel<DashboardUiState, DashboardUiEvent, DashboardUiEffect>() {

    init {
        setState(DashboardUiState())
        observeCurrentUser()
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
