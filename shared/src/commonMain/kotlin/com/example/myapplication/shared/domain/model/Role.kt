package com.example.myapplication.shared.domain.model

/**
 * Roles supported by the system.
 */
enum class UserRole(
    val key: String,
    val displayName: String,
    val description: String
) {
    ADMIN(
        key = "ADMIN",
        displayName = "Admin",
        description = "Full administrative access, user directory & system management"
    ),
    OPERATOR(
        key = "OPERATOR",
        displayName = "Operator",
        description = "Live shuttle operation, store/retrieve missions"
    ),
    MAINTENANCE(
        key = "MAINTENANCE",
        displayName = "Maintenance",
        description = "Guided sensor tests and maintenance calibration"
    ),
    ENGINEER(
        key = "ENGINEER",
        displayName = "Engineer",
        description = "Diagnostics telemetry and system health monitoring"
    ),
    VIEWER(
        key = "VIEWER",
        displayName = "Viewer",
        description = "Read-only access to operational reports and dashboard"
    );

    companion object {
        fun fromKey(key: String): UserRole {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: VIEWER
        }
    }
}
