package com.example.myapplication.shared.features.diagnostics.repository

import com.example.myapplication.shared.communication.service.CommunicationService
import com.example.myapplication.shared.features.diagnostics.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class DiagnosticsRepositoryImpl(
    private val communicationService: CommunicationService
) : DiagnosticsRepository {

    override fun getDiagnostics(shuttleId: String): Flow<DiagnosticsData> {
        return communicationService.observeDiagnostics(shuttleId).map { payload ->
            val timestamp = Instant.fromEpochMilliseconds(payload.timestamp)
            
            // Map the WSP Sub-payloads to the Domain Models
            val mappedMotors = payload.motors?.map {
                MotorDiagnostics(
                    id = "M", name = "Motor", state = DiagnosticsComponentState.valueOf(it.status),
                    voltage = it.voltage.toFloat(), current = it.current.toFloat(), power = it.power.toFloat(),
                    temperature = it.temperature.toFloat(), rpm = it.rpm, torque = it.torque.toFloat(),
                    direction = it.direction, isRunning = it.rpm > 0, runtimeHours = it.runtime / 3600f,
                    driveStatus = it.status, faultCode = it.faultCode
                )
            } ?: emptyList()
            
            val mappedBattery = payload.battery?.let {
                BatteryDiagnostics(
                    state = DiagnosticsComponentState.valueOf(it.health),
                    voltage = it.voltage.toFloat(), current = it.current.toFloat(),
                    temperature = it.temperature.toFloat(), percentage = it.percentage.toFloat(),
                    remainingCapacityAh = it.remainingCapacity.toFloat(),
                    estimatedRuntimeMinutes = it.estimatedRuntime, chargeCycles = it.chargeCycles,
                    isCharging = it.charging, healthPercentage = 100f // Mapped properly if needed
                )
            } ?: BatteryDiagnostics(DiagnosticsComponentState.OFFLINE, 0f, 0f, 0f, 0f, 0f, 0, 0, false, 0f)

            val mappedPlc = payload.plc?.let {
                PLCDiagnostics(
                    state = DiagnosticsComponentState.valueOf(it.status),
                    statusText = it.status, cpuUtilizationPercent = it.cpuUtilization.toFloat(),
                    scanTimeMs = it.scanTime.toFloat(), memoryUsagePercent = it.memoryUsage.toFloat(),
                    programStatus = "OK", watchdogStatus = it.watchdog,
                    communicationStatus = "OK", uptimeSeconds = it.uptime
                )
            } ?: PLCDiagnostics(DiagnosticsComponentState.OFFLINE, "", 0f, 0f, 0f, "", "", "", 0L)

            val mappedComms = payload.communication?.let {
                CommunicationDiagnostics(
                    state = DiagnosticsComponentState.valueOf(it.mqtt),
                    mqttStatus = it.mqtt, canStatus = it.can, radioReceiverStatus = it.radio,
                    wifiSignalStrengthDbm = it.wifiSignal, framesSent = it.framesSent,
                    framesReceived = it.framesReceived, packetLossPercent = it.packetLoss.toFloat(),
                    communicationErrors = 0, busLoadPercent = it.busLoad.toFloat(),
                    heartbeatStatus = "OK"
                )
            } ?: CommunicationDiagnostics(DiagnosticsComponentState.OFFLINE, "", "", "", 0, 0L, 0L, 0f, 0, 0f, "")
            
            val summary = DiagnosticsSummary(
                overallState = DiagnosticsComponentState.HEALTHY, // Could be derived
                activeFaults = 0, warningCount = 0, onlineComponents = 0, offlineComponents = 0,
                communicationState = mappedComms.state, batteryState = mappedBattery.state, plcState = mappedPlc.state
            )

            DiagnosticsData(
                shuttleId = shuttleId,
                timestamp = timestamp,
                summary = summary,
                motors = mappedMotors,
                battery = mappedBattery,
                plc = mappedPlc,
                communication = mappedComms,
                sensors = emptyList(), // Maps logic
                relays = emptyList(),
                radio = RadioDiagnostics(DiagnosticsComponentState.OFFLINE, 0f, "", 0L, 0L, null),
                emergencyStop = EmergencyStopDiagnostics(DiagnosticsComponentState.OFFLINE, false, null, "")
            )
        }
    }
}
