package com.example.myapplication.shared.features.device.repository

import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.model.DeviceCounts
import kotlinx.coroutines.flow.Flow

interface DiscoveryRepository {
    fun getAllDevices(): Flow<List<DiscoveredDevice>>
    fun getDeviceCounts(): Flow<DeviceCounts>
    suspend fun getDeviceById(id: String): DiscoveredDevice?
    
    // Only local fields can be edited manually
    suspend fun updateDeviceAliasAndNotes(deviceId: String, alias: String?, notes: String?)
    
    // Manually drop a device from the list
    suspend fun forgetDevice(deviceId: String)

    suspend fun requestDiscovery()
}
