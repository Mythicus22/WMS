package com.example.myapplication.shared.features.operator.repository

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.operator.model.FaultSeverity
import com.example.myapplication.shared.features.operator.model.ShuttleCommandType
import com.example.myapplication.shared.features.operator.model.ShuttleFault
import com.example.myapplication.shared.features.operator.model.ShuttleLiveStatus
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CommandRepositoryImpl : CommandRepository {

    private val liveStatusMap = mutableMapOf<String, MutableStateFlow<ShuttleLiveStatus>>()
    private val activeFaultsMap = mutableMapOf<String, MutableStateFlow<List<ShuttleFault>>>()

    override fun observeLiveStatus(shuttleId: String): Flow<ShuttleLiveStatus> {
        val flow = liveStatusMap.getOrPut(shuttleId) {
            MutableStateFlow(
                ShuttleLiveStatus(
                    shuttleId = shuttleId,
                    isOnline = true,
                    currentMission = "Standby / Ready",
                    currentState = "IDLE",
                    speed = "0.0 m/s",
                    direction = "NONE",
                    batteryPercent = 88,
                    rackPosition = "Aisle 01 / Bay 08 / Tier 02",
                    liftPosition = "DOWN",
                    commStatus = "CONNECTED (PLC/MQTT Stub)",
                    isEmergencyStopActive = false
                )
            )
        }
        return flow.asStateFlow()
    }

    override fun observeActiveFaults(shuttleId: String): Flow<List<ShuttleFault>> {
        val flow = activeFaultsMap.getOrPut(shuttleId) {
            MutableStateFlow(
                listOf(
                    ShuttleFault(
                        faultId = "F-102",
                        faultName = "Optical Distance Warning",
                        description = "Laser sensor PE-02 detected dust or slight alignment drift.",
                        severity = FaultSeverity.WARNING,
                        timestamp = "2026-07-28 14:15:00",
                        suggestedAction = "Inspect laser reflector lens on Bay 08 during next maintenance window."
                    )
                )
            )
        }
        return flow.asStateFlow()
    }

    override suspend fun sendCommand(shuttleId: String, command: ShuttleCommandType): Result<Unit> {
        Napier.d("Sending ${command.displayName} to shuttle: $shuttleId", tag = "CommandRepositoryImpl")
        
        // Simulate execution update in state flow
        val statusFlow = liveStatusMap.getOrPut(shuttleId) {
            MutableStateFlow(ShuttleLiveStatus(shuttleId = shuttleId))
        }

        when (command) {
            ShuttleCommandType.STOP -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "EMERGENCY STOP EXECUTED",
                        currentState = "HALTED",
                        speed = "0.0 m/s",
                        direction = "NONE",
                        isEmergencyStopActive = true
                    )
                }
            }
            ShuttleCommandType.AUTO_STORE -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "Auto Store [Pallet #8291]",
                        currentState = "EXECUTING_STORE",
                        speed = "1.2 m/s",
                        direction = "FORWARD",
                        isEmergencyStopActive = false
                    )
                }
            }
            ShuttleCommandType.AUTO_RETRIEVE -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "Auto Retrieve [Pallet #4012]",
                        currentState = "EXECUTING_RETRIEVE",
                        speed = "1.4 m/s",
                        direction = "REVERSE",
                        isEmergencyStopActive = false
                    )
                }
            }
            ShuttleCommandType.COMPACT_PUSH -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "Compacting Push Bay 08",
                        currentState = "COMPACTING",
                        speed = "0.6 m/s",
                        direction = "FORWARD",
                        isEmergencyStopActive = false
                    )
                }
            }
            ShuttleCommandType.COMPACT_PULL -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "Compacting Pull Bay 08",
                        currentState = "COMPACTING",
                        speed = "0.6 m/s",
                        direction = "REVERSE",
                        isEmergencyStopActive = false
                    )
                }
            }
            ShuttleCommandType.COUNT_ITEMS -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "Inventory Cycle Count",
                        currentState = "COUNTING",
                        speed = "0.4 m/s",
                        direction = "FORWARD",
                        isEmergencyStopActive = false
                    )
                }
            }
            ShuttleCommandType.MOVE_FORWARD -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "Manual Jog Forward",
                        currentState = "MANUAL_MOVE",
                        speed = "0.5 m/s",
                        direction = "FORWARD",
                        isEmergencyStopActive = false
                    )
                }
            }
            ShuttleCommandType.MOVE_REVERSE -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "Manual Jog Reverse",
                        currentState = "MANUAL_MOVE",
                        speed = "0.5 m/s",
                        direction = "REVERSE",
                        isEmergencyStopActive = false
                    )
                }
            }
            ShuttleCommandType.LIFT_UP -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "Manual Lift Up",
                        currentState = "LIFTING",
                        liftPosition = "UP",
                        isEmergencyStopActive = false
                    )
                }
            }
            ShuttleCommandType.LIFT_DOWN -> {
                statusFlow.update {
                    it.copy(
                        currentMission = "Manual Lift Down",
                        currentState = "LIFTING",
                        liftPosition = "DOWN",
                        isEmergencyStopActive = false
                    )
                }
            }
        }
        return Result.Success(Unit)
    }

    override suspend fun clearFault(shuttleId: String, faultId: String): Result<Unit> {
        val faultsFlow = activeFaultsMap[shuttleId]
        faultsFlow?.update { list -> list.filterNot { it.faultId == faultId } }
        return Result.Success(Unit)
    }
}
