package com.example.myapplication.shared.features.operator.repository

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.operator.model.ShuttleCommandType
import com.example.myapplication.shared.features.operator.model.ShuttleFault
import com.example.myapplication.shared.features.operator.model.ShuttleLiveStatus
import kotlinx.coroutines.flow.Flow

interface CommandRepository {
    fun observeLiveStatus(shuttleId: String): Flow<ShuttleLiveStatus>
    fun observeActiveFaults(shuttleId: String): Flow<List<ShuttleFault>>

    suspend fun sendCommand(shuttleId: String, command: ShuttleCommandType): Result<Unit>
    suspend fun clearFault(shuttleId: String, faultId: String): Result<Unit>
}
