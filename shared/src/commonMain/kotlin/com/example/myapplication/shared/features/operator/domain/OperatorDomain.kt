package com.example.myapplication.shared.features.operator.domain

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.operator.model.ShuttleCommandType
import com.example.myapplication.shared.features.operator.model.ShuttleFault
import com.example.myapplication.shared.features.operator.model.ShuttleLiveStatus
import com.example.myapplication.shared.features.operator.repository.CommandRepository
import com.example.myapplication.shared.features.shuttle.model.Shuttle
import com.example.myapplication.shared.features.shuttle.repository.ShuttleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetEnabledShuttlesUseCase(
    private val shuttleRepository: ShuttleRepository
) {
    operator fun invoke(): Flow<List<Shuttle>> {
        return shuttleRepository.getAllShuttles().map { list ->
            list.filter { it.isEnabled }
        }
    }
}

class GetShuttleLiveStatusUseCase(
    private val commandRepository: CommandRepository
) {
    operator fun invoke(shuttleId: String): Flow<ShuttleLiveStatus> {
        return commandRepository.observeLiveStatus(shuttleId)
    }
}

class GetShuttleFaultsUseCase(
    private val commandRepository: CommandRepository
) {
    operator fun invoke(shuttleId: String): Flow<List<ShuttleFault>> {
        return commandRepository.observeActiveFaults(shuttleId)
    }
}

class SendShuttleCommandUseCase(
    private val commandRepository: CommandRepository
) {
    suspend operator fun invoke(shuttleId: String, command: ShuttleCommandType): Result<Unit> {
        return commandRepository.sendCommand(shuttleId, command)
    }
}
