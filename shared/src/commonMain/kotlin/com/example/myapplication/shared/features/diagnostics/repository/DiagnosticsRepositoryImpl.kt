package com.example.myapplication.shared.features.diagnostics.repository

import com.example.myapplication.shared.features.diagnostics.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class DiagnosticsRepositoryImpl : DiagnosticsRepository {

    override fun getDiagnostics(shuttleId: String): Flow<DiagnosticsData> = flow {
        // Mock periodic updates
        while (true) {
            emit(generateMockDiagnostics(shuttleId))
            delay(2000) // update every 2 seconds
        }
    }

    private fun generateMockDiagnostics(shuttleId: String): DiagnosticsData {
        val isAll = shuttleId == "ALL"
        
        val motor1 = MotorDiagnostics(
            id = "M1", name = "Drive Motor Left", state = DiagnosticsComponentState.HEALTHY,
            voltage = 48.2f, current = 12.5f, power = 0.6f, temperature = 42.1f,
            rpm = 1450, torque = 22.4f, direction = "FORWARD", isRunning = true,
            runtimeHours = 1250.5f, driveStatus = "OK", faultCode = null
        )
        val motor2 = MotorDiagnostics(
            id = "M2", name = "Drive Motor Right", state = DiagnosticsComponentState.HEALTHY,
            voltage = 48.1f, current = 12.3f, power = 0.59f, temperature = 41.8f,
            rpm = 1450, torque = 22.1f, direction = "FORWARD", isRunning = true,
            runtimeHours = 1250.5f, driveStatus = "OK", faultCode = null
        )
        val motor3 = MotorDiagnostics(
            id = "M3", name = "Lift Motor", state = DiagnosticsComponentState.WARNING,
            voltage = 47.5f, current = 28.0f, power = 1.33f, temperature = 58.5f,
            rpm = 0, torque = 0.0f, direction = "STOPPED", isRunning = false,
            runtimeHours = 840.2f, driveStatus = "OVERTEMP_WARN", faultCode = "W-015"
        )

        val battery = BatteryDiagnostics(
            state = DiagnosticsComponentState.HEALTHY,
            voltage = 50.4f, current = -24.8f, temperature = 35.2f,
            percentage = 82.5f, remainingCapacityAh = 82.5f, estimatedRuntimeMinutes = 198,
            chargeCycles = 145, isCharging = false, healthPercentage = 95.0f
        )

        val plc = PLCDiagnostics(
            state = DiagnosticsComponentState.HEALTHY,
            statusText = "RUN", cpuUtilizationPercent = 24.5f, scanTimeMs = 12.4f,
            memoryUsagePercent = 45.2f, programStatus = "VALID", watchdogStatus = "OK",
            communicationStatus = "ACTIVE", uptimeSeconds = 864000L
        )

        val comms = CommunicationDiagnostics(
            state = DiagnosticsComponentState.HEALTHY,
            mqttStatus = "CONNECTED", canStatus = "ACTIVE", radioReceiverStatus = "OK",
            wifiSignalStrengthDbm = -65, framesSent = 154200L, framesReceived = 154198L,
            packetLossPercent = 0.01f, communicationErrors = 2, busLoadPercent = 18.5f,
            heartbeatStatus = "OK"
        )

        val sensors = listOf(
            SensorDiagnostics("S1", "Top Pallet 1", SensorType.TOP_PALLET, DiagnosticsComponentState.HEALTHY, "CLEAR", Clock.System.now(), 4500, "STRONG"),
            SensorDiagnostics("S2", "Top Pallet 2", SensorType.TOP_PALLET, DiagnosticsComponentState.HEALTHY, "CLEAR", Clock.System.now(), 4500, "STRONG"),
            SensorDiagnostics("S3", "Rack End Front", SensorType.RACK_END, DiagnosticsComponentState.HEALTHY, "DETECTED", Clock.System.now(), 12000, "STRONG"),
            SensorDiagnostics("S4", "Rack End Rear", SensorType.RACK_END, DiagnosticsComponentState.HEALTHY, "CLEAR", Clock.System.now(), 12000, "STRONG"),
            SensorDiagnostics("S5", "Obstacle Front Center", SensorType.OBSTACLE, DiagnosticsComponentState.HEALTHY, "CLEAR", null, 0, "STRONG"),
            SensorDiagnostics("S6", "DT35 Lift", SensorType.DT35, DiagnosticsComponentState.HEALTHY, "450 mm", Clock.System.now(), 8000, "STRONG")
        )

        val relays = listOf(
            RelayDiagnostics("R1", "Main Contactor", DiagnosticsComponentState.HEALTHY, true, true, 1250),
            RelayDiagnostics("R2", "Brake Relay", DiagnosticsComponentState.HEALTHY, false, false, 15000)
        )

        val radio = RadioDiagnostics(
            state = DiagnosticsComponentState.HEALTHY,
            signalStrengthPercent = 88.5f, receiverStatus = "OK",
            packetCount = 45000L, packetLossCount = 12L, lastPacketReceivedTime = Clock.System.now()
        )

        val eStop = EmergencyStopDiagnostics(
            state = DiagnosticsComponentState.HEALTHY,
            isTriggered = false, lastTriggerTime = null, recoveryStatus = "READY"
        )

        val summary = DiagnosticsSummary(
            overallState = DiagnosticsComponentState.HEALTHY,
            activeFaults = 0, warningCount = 1,
            onlineComponents = 24, offlineComponents = 0,
            communicationState = DiagnosticsComponentState.HEALTHY,
            batteryState = DiagnosticsComponentState.HEALTHY,
            plcState = DiagnosticsComponentState.HEALTHY
        )

        return DiagnosticsData(
            shuttleId = shuttleId,
            timestamp = Clock.System.now(),
            summary = summary,
            motors = listOf(motor1, motor2, motor3),
            battery = battery,
            plc = plc,
            communication = comms,
            sensors = sensors,
            relays = relays,
            radio = radio,
            emergencyStop = eStop
        )
    }
}
