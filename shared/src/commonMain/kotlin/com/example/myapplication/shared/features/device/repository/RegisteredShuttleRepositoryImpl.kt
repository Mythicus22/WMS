package com.example.myapplication.shared.features.device.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.shared.database.AppDatabase
import com.example.myapplication.shared.features.device.model.DeviceCounts
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock

class RegisteredShuttleRepositoryImpl(
    private val database: AppDatabase
) : RegisteredShuttleRepository {

    override fun getAllRegisteredShuttles(): Flow<List<DiscoveredDevice>> = flow {
        try {
            val dbFlow = database.appDatabaseQueries.getAllRegisteredShuttles().asFlow().mapToList(Dispatchers.Default).map { list ->
                list.map { dbDevice ->
                    val localData = database.appDatabaseQueries.getDeviceAlias(dbDevice.deviceId).executeAsOneOrNull()
                    DiscoveredDevice(
                        deviceId = dbDevice.deviceId,
                        serialNumber = dbDevice.serialNumber,
                        displayName = dbDevice.displayName,
                        protocolVersion = dbDevice.protocolVersion,
                        firmwareVersion = dbDevice.firmwareVersion,
                        hardwareVersion = dbDevice.hardwareVersion,
                        manufacturer = dbDevice.manufacturer,
                        status = dbDevice.status,
                        lastSeenAt = dbDevice.lastSeenAt,
                        localAlias = localData?.alias,
                        localNotes = localData?.notes
                    )
                }.sortedBy { it.nameToDisplay }
            }
            emitAll(dbFlow)
        } catch (e: Exception) {
            Napier.e("Failed to get registered shuttles (Schema outdated?): ${e.message}")
            emit(emptyList())
        }
    }

    override fun getRegisteredShuttleCounts(): Flow<DeviceCounts> {
        return getAllRegisteredShuttles().map { list ->
            val total = list.size.toLong()
            val online = list.count { it.status.equals("ONLINE", ignoreCase = true) }.toLong()
            val offline = total - online
            DeviceCounts(total, online, offline)
        }
    }

    override suspend fun getRegisteredShuttleById(id: String): DiscoveredDevice? {
        val dbDevice = database.appDatabaseQueries.getAllRegisteredShuttles().executeAsList().find { it.deviceId == id } ?: return null
        val localData = database.appDatabaseQueries.getDeviceAlias(id).executeAsOneOrNull()
        return DiscoveredDevice(
            deviceId = dbDevice.deviceId,
            serialNumber = dbDevice.serialNumber,
            displayName = dbDevice.displayName,
            protocolVersion = dbDevice.protocolVersion,
            firmwareVersion = dbDevice.firmwareVersion,
            hardwareVersion = dbDevice.hardwareVersion,
            manufacturer = dbDevice.manufacturer,
            status = dbDevice.status,
            lastSeenAt = dbDevice.lastSeenAt,
            localAlias = localData?.alias,
            localNotes = localData?.notes
        )
    }

    override suspend fun registerShuttle(device: DiscoveredDevice) {
        try {
            database.appDatabaseQueries.insertRegisteredShuttle(
                deviceId = device.deviceId,
                serialNumber = device.serialNumber,
                displayName = device.displayName,
                protocolVersion = device.protocolVersion,
                firmwareVersion = device.firmwareVersion,
                hardwareVersion = device.hardwareVersion,
                manufacturer = device.manufacturer,
                status = device.status,
                lastSeenAt = device.lastSeenAt
            )
        } catch (e: Exception) {
            Napier.e("Failed to register shuttle (Schema outdated?): ${e.message}")
            throw Exception("Failed to register shuttle: Please clear app data / reinstall to update schema.")
        }
    }

    override suspend fun updateDeviceAliasAndNotes(deviceId: String, alias: String?, notes: String?) {
        database.appDatabaseQueries.insertDeviceAlias(
            deviceId = deviceId,
            alias = alias,
            notes = notes,
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun unregisterShuttle(deviceId: String) {
        database.appDatabaseQueries.deleteRegisteredShuttle(deviceId)
        database.appDatabaseQueries.deleteDeviceAlias(deviceId)
    }
}
