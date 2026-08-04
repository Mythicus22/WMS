package com.example.myapplication.shared.domain.repository

import com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepository

// Auth repository interface
interface IAuthRepository {
    // suspend fun login(username: String, password: String): Result<AuthToken>
    // suspend fun logout(): Result<Unit>
    // suspend fun isLoggedIn(): Result<Boolean>
}

// Dashboard repository interface
interface IDashboardRepository {
    // Placeholder for dashboard-related repository functions
}

// Shuttle repository interface
interface IDiscoveryRepository {
    // Placeholder for shuttle-related repository functions
}

// Registered Shuttle repository interface
interface IRegisteredShuttleRepository {
    // Placeholder for registered shuttle-related repository functions
}

// Operator repository interface
interface IOperatorRepository {
    // Placeholder for operator-related repository functions
}

// Diagnostics repository interface
interface IDiagnosticsRepository {
    // Placeholder for diagnostics-related repository functions
}

// Maintenance repository interface
interface IMaintenanceRepository {
    // Placeholder for maintenance-related repository functions
}

// Reports repository interface
interface IReportsRepository {
    // Placeholder for reports-related repository functions
}

// Settings repository interface
interface ISettingsRepository {
    // suspend fun getSettings(): Result<AppSettings>
    // suspend fun updateSettings(settings: AppSettings): Result<Unit>
}

// UserManagement repository interface
interface IUserManagementRepository {
    // Placeholder for user management-related repository functions
}

