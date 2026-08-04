package com.example.myapplication.shared.communication.model

import com.example.myapplication.shared.features.diagnostics.model.*
import kotlinx.serialization.Serializable

@Serializable
data class WspInfoPayload(
    val deviceId: String,
    val timestamp: Long,
    val serialNumber: String,
    val displayName: String,
    val protocolVersion: String,
    val firmwareVersion: String,
    val hardwareVersion: String,
    val manufacturer: String,
    val status: String
)

@Serializable
data class WspCommandPayload(
    val deviceId: String,
    val timestamp: Long,
    val requestId: String,
    val command: String,
    val parameters: Map<String, String>? = null
)

@Serializable
data class WspResponsePayload(
    val deviceId: String,
    val timestamp: Long,
    val requestId: String,
    val status: String,
    val message: String? = null
)

@Serializable
data class WspStatusPayload(
    val deviceId: String,
    val timestamp: Long,
    val state: String,
    val currentTask: String? = null,
    val batteryPercentage: Int,
    val speed: Double,
    val direction: String,
    val rackPosition: String? = null,
    val liftPosition: String? = null,
    val emergencyStop: Boolean,
    val activeFaultCount: Int
)

@Serializable
data class WspHeartbeatPayload(
    val deviceId: String,
    val timestamp: Long
)

@Serializable
data class WspTelemetryPayload(
    val deviceId: String,
    val timestamp: Long,
    val motorVoltage: Double,
    val motorCurrent: Double,
    val batteryVoltage: Double,
    val batteryCurrent: Double,
    val speed: Double,
    val motorTemperature: Double
)

@Serializable
data class WspDiagnosticsPayload(
    val deviceId: String,
    val timestamp: Long,
    val motors: List<WspMotorDiagnostics>? = null,
    val battery: WspBatteryDiagnostics? = null,
    val plc: WspPLCDiagnostics? = null,
    val communication: WspCommunicationDiagnostics? = null,
    val sensors: List<WspSensorDiagnostics>? = null,
    val relays: List<WspRelayDiagnostics>? = null,
    val radio: WspRadioDiagnostics? = null,
    val emergencyStop: WspEmergencyStopDiagnostics? = null
)

// Sub-components for Diagnostics payload mapping to 03_MESSAGE_SPECIFICATION.md
@Serializable
data class WspMotorDiagnostics(
    val voltage: Double,
    val current: Double,
    val power: Double,
    val temperature: Double,
    val rpm: Int,
    val torque: Double,
    val direction: String,
    val runtime: Long,
    val status: String,
    val faultCode: String
)

@Serializable
data class WspBatteryDiagnostics(
    val voltage: Double,
    val current: Double,
    val temperature: Double,
    val percentage: Int,
    val remainingCapacity: Double,
    val estimatedRuntime: Int,
    val chargeCycles: Int,
    val health: String,
    val charging: Boolean
)

@Serializable
data class WspPLCDiagnostics(
    val status: String,
    val scanTime: Int,
    val cpuUtilization: Int,
    val memoryUsage: Int,
    val watchdog: String,
    val uptime: Long
)

@Serializable
data class WspCommunicationDiagnostics(
    val mqtt: String,
    val wifiSignal: Int,
    val radio: String,
    val can: String,
    val framesSent: Long,
    val framesReceived: Long,
    val packetLoss: Int,
    val busLoad: Int
)

@Serializable
data class WspSensorDiagnostics(
    val sensorId: String,
    val state: Boolean,
    val health: String,
    val triggerCount: Long,
    val lastTrigger: Long
)

@Serializable
data class WspRelayDiagnostics(
    val relayId: String,
    val coil: Boolean,
    val contact: Boolean,
    val switchCount: Long
)

@Serializable
data class WspRadioDiagnostics(
    val status: String,
    val signalStrength: Int,
    val packetCount: Long,
    val packetLoss: Int,
    val lastPacket: Long
)

@Serializable
data class WspEmergencyStopDiagnostics(
    val active: Boolean,
    val lastTriggered: Long,
    val recovered: Boolean
)

@Serializable
data class WspMaintenanceRequestPayload(
    val deviceId: String,
    val timestamp: Long,
    val requestId: String,
    val test: String
)

@Serializable
data class WspMaintenanceResultPayload(
    val deviceId: String,
    val timestamp: Long,
    val requestId: String,
    val test: String,
    val result: String,
    val details: String? = null
)

@Serializable
data class WspFaultPayload(
    val deviceId: String,
    val timestamp: Long,
    val faultCode: String,
    val severity: String,
    val component: String,
    val description: String,
    val suggestedAction: String,
    val active: Boolean
)

@Serializable
data class WspLogPayload(
    val deviceId: String,
    val timestamp: Long,
    val level: String,
    val message: String
)
