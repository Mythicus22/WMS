package com.example.myapplication.shared.core.session

import com.example.myapplication.shared.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.example.myapplication.shared.core.security.ConfigProvider
import com.example.myapplication.shared.domain.repository.IUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

/**
 * Session manager.
 * Tracks logged-in user state, persistent across app usage using ConfigProvider.
 */
class SessionManager(
    private val configProvider: ConfigProvider,
    private val userRepository: IUserRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Attempt to restore session on startup
        val savedSessionId = configProvider.getSessionId()
        if (savedSessionId != null) {
            scope.launch {
                val user = userRepository.getUserById(savedSessionId)
                if (user != null) {
                    _currentUser.update { user }
                } else {
                    configProvider.clearSessionId()
                }
            }
        }
    }

    fun saveSession(user: User) {
        _currentUser.update { user }
        configProvider.saveSessionId(user.id)
    }

    fun updateSessionUser(user: User) {
        if (_currentUser.value?.id == user.id) {
            _currentUser.update { user }
        }
    }

    fun clearSession() {
        _currentUser.update { null }
        configProvider.clearSessionId()
    }

    fun isLoggedIn(): Boolean {
        return _currentUser.value != null
    }

    fun getCurrentUser(): User? {
        return _currentUser.value
    }
}
