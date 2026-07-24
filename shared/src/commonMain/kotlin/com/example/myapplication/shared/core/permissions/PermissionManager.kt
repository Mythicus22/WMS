package com.example.myapplication.shared.core.permissions

// Permissions abstraction placeholder
interface PermissionManager {
    suspend fun hasPermission(permission: String): Boolean
    suspend fun requestPermission(permission: String): Boolean
}

