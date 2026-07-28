package com.example.myapplication.shared.domain.model

/**
 * Enums representing available modules/features in the application.
 * Admin can grant/revoke specific features for each user via checkboxes.
 */
enum class FeaturePermission(
    val key: String,
    val displayName: String,
    val description: String
) {
    OPERATOR(
        key = "OPERATOR",
        displayName = "Operator Console",
        description = "Operate and monitor shuttles in real time"
    ),
    DIAGNOSTICS(
        key = "DIAGNOSTICS",
        displayName = "Diagnostics",
        description = "View engineering diagnostics and machine health"
    ),
    MAINTENANCE(
        key = "MAINTENANCE",
        displayName = "Maintenance Console",
        description = "Perform guided maintenance and sensor testing"
    ),
    REPORTS(
        key = "REPORTS",
        displayName = "Reports & Analytics",
        description = "Generate reports and analyze warehouse performance"
    ),
    SETTINGS(
        key = "SETTINGS",
        displayName = "Settings",
        description = "Configure system and shuttle preferences"
    ),
    USER_MANAGEMENT(
        key = "USER_MANAGEMENT",
        displayName = "User Directory & RBAC",
        description = "Manage users, assign roles, and configure feature permissions"
    ),
    SHUTTLE_MANAGEMENT(
        key = "SHUTTLE_MANAGEMENT",
        displayName = "Shuttle Management",
        description = "Register and manage connected radio shuttles"
    );

    companion object {
        fun fromKey(key: String): FeaturePermission? {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
        }
    }
}

/**
 * Enums representing specific configuration screens inside Settings.
 * Admin can grant/revoke individual settings sections for each user via checkboxes.
 */
enum class SettingPermission(
    val key: String,
    val displayName: String,
    val description: String
) {
    SYSTEM_CONFIGURATION(
        key = "SYSTEM_CONFIGURATION",
        displayName = "System Configuration",
        description = "Configure application and system settings"
    ),
    SHUTTLE_CONFIGURATION(
        key = "SHUTTLE_CONFIGURATION",
        displayName = "Shuttle Configuration",
        description = "Configure shuttle default parameters and limits"
    ),
    COMMUNICATION_SETTINGS(
        key = "COMMUNICATION_SETTINGS",
        displayName = "Communication Settings",
        description = "Configure communication infrastructure (MQTT & PLC)"
    ),
    REPORT_CONFIGURATION(
        key = "REPORT_CONFIGURATION",
        displayName = "Report Configuration",
        description = "Configure report generation preferences"
    );

    companion object {
        fun fromKey(key: String): SettingPermission? {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
        }
    }
}
