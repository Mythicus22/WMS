package com.example.myapplication.shared.features.shuttle.model

import kotlinx.serialization.Serializable

@Serializable
data class Shuttle(
    val id: String,
    val name: String,
    val description: String?,
    val plcIpAddress: String,
    val mqttPublishTopic: String,
    val mqttSubscribeTopic: String,
    val communicationTimeout: Int,
    val heartbeatInterval: Int,
    val firmwareVersion: String,
    val isEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class ShuttleCounts(
    val total: Long,
    val enabled: Long,
    val disabled: Long
)
