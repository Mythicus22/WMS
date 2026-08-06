package com.example.myapplication.shared.communication.model

import com.example.myapplication.shared.features.diagnostics.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class WspWebSocketMessage(
    val type: String,
    val data: JsonElement
)

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
data class WspReportsPayload(
    val deviceId: String,
    val timestamp: Long,
    val summary: WspReportSummary,
    val storeOperations: List<WspStoreOperationRecord>,
    val retrieveOperations: List<WspRetrieveOperationRecord>,
    val productivity: WspProductivityRecord,
    val taskHistory: List<WspTaskRecord> = emptyList(),
    val missionHistory: List<WspMissionRecord> = emptyList(),
    val utilization: List<WspUtilizationRecord> = emptyList(),
    val batteryReport: List<WspBatteryRecord> = emptyList(),
    val motorRuntime: List<WspMotorRuntimeRecord> = emptyList(),
    val faultHistory: List<WspFaultRecord> = emptyList(),
    val maintenanceHistory: List<WspMaintenanceRecord> = emptyList()
)

@Serializable
data class WspReportSummary(
    val totalStoreOperations: Int,
    val totalRetrieveOperations: Int,
    val totalTasksCompleted: Int,
    val totalFaults: Int,
    val avgBatteryLevel: Float,
    val avgProductivity: Float,
    val activeFaults: Int
)

@Serializable
data class WspStoreOperationRecord(
    val id: String,
    val location: String,
    val timeTakenSec: Int,
    val status: String,
    val timestamp: String
)

@Serializable
data class WspRetrieveOperationRecord(
    val id: String,
    val location: String,
    val timeTakenSec: Int,
    val status: String,
    val timestamp: String
)

@Serializable
data class WspProductivityRecord(
    val completed: Int,
    val failed: Int,
    val efficiencyPercentage: Float
)

@Serializable
data class WspTaskRecord(
    val id: String,
    val type: String,
    val priority: String,
    val status: String,
    val createdTime: String,
    val completedTime: String,
    val durationSec: Int
)

@Serializable
data class WspMissionRecord(
    val id: String,
    val type: String,
    val totalTasks: Int,
    val status: String,
    val distanceTraveled: Float,
    val startTime: String,
    val endTime: String,
    val faultsEncountered: Int
)

@Serializable
data class WspUtilizationRecord(
    val date: String,
    val activeHours: Float,
    val idleHours: Float,
    val utilizationPercentage: Float,
    val tasksCompleted: Int,
    val distanceTraveled: Float
)

@Serializable
data class WspBatteryRecord(
    val id: String,
    val timestamp: String,
    val endPercentage: Float,
    val endVoltage: Float,
    val endCurrent: Float,
    val endTemperature: Float,
    val dischargeTimeMin: Int,
    val status: String
)

@Serializable
data class WspMotorRuntimeRecord(
    val id: String,
    val date: String,
    val driveMotorHours: Float,
    val liftMotorHours: Float,
    val driveMotorStarts: Int,
    val liftMotorStarts: Int,
    val avgDriveTemp: Float,
    val avgLiftTemp: Float
)

@Serializable
data class WspFaultRecord(
    val id: String,
    val faultCode: String,
    val faultType: String,
    val severity: String,
    val description: String,
    val timeOccurred: String,
    val timeResolved: String?,
    val resolvedBy: String?,
    val downtimeMin: Int
)

@Serializable
data class WspMaintenanceRecord(
    val id: String,
    val type: String,
    val technician: String,
    val date: String,
    val durationMin: Int,
    val partsReplaced: List<String>,
    val notes: String,
    val nextScheduledDate: String
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
