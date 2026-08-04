package com.example.myapplication.shared.features.device.repository

import com.example.myapplication.shared.communication.service.CommunicationService
import com.example.myapplication.shared.features.device.model.DeviceCounts
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DiscoveryRepositoryImpl(
    private val communicationService: CommunicationService
) : DiscoveryRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _devices = MutableStateFlow<Map<String, DiscoveredDevice>>(emptyMap())

    init {
        scope.launch {
            communicationService.observeDiscovery().collect { info ->
                val newDevice = DiscoveredDevice(
                    deviceId = info.deviceId,
                    serialNumber = info.serialNumber,
                    displayName = info.displayName,
                    protocolVersion = info.protocolVersion,
                    firmwareVersion = info.firmwareVersion,
                    hardwareVersion = info.hardwareVersion,
                    manufacturer = info.manufacturer,
                    status = info.status,
                    lastSeenAt = info.timestamp
                )
                _devices.update { current ->
                    current + (info.deviceId to newDevice)
                }
            }
        }
    }

    override fun getAllDevices(): Flow<List<DiscoveredDevice>> {
        return _devices.map { it.values.toList().sortedBy { d -> d.nameToDisplay } }
    }

    override fun getDeviceCounts(): Flow<DeviceCounts> {
        return getAllDevices().map { list ->
            val total = list.size.toLong()
            val online = list.count { it.status.equals("ONLINE", ignoreCase = true) }.toLong()
            val offline = total - online
            DeviceCounts(total, online, offline)
        }
    }

    override suspend fun getDeviceById(id: String): DiscoveredDevice? {
        return _devices.value[id]
    }

    override suspend fun updateDeviceAliasAndNotes(deviceId: String, alias: String?, notes: String?) {
        // Discovered devices do not persist alias and notes. This belongs to RegisteredShuttleRepository.
    }

    override suspend fun forgetDevice(deviceId: String) {
        _devices.update { current -> current - deviceId }
    }

    override suspend fun requestDiscovery() {
        // Clear current discovered cache on refresh
        _devices.value = emptyMap()
        communicationService.subscribeToDiscovery()
    }
}
