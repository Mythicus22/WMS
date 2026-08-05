package com.example.myapplication.shared.features.auth.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.UiEffect
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.core.security.ConfigProvider
import com.example.myapplication.shared.core.security.SecurityProvider
import com.example.myapplication.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

data class SetupUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SetupUiEvent : UiEvent {
    data class OnUsernameChanged(val username: String) : SetupUiEvent
    data class OnPasswordChanged(val password: String) : SetupUiEvent
    data class OnConfirmPasswordChanged(val password: String) : SetupUiEvent
    object OnTogglePasswordVisibility : SetupUiEvent
    object OnFinishClicked : SetupUiEvent
    object OnDismissError : SetupUiEvent
}

sealed interface SetupUiEffect : UiEffect {
    object NavigateToLogin : SetupUiEffect
}

class SetupViewModel(
    private val securityProvider: SecurityProvider,
    private val configProvider: ConfigProvider
) : BaseViewModel<SetupUiState, SetupUiEvent, SetupUiEffect>() {

    init {
        setState(SetupUiState())
    }

    override fun onEvent(event: SetupUiEvent) {
        val current = _uiState.value ?: return
        when (event) {
            is SetupUiEvent.OnUsernameChanged -> setState(current.copy(username = event.username, errorMessage = null))
            is SetupUiEvent.OnPasswordChanged -> setState(current.copy(password = event.password, errorMessage = null))
            is SetupUiEvent.OnConfirmPasswordChanged -> setState(current.copy(confirmPassword = event.password, errorMessage = null))
            is SetupUiEvent.OnTogglePasswordVisibility -> setState(current.copy(isPasswordVisible = !current.isPasswordVisible))
            is SetupUiEvent.OnDismissError -> setState(current.copy(errorMessage = null))
            is SetupUiEvent.OnFinishClicked -> performSetup()
        }
    }

    private fun performSetup() {
        val current = _uiState.value ?: return
        if (current.isLoading) return

        if (current.username.isBlank()) {
            setState(current.copy(errorMessage = "Username cannot be empty"))
            return
        }
        if (current.password.isBlank()) {
            setState(current.copy(errorMessage = "Password cannot be empty"))
            return
        }
        if (current.password != current.confirmPassword) {
            setState(current.copy(errorMessage = "Passwords do not match"))
            return
        }

        setState(current.copy(isLoading = true, errorMessage = null))

        viewModelScope.launch {
            try {
                val hash = securityProvider.hashPassword(current.password)
                configProvider.saveConfig(current.username, hash)
                setState(current.copy(isLoading = false))
                emitEffect(SetupUiEffect.NavigateToLogin)
            } catch (e: Exception) {
                setState(current.copy(isLoading = false, errorMessage = "Setup failed: ${e.message}"))
            }
        }
    }
}
