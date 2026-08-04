package com.example.myapplication.shared.features.operator.domain

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.operator.model.ShuttleCommandType
import com.example.myapplication.shared.features.operator.model.ShuttleFault
import com.example.myapplication.shared.features.operator.model.ShuttleLiveStatus
import com.example.myapplication.shared.features.operator.repository.CommandRepository
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.repository.DiscoveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetEnabledDiscoveredDevicesUseCase(
    private val discoveryRepository: DiscoveryRepository
) {
    operator fun invoke(): Flow<List<DiscoveredDevice>> {
        return discoveryRepository.getAllDevices().map { list ->
            list.filter { it.status == "ONLINE" }
        }
    }
}

class GetDiscoveredDeviceLiveStatusUseCase(
    private val commandRepository: CommandRepository
) {
    operator fun invoke(shuttleId: String): Flow<ShuttleLiveStatus> {
        return commandRepository.observeLiveStatus(shuttleId)
    }
}

class GetDiscoveredDeviceFaultsUseCase(
    private val commandRepository: CommandRepository
) {
    operator fun invoke(shuttleId: String): Flow<List<ShuttleFault>> {
        return commandRepository.observeActiveFaults(shuttleId)
    }
}

class SendDiscoveredDeviceCommandUseCase(
    private val commandRepository: CommandRepository
) {
    suspend operator fun invoke(shuttleId: String, command: ShuttleCommandType): Result<Unit> {
        return commandRepository.sendCommand(shuttleId, command)
    }
}
