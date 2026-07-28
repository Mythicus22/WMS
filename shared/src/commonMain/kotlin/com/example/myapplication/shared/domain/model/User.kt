package com.example.myapplication.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain User representation holding granted features and settings lists.
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val passwordHash: String,
    val role: UserRole,
    val grantedFeatures: Set<FeaturePermission>,
    val grantedSettings: Set<SettingPermission>,
    val createdAt: Long = 0L
) {
    val isAdmin: Boolean get() = role == UserRole.ADMIN

    fun hasFeature(feature: FeaturePermission): Boolean {
        return grantedFeatures.contains(feature)
    }

    fun hasSetting(setting: SettingPermission): Boolean {
        return grantedSettings.contains(setting)
    }
}
