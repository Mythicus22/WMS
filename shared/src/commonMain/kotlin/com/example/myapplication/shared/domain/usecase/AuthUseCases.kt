package com.example.myapplication.shared.domain.usecase

import com.example.myapplication.shared.core.security.ConfigProvider
import com.example.myapplication.shared.core.security.SecurityProvider
import com.example.myapplication.shared.core.session.SessionManager
import com.example.myapplication.shared.domain.model.FeaturePermission
import com.example.myapplication.shared.domain.model.SettingPermission
import com.example.myapplication.shared.domain.model.User
import com.example.myapplication.shared.domain.model.UserRole
import com.example.myapplication.shared.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase for authenticating user credentials and establishing a session.
 */
class LoginUseCase(
    private val userRepository: IUserRepository,
    private val sessionManager: SessionManager,
    private val configProvider: ConfigProvider,
    private val securityProvider: SecurityProvider
) {
    suspend operator fun invoke(usernameInput: String, passwordInput: String): Result<User> {
        val username = usernameInput.trim()
        val password = passwordInput.trim()

        if (username.isEmpty()) {
            return Result.failure(IllegalArgumentException("Username cannot be empty"))
        }
        if (password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }

        val config = configProvider.readConfig()
        if (config != null && config.first == username) {
            val isPasswordCorrect = securityProvider.verifyPassword(password, config.second)
            if (isPasswordCorrect) {
                val adminUser = User(
                    id = "master_admin",
                    username = config.first,
                    passwordHash = config.second,
                    role = UserRole.ADMIN,
                    grantedFeatures = FeaturePermission.entries.toSet(),
                    grantedSettings = SettingPermission.entries.toSet(),
                    createdAt = 0L
                )
                sessionManager.saveSession(adminUser)
                return Result.success(adminUser)
            } else {
                return Result.failure(IllegalArgumentException("Invalid username or password"))
            }
        }

        // Fallback to database users if any
        val user = userRepository.getUserByUsername(username)
            ?: return Result.failure(IllegalArgumentException("Invalid username or password"))

        val isPasswordCorrect = securityProvider.verifyPassword(password, user.passwordHash)
        if (!isPasswordCorrect) {
            return Result.failure(IllegalArgumentException("Invalid username or password"))
        }

        sessionManager.saveSession(user)
        return Result.success(user)
    }
}

/**
 * UseCase for ending the user session.
 */
class LogoutUseCase(
    private val sessionManager: SessionManager
) {
    operator fun invoke() {
        sessionManager.clearSession()
    }
}

/**
 * UseCase for observing/retrieving current logged-in user.
 */
class GetCurrentUserUseCase(
    private val sessionManager: SessionManager
) {
    val currentUser: Flow<User?> = sessionManager.currentUser

    fun get(): User? {
        return sessionManager.getCurrentUser()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}

/**
 * UseCase for Admin user management operations (CRUD + feature/setting permission checkboxes).
 */
class ManageUsersUseCase(
    private val userRepository: IUserRepository,
    private val sessionManager: SessionManager,
    private val securityProvider: SecurityProvider
) {
    fun getAllUsersFlow(): Flow<List<User>> = userRepository.getAllUsersFlow()

    suspend fun getAllUsers(): List<User> = userRepository.getAllUsers()

    suspend fun createUser(
        username: String,
        passwordRaw: String,
        role: UserRole,
        grantedFeatures: Set<FeaturePermission>,
        grantedSettings: Set<SettingPermission>
    ): Result<User> {
        val currentUser = sessionManager.getCurrentUser()
        if (currentUser == null || !currentUser.hasFeature(FeaturePermission.USER_MANAGEMENT)) {
            return Result.failure(IllegalStateException("Only users with User Management permission can create users"))
        }

        val id = "user_" + System.currentTimeMillis()
        val passwordHash = securityProvider.hashPassword(passwordRaw)
        val newUser = User(
            id = id,
            username = username,
            passwordHash = passwordHash,
            role = role,
            grantedFeatures = grantedFeatures,
            grantedSettings = grantedSettings,
            createdAt = System.currentTimeMillis()
        )

        return userRepository.createUser(newUser)
    }

    suspend fun updateUserPermissions(
        userId: String,
        role: UserRole,
        grantedFeatures: Set<FeaturePermission>,
        grantedSettings: Set<SettingPermission>,
        newPasswordRaw: String? = null
    ): Result<User> {
        val currentUser = sessionManager.getCurrentUser()
        if (currentUser == null || !currentUser.hasFeature(FeaturePermission.USER_MANAGEMENT)) {
            return Result.failure(IllegalStateException("Only users with User Management permission can modify user permissions"))
        }

        val existingUser = userRepository.getUserById(userId)
            ?: return Result.failure(IllegalArgumentException("User not found"))

        val newPasswordHash = if (!newPasswordRaw.isNullOrBlank()) {
            securityProvider.hashPassword(newPasswordRaw)
        } else {
            existingUser.passwordHash
        }

        val updatedUser = existingUser.copy(
            role = role,
            passwordHash = newPasswordHash,
            grantedFeatures = grantedFeatures,
            grantedSettings = grantedSettings
        )

        // Ensure we don't remove USER_MANAGEMENT from the last user who has it
        if (!updatedUser.hasFeature(FeaturePermission.USER_MANAGEMENT)) {
            val allUsers = userRepository.getAllUsers()
            val othersWithManagement = allUsers.filter { it.id != userId && it.hasFeature(FeaturePermission.USER_MANAGEMENT) }
            if (othersWithManagement.isEmpty()) {
                return Result.failure(IllegalStateException("Cannot remove User Management permission from the last admin"))
            }
        }

        val result = userRepository.updateUser(updatedUser)
        result.getOrNull()?.let { updated ->
            sessionManager.updateSessionUser(updated)
        }
        return result
    }

    suspend fun deleteUser(userId: String): Result<Boolean> {
        val currentUser = sessionManager.getCurrentUser()
        if (currentUser == null || !currentUser.hasFeature(FeaturePermission.USER_MANAGEMENT)) {
            return Result.failure(IllegalStateException("Only users with User Management permission can delete users"))
        }

        if (currentUser.id == userId) {
            return Result.failure(IllegalStateException("Cannot delete your own active account"))
        }

        // Prevent deleting the last user with USER_MANAGEMENT permission
        val allUsers = userRepository.getAllUsers()
        val usersWithManagement = allUsers.filter { it.hasFeature(FeaturePermission.USER_MANAGEMENT) }
        if (usersWithManagement.size == 1 && usersWithManagement.first().id == userId) {
            return Result.failure(IllegalStateException("Cannot delete the last user with User Management permission"))
        }

        return userRepository.deleteUser(userId)
    }
}
