package com.example.myapplication.shared.features.usermanagement.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.UiEffect
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.domain.model.FeaturePermission
import com.example.myapplication.shared.domain.model.SettingPermission
import com.example.myapplication.shared.domain.model.User
import com.example.myapplication.shared.domain.model.UserRole
import com.example.myapplication.shared.domain.usecase.ManageUsersUseCase
import com.example.myapplication.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class UserManagementUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAddUserDialogOpen: Boolean = false,
    val editingUser: User? = null,
    val deletingUser: User? = null
)

sealed interface UserManagementUiEvent : UiEvent {
    object OnAddUserClicked : UserManagementUiEvent
    data class OnEditUserClicked(val user: User) : UserManagementUiEvent
    data class OnDeleteUserClicked(val user: User) : UserManagementUiEvent
    object OnDismissDialogs : UserManagementUiEvent
    object OnDismissMessages : UserManagementUiEvent
    data class OnCreateUserSubmitted(
        val username: String,
        val passwordRaw: String,
        val role: UserRole,
        val grantedFeatures: Set<FeaturePermission>,
        val grantedSettings: Set<SettingPermission>
    ) : UserManagementUiEvent
    data class OnUpdateUserSubmitted(
        val userId: String,
        val role: UserRole,
        val grantedFeatures: Set<FeaturePermission>,
        val grantedSettings: Set<SettingPermission>,
        val newPasswordRaw: String?
    ) : UserManagementUiEvent
    data class OnConfirmDeleteUser(val userId: String) : UserManagementUiEvent
}

sealed interface UserManagementUiEffect : UiEffect

class UserManagementViewModel(
    private val manageUsersUseCase: ManageUsersUseCase
) : BaseViewModel<UserManagementUiState, UserManagementUiEvent, UserManagementUiEffect>() {

    init {
        setState(UserManagementUiState())
        observeUsers()
    }

    private fun observeUsers() {
        manageUsersUseCase.getAllUsersFlow().onEach { userList ->
            val current = _uiState.value ?: UserManagementUiState()
            setState(current.copy(users = userList))
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: UserManagementUiEvent) {
        val current = _uiState.value ?: UserManagementUiState()
        when (event) {
            is UserManagementUiEvent.OnAddUserClicked -> {
                setState(current.copy(isAddUserDialogOpen = true, errorMessage = null))
            }
            is UserManagementUiEvent.OnEditUserClicked -> {
                setState(current.copy(editingUser = event.user, errorMessage = null))
            }
            is UserManagementUiEvent.OnDeleteUserClicked -> {
                setState(current.copy(deletingUser = event.user, errorMessage = null))
            }
            is UserManagementUiEvent.OnDismissDialogs -> {
                setState(
                    current.copy(
                        isAddUserDialogOpen = false,
                        editingUser = null,
                        deletingUser = null,
                        errorMessage = null
                    )
                )
            }
            is UserManagementUiEvent.OnDismissMessages -> {
                setState(current.copy(errorMessage = null, successMessage = null))
            }
            is UserManagementUiEvent.OnCreateUserSubmitted -> {
                createUser(event)
            }
            is UserManagementUiEvent.OnUpdateUserSubmitted -> {
                updateUser(event)
            }
            is UserManagementUiEvent.OnConfirmDeleteUser -> {
                deleteUser(event.userId)
            }
        }
    }

    private fun createUser(event: UserManagementUiEvent.OnCreateUserSubmitted) {
        val current = _uiState.value ?: return
        viewModelScope.launch {
            setState(current.copy(isLoading = true, errorMessage = null))
            val result = manageUsersUseCase.createUser(
                username = event.username,
                passwordRaw = event.passwordRaw,
                role = event.role,
                grantedFeatures = event.grantedFeatures,
                grantedSettings = event.grantedSettings
            )

            result.onSuccess { newObj ->
                setState(
                    current.copy(
                        isLoading = false,
                        isAddUserDialogOpen = false,
                        successMessage = "User '${newObj.username}' created successfully"
                    )
                )
            }.onFailure { err ->
                setState(current.copy(isLoading = false, errorMessage = err.message ?: "Failed to create user"))
            }
        }
    }

    private fun updateUser(event: UserManagementUiEvent.OnUpdateUserSubmitted) {
        val current = _uiState.value ?: return
        viewModelScope.launch {
            setState(current.copy(isLoading = true, errorMessage = null))
            val result = manageUsersUseCase.updateUserPermissions(
                userId = event.userId,
                role = event.role,
                grantedFeatures = event.grantedFeatures,
                grantedSettings = event.grantedSettings,
                newPasswordRaw = event.newPasswordRaw
            )

            result.onSuccess { updatedObj ->
                setState(
                    current.copy(
                        isLoading = false,
                        editingUser = null,
                        successMessage = "Permissions for '${updatedObj.username}' updated"
                    )
                )
            }.onFailure { err ->
                setState(current.copy(isLoading = false, errorMessage = err.message ?: "Failed to update user"))
            }
        }
    }

    private fun deleteUser(userId: String) {
        val current = _uiState.value ?: return
        viewModelScope.launch {
            setState(current.copy(isLoading = true, errorMessage = null))
            val result = manageUsersUseCase.deleteUser(userId)
            result.onSuccess {
                setState(
                    current.copy(
                        isLoading = false,
                        deletingUser = null,
                        successMessage = "User deleted successfully"
                    )
                )
            }.onFailure { err ->
                setState(current.copy(isLoading = false, deletingUser = null, errorMessage = err.message ?: "Failed to delete user"))
            }
        }
    }
}
