package com.example.myapplication.shared.domain.repository

import com.example.myapplication.shared.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserById(id: String): User?
    fun getAllUsersFlow(): Flow<List<User>>
    suspend fun getAllUsers(): List<User>
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(userId: String): Result<Boolean>
    suspend fun seedDefaultAdminIfNeeded()
}
