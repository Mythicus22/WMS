package com.example.myapplication.shared.features.device.repository

import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.model.DeviceCounts
import kotlinx.coroutines.flow.Flow

interface RegisteredShuttleRepository {
    fun getAllRegisteredShuttles(): Flow<List<DiscoveredDevice>>
    fun getRegisteredShuttleCounts(): Flow<DeviceCounts>
    suspend fun getRegisteredShuttleById(id: String): DiscoveredDevice?
    
    suspend fun registerShuttle(device: DiscoveredDevice)
    suspend fun updateDeviceAliasAndNotes(deviceId: String, alias: String?, notes: String?)
    suspend fun unregisterShuttle(deviceId: String)
}
