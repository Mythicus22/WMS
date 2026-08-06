package com.example.myapplication.shared.features.operator.repository

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.operator.model.FaultSeverity
import com.example.myapplication.shared.features.operator.model.ShuttleCommandType
import com.example.myapplication.shared.features.operator.model.ShuttleFault
import com.example.myapplication.shared.features.operator.model.ShuttleLiveStatus
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

import com.example.myapplication.shared.communication.service.CommunicationService
import com.example.myapplication.shared.communication.model.WspCommandPayload

class CommandRepositoryImpl(
    private val communicationService: CommunicationService
) : CommandRepository {

    override fun observeLiveStatus(shuttleId: String): Flow<ShuttleLiveStatus> {
        return kotlinx.coroutines.flow.combine(
            communicationService.observeStatus(shuttleId),
            communicationService.observeTelemetry(shuttleId)
        ) { status, telemetry ->
            ShuttleLiveStatus(
                shuttleId = shuttleId,
                isOnline = true, // We received a message, so it's online
                currentMission = status.currentTask ?: "IDLE",
                currentState = status.state,
                speed = "${telemetry.speed} m/s",
                direction = status.direction,
                batteryPercent = telemetry.batteryVoltage.toInt(), // rough approx, or use status.batteryPercentage
                rackPosition = status.rackPosition ?: "Unknown",
                liftPosition = status.liftPosition ?: "Unknown",
                commStatus = "CONNECTED",
                isEmergencyStopActive = false
            )
        }.catch { 
            // Fallback if no data or offline
            emit(ShuttleLiveStatus(
                shuttleId = shuttleId,
                isOnline = false,
                currentMission = "Offline",
                currentState = "OFFLINE",
                speed = "0.0 m/s",
                direction = "NONE",
                batteryPercent = 0,
                rackPosition = "Unknown",
                liftPosition = "Unknown",
                commStatus = "DISCONNECTED",
                isEmergencyStopActive = false
            ))
        }
    }

    override fun observeActiveFaults(shuttleId: String): Flow<List<ShuttleFault>> {
        return communicationService.observeFaults(shuttleId).map { fault ->
            listOf(
                ShuttleFault(
                    faultId = fault.faultCode,
                    faultName = fault.component,
                    description = fault.description,
                    severity = when (fault.severity.uppercase()) {
                        "CRITICAL", "FAULT" -> FaultSeverity.FAULT
                        "WARNING" -> FaultSeverity.WARNING
                        else -> FaultSeverity.INFO
                    },
                    timestamp = fault.timestamp.toString(), // Replace with proper date formatting if needed
                    suggestedAction = fault.suggestedAction ?: "Contact maintenance"
                )
            )
        }
    }

    override suspend fun sendCommand(shuttleId: String, command: ShuttleCommandType): Result<Unit> {
        return try {
            communicationService.sendCommand(
                WspCommandPayload(
                    deviceId = shuttleId,
                    timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                    requestId = "REQ-${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
                    command = command.name,
                    parameters = emptyMap()
                )
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Napier.e("Failed to send command to CommunicationService", e, "CommandRepositoryImpl")
            Result.Error(e)
        }
    }

    override suspend fun clearFault(shuttleId: String, faultId: String): Result<Unit> {
        return try {
            communicationService.sendCommand(
                WspCommandPayload(
                    deviceId = shuttleId,
                    timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                    requestId = "CLR-${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
                    command = "CLEAR_FAULT",
                    parameters = mapOf("faultId" to faultId)
                )
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
