package com.example.myapplication.shared.communication.service

import com.example.myapplication.shared.communication.model.*
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CommunicationTransport {
    val connectionState: StateFlow<ConnectionState>
    val diagnosticsLog: StateFlow<List<String>>
    val lastConnectedTime: StateFlow<Long>
    val lastDisconnectedTime: StateFlow<Long>
    val lastError: StateFlow<String>
    val reconnectCount: StateFlow<Int>
    val incomingMessageCount: StateFlow<Long>
    val outgoingMessageCount: StateFlow<Long>
    
    val activeDevice: StateFlow<String?>

    suspend fun connect(settings: CommunicationSettings)
    suspend fun disconnect()
    suspend fun reconnect()

    suspend fun setActiveDevice(deviceId: String?, ipAddress: String? = null)

    suspend fun subscribeToDiscovery()
    suspend fun unsubscribeFromDiscovery()

    fun observeDiscovery(): Flow<WspInfoPayload>
    fun observeStatus(deviceId: String): Flow<WspStatusPayload>
    fun observeTelemetry(deviceId: String): Flow<WspTelemetryPayload>
    fun observeDiagnostics(deviceId: String): Flow<WspDiagnosticsPayload>
    fun observeFaults(deviceId: String): Flow<WspFaultPayload>
    fun observeHeartbeats(deviceId: String): Flow<WspHeartbeatPayload>
    fun observeResponses(deviceId: String): Flow<WspResponsePayload>

    suspend fun sendCommand(payload: WspCommandPayload)
    suspend fun sendMaintenanceRequest(payload: WspMaintenanceRequestPayload)
    suspend fun manualDiscoveryRequest()
    suspend fun pingShuttle(deviceId: String)
}
