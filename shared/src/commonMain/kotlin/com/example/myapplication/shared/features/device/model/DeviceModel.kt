package com.example.myapplication.shared.features.device.model

import kotlinx.serialization.Serializable

@Serializable
data class DiscoveredDevice(
    val deviceId: String,
    val serialNumber: String,
    val displayName: String,
    val protocolVersion: String,
    val firmwareVersion: String,
    val hardwareVersion: String,
    val manufacturer: String,
    val status: String,
    val lastSeenAt: Long,
    
    // Locally stored fields, populated by merging with local DB
    val localAlias: String? = null,
    val localNotes: String? = null
) {
    val nameToDisplay: String
        get() = localAlias?.takeIf { it.isNotBlank() } ?: displayName
}

data class DeviceCounts(
    val total: Long,
    val online: Long,
    val offline: Long
)
