package com.example.myapplication.shared.features.diagnostics.model

import kotlinx.datetime.Instant

enum class DiagnosticsComponentState {
    HEALTHY, WARNING, FAULT, ONLINE, OFFLINE
}

data class DiagnosticsData(
    val shuttleId: String,
    val timestamp: Instant,
    val summary: DiagnosticsSummary,
    val motors: List<MotorDiagnostics>,
    val battery: BatteryDiagnostics,
    val plc: PLCDiagnostics,
    val communication: CommunicationDiagnostics,
    val sensors: List<SensorDiagnostics>,
    val relays: List<RelayDiagnostics>,
    val radio: RadioDiagnostics,
    val emergencyStop: EmergencyStopDiagnostics
)

data class DiagnosticsSummary(
    val overallState: DiagnosticsComponentState,
    val activeFaults: Int,
    val warningCount: Int,
    val onlineComponents: Int,
    val offlineComponents: Int,
    val communicationState: DiagnosticsComponentState,
    val batteryState: DiagnosticsComponentState,
    val plcState: DiagnosticsComponentState
)

data class MotorDiagnostics(
    val id: String,
    val name: String,
    val state: DiagnosticsComponentState,
    val voltage: Float, // V
    val current: Float, // A
    val power: Float, // kW
    val temperature: Float, // C
    val rpm: Int,
    val torque: Float, // Nm
    val direction: String, // FORWARD, REVERSE, STOPPED
    val isRunning: Boolean,
    val runtimeHours: Float,
    val driveStatus: String,
    val faultCode: String?
)

data class BatteryDiagnostics(
    val state: DiagnosticsComponentState,
    val voltage: Float, // V
    val current: Float, // A
    val temperature: Float, // C
    val percentage: Float, // 0-100%
    val remainingCapacityAh: Float,
    val estimatedRuntimeMinutes: Int,
    val chargeCycles: Int,
    val isCharging: Boolean,
    val healthPercentage: Float // 0-100%
)

data class PLCDiagnostics(
    val state: DiagnosticsComponentState,
    val statusText: String, // RUN, STOP, ERROR
    val cpuUtilizationPercent: Float,
    val scanTimeMs: Float,
    val memoryUsagePercent: Float,
    val programStatus: String,
    val watchdogStatus: String, // OK, TRIPPED
    val communicationStatus: String,
    val uptimeSeconds: Long
)

data class CommunicationDiagnostics(
    val state: DiagnosticsComponentState,
    val mqttStatus: String, // CONNECTED, DISCONNECTED
    val canStatus: String, // ACTIVE, ERROR_PASSIVE, BUS_OFF
    val radioReceiverStatus: String, // OK, ERROR
    val wifiSignalStrengthDbm: Int,
    val framesSent: Long,
    val framesReceived: Long,
    val packetLossPercent: Float,
    val communicationErrors: Int,
    val busLoadPercent: Float,
    val heartbeatStatus: String // OK, MISSED
)

enum class SensorType {
    TOP_PALLET, RACK_END, OBSTACLE, ENTRY_EXIT, DT35, LIFT, RAIL, OTHER
}

data class SensorDiagnostics(
    val id: String,
    val name: String,
    val type: SensorType,
    val state: DiagnosticsComponentState,
    val currentValue: String, // e.g. "DETECTED", "CLEAR", "1450 mm"
    val lastTriggerTime: Instant?,
    val triggerCount: Long,
    val signalStatus: String // STRONG, WEAK, NO_SIGNAL
)

data class RelayDiagnostics(
    val id: String,
    val name: String,
    val state: DiagnosticsComponentState,
    val coilStatus: Boolean, // Energized/De-energized
    val contactStatus: Boolean, // Closed/Open
    val switchingCount: Long
)

data class RadioDiagnostics(
    val state: DiagnosticsComponentState,
    val signalStrengthPercent: Float,
    val receiverStatus: String, // OK, ERROR
    val packetCount: Long,
    val packetLossCount: Long,
    val lastPacketReceivedTime: Instant?
)

data class EmergencyStopDiagnostics(
    val state: DiagnosticsComponentState,
    val isTriggered: Boolean,
    val lastTriggerTime: Instant?,
    val recoveryStatus: String // READY, REQUIRES_RESET
)
