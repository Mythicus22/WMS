package com.example.myapplication.shared.data.repository

import com.example.myapplication.shared.core.security.PasswordHasher
import com.example.myapplication.shared.database.AppDatabase
import com.example.myapplication.shared.domain.model.FeaturePermission
import com.example.myapplication.shared.domain.model.SettingPermission
import com.example.myapplication.shared.domain.model.User
import com.example.myapplication.shared.domain.model.UserRole
import com.example.myapplication.shared.domain.repository.IUserRepository
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val database: AppDatabase
) : IUserRepository {

    private val queries = database.appDatabaseQueries

    override suspend fun seedDefaultAdminIfNeeded() {
        val adminUser = getUserByUsername("admin")
        if (adminUser == null) {
            val defaultAdmin = User(
                id = "user_admin_001",
                username = "admin",
                passwordHash = PasswordHasher.hashPassword("admin123"),
                role = UserRole.ADMIN,
                grantedFeatures = FeaturePermission.entries.toSet(),
                grantedSettings = SettingPermission.entries.toSet(),
                createdAt = System.currentTimeMillis()
            )
            queries.insertUser(
                id = defaultAdmin.id,
                username = defaultAdmin.username,
                passwordHash = defaultAdmin.passwordHash,
                role = defaultAdmin.role.key,
                grantedFeaturesCsv = defaultAdmin.grantedFeatures.joinToString(",") { it.key },
                grantedSettingsCsv = defaultAdmin.grantedSettings.joinToString(",") { it.key },
                createdAt = defaultAdmin.createdAt
            )
        } else {
            // Restore admin privileges if lost
            if (adminUser.role != UserRole.ADMIN || !adminUser.grantedFeatures.contains(FeaturePermission.USER_MANAGEMENT)) {
                val restoredAdmin = adminUser.copy(
                    role = UserRole.ADMIN,
                    grantedFeatures = adminUser.grantedFeatures + FeaturePermission.USER_MANAGEMENT
                )
                updateUser(restoredAdmin)
            }
        }
    }

    override suspend fun getUserByUsername(username: String): User? {
        val entity = queries.getUserByUsername(username).executeAsOneOrNull()
        return entity?.let { mapToDomain(it) }
    }

    override suspend fun getUserById(id: String): User? {
        val entity = queries.getUserById(id).executeAsOneOrNull()
        return entity?.let { mapToDomain(it) }
    }

    override fun getAllUsersFlow(): Flow<List<User>> {
        return queries.getAllUsers()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { mapToDomain(it) } }
    }

    override suspend fun getAllUsers(): List<User> {
        return queries.getAllUsers().executeAsList().map { mapToDomain(it) }
    }

    override suspend fun createUser(user: User): Result<User> {
        val existing = getUserByUsername(user.username)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Username '${user.username}' already exists"))
        }

        queries.insertUser(
            id = user.id,
            username = user.username,
            passwordHash = user.passwordHash,
            role = user.role.key,
            grantedFeaturesCsv = user.grantedFeatures.joinToString(",") { it.key },
            grantedSettingsCsv = user.grantedSettings.joinToString(",") { it.key },
            createdAt = user.createdAt
        )
        return Result.success(user)
    }

    override suspend fun updateUser(user: User): Result<User> {
        var userToUpdate = user
        if (user.username.lowercase() == "admin") {
            // Prevent removing admin role and user management feature from the main admin account
            userToUpdate = userToUpdate.copy(
                role = UserRole.ADMIN,
                grantedFeatures = user.grantedFeatures + FeaturePermission.USER_MANAGEMENT
            )
        }

        queries.updateUser(
            username = userToUpdate.username,
            passwordHash = userToUpdate.passwordHash,
            role = userToUpdate.role.key,
            grantedFeaturesCsv = userToUpdate.grantedFeatures.joinToString(",") { it.key },
            grantedSettingsCsv = userToUpdate.grantedSettings.joinToString(",") { it.key },
            id = userToUpdate.id
        )
        return Result.success(userToUpdate)
    }

    override suspend fun deleteUser(userId: String): Result<Boolean> {
        val user = getUserById(userId) ?: return Result.failure(IllegalArgumentException("User not found"))
        if (user.username.lowercase() == "admin") {
            return Result.failure(IllegalStateException("Cannot delete default admin user"))
        }

        queries.deleteUserById(userId)
        return Result.success(true)
    }

    private fun mapToDomain(entity: com.example.myapplication.shared.database.UserEntity): User {
        val features = entity.grantedFeaturesCsv
            .split(",")
            .mapNotNull { FeaturePermission.fromKey(it.trim()) }
            .toSet()

        val settings = entity.grantedSettingsCsv
            .split(",")
            .mapNotNull { SettingPermission.fromKey(it.trim()) }
            .toSet()

        return User(
            id = entity.id,
            username = entity.username,
            passwordHash = entity.passwordHash,
            role = UserRole.fromKey(entity.role),
            grantedFeatures = features,
            grantedSettings = settings,
            createdAt = entity.createdAt
        )
    }
}
