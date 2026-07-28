package com.example.myapplication.shared.features.settings.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.UiEffect
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.domain.model.SettingPermission
import com.example.myapplication.shared.domain.model.User
import com.example.myapplication.shared.domain.usecase.GetCurrentUserUseCase
import com.example.myapplication.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class SettingsUiState(
    val currentUser: User? = null,
    val availableSettings: List<SettingPermission> = emptyList()
)

sealed interface SettingsUiEvent : UiEvent
sealed interface SettingsUiEffect : UiEffect

class SettingsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : BaseViewModel<SettingsUiState, SettingsUiEvent, SettingsUiEffect>() {

    init {
        setState(SettingsUiState())
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        getCurrentUserUseCase.currentUser.onEach { user ->
            val settings = if (user != null) {
                SettingPermission.entries.filter { user.grantedSettings.contains(it) }
            } else {
                emptyList()
            }
            setState(SettingsUiState(currentUser = user, availableSettings = settings))
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: SettingsUiEvent) {
    }
}
