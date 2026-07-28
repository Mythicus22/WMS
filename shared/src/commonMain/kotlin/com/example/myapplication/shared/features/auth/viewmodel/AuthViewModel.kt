package com.example.myapplication.shared.features.auth.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.UiEffect
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.domain.usecase.LoginUseCase
import com.example.myapplication.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginUiEvent : UiEvent {
    data class OnUsernameChanged(val username: String) : LoginUiEvent
    data class OnPasswordChanged(val password: String) : LoginUiEvent
    object OnLoginClicked : LoginUiEvent
    object OnDismissError : LoginUiEvent
}

sealed interface LoginUiEffect : UiEffect {
    object NavigateToDashboard : LoginUiEffect
}

class AuthViewModel(
    private val loginUseCase: LoginUseCase
) : BaseViewModel<LoginUiState, LoginUiEvent, LoginUiEffect>() {

    init {
        setState(LoginUiState())
    }

    override fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnUsernameChanged -> {
                val current = _uiState.value ?: LoginUiState()
                setState(current.copy(username = event.username, errorMessage = null))
            }
            is LoginUiEvent.OnPasswordChanged -> {
                val current = _uiState.value ?: LoginUiState()
                setState(current.copy(password = event.password, errorMessage = null))
            }
            is LoginUiEvent.OnDismissError -> {
                val current = _uiState.value ?: LoginUiState()
                setState(current.copy(errorMessage = null))
            }
            is LoginUiEvent.OnLoginClicked -> {
                performLogin()
            }
        }
    }

    private fun performLogin() {
        val currentState = _uiState.value ?: return
        if (currentState.isLoading) return

        setState(currentState.copy(isLoading = true, errorMessage = null))

        viewModelScope.launch {
            val result = loginUseCase(currentState.username, currentState.password)
            result.onSuccess {
                setState(currentState.copy(isLoading = false, errorMessage = null))
                emitEffect(LoginUiEffect.NavigateToDashboard)
            }.onFailure { throwable ->
                setState(currentState.copy(isLoading = false, errorMessage = throwable.message ?: "Authentication failed"))
            }
        }
    }
}
