package com.example.myapplication.shared.communication.service

import com.example.myapplication.shared.communication.model.*
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    AUTHENTICATING,
    SUBSCRIBED,
    READY,
    ERROR
}

interface CommunicationService {
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

    // Subscriptions
    suspend fun subscribeToDiscovery()
    suspend fun unsubscribeFromDiscovery()

    fun observeDiscovery(): Flow<WspInfoPayload>
    fun observeStatus(deviceId: String): Flow<WspStatusPayload>
    fun observeTelemetry(deviceId: String): Flow<WspTelemetryPayload>
    fun observeDiagnostics(deviceId: String): Flow<WspDiagnosticsPayload>
    fun observeReports(deviceId: String): Flow<WspReportsPayload>
    fun observeFaults(deviceId: String): Flow<WspFaultPayload>
    fun observeHeartbeats(deviceId: String): Flow<WspHeartbeatPayload>
    fun observeResponses(deviceId: String): Flow<WspResponsePayload>

    // Publications
    suspend fun sendCommand(payload: WspCommandPayload)
    suspend fun sendMaintenanceRequest(payload: WspMaintenanceRequestPayload)
    suspend fun manualDiscoveryRequest()
    suspend fun pingShuttle(deviceId: String)
}
