package com.example.myapplication.shared.core.session

import com.example.myapplication.shared.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * DataStore & in-memory session manager.
 * Tracks logged-in user state, persistent across app usage.
 */
class SessionManager {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun saveSession(user: User) {
        _currentUser.update { user }
    }

    fun updateSessionUser(user: User) {
        if (_currentUser.value?.id == user.id) {
            _currentUser.update { user }
        }
    }

    fun clearSession() {
        _currentUser.update { null }
    }

    fun isLoggedIn(): Boolean {
        return _currentUser.value != null
    }

    fun getCurrentUser(): User? {
        return _currentUser.value
    }
}
