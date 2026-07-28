package com.example.myapplication.shared.features.shuttle.repository

import com.example.myapplication.shared.features.shuttle.model.Shuttle
import com.example.myapplication.shared.features.shuttle.model.ShuttleCounts
import kotlinx.coroutines.flow.Flow

interface ShuttleRepository {
    fun getAllShuttles(): Flow<List<Shuttle>>
    fun getShuttleCounts(): Flow<ShuttleCounts>
    suspend fun getShuttleById(id: String): Shuttle?
    suspend fun addShuttle(shuttle: Shuttle)
    suspend fun updateShuttle(shuttle: Shuttle)
    suspend fun updateShuttleStatus(id: String, isEnabled: Boolean)
    suspend fun deleteShuttle(id: String)
}
