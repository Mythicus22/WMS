package com.example.myapplication.shared.features.operator.model

import kotlinx.serialization.Serializable

enum class ShuttleCommandType(val displayName: String, val isAutomatic: Boolean) {
    AUTO_STORE("Automatic Store", true),
    AUTO_RETRIEVE("Automatic Retrieve", true),
    COMPACT_PUSH("Compact Push", true),
    COMPACT_PULL("Compact Pull", true),
    COUNT_ITEMS("Count Items", true),
    MOVE_FORWARD("Move Forward", false),
    MOVE_REVERSE("Move Reverse", false),
    LIFT_UP("Lift Up", false),
    LIFT_DOWN("Lift Down", false),
    STOP("STOP (Emergency/Action)", false)
}

@Serializable
data class ShuttleLiveStatus(
    val shuttleId: String,
    val isOnline: Boolean = true,
    val currentMission: String = "Idle - Standby",
    val currentState: String = "READY",
    val speed: String = "0.0 m/s",
    val direction: String = "STOPPED",
    val batteryPercent: Int = 88,
    val rackPosition: String = "Aisle 02 / Bay 14 / Tier 03",
    val liftPosition: String = "DOWN",
    val commStatus: String = "CONNECTED (PLC/MQTT)",
    val isEmergencyStopActive: Boolean = false
)

enum class FaultSeverity(val displayName: String) {
    INFO("INFO"),
    WARNING("WARNING"),
    FAULT("CRITICAL FAULT")
}

@Serializable
data class ShuttleFault(
    val faultId: String,
    val faultName: String,
    val description: String,
    val severity: FaultSeverity,
    val timestamp: String,
    val suggestedAction: String
)
