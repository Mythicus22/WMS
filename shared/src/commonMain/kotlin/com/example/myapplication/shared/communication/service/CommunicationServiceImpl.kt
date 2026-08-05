package com.example.myapplication.shared.communication.service

import com.example.myapplication.shared.communication.model.*
import com.example.myapplication.shared.communication.transport.DirectTransport
import com.example.myapplication.shared.communication.transport.MqttTransport
import com.example.myapplication.shared.features.settings.model.CommunicationMode
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class CommunicationServiceImpl(
    private val mqttTransport: MqttTransport,
    private val directTransport: DirectTransport
) : CommunicationService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var currentTransport: CommunicationTransport = mqttTransport
    
    // We maintain our own state flows that mirror the current transport's state flows
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _diagnosticsLog = MutableStateFlow<List<String>>(emptyList())
    override val diagnosticsLog: StateFlow<List<String>> = _diagnosticsLog.asStateFlow()

    private val _lastConnectedTime = MutableStateFlow(0L)
    override val lastConnectedTime = _lastConnectedTime.asStateFlow()

    private val _lastDisconnectedTime = MutableStateFlow(0L)
    override val lastDisconnectedTime = _lastDisconnectedTime.asStateFlow()

    private val _lastError = MutableStateFlow("None")
    override val lastError = _lastError.asStateFlow()

    private val _reconnectCount = MutableStateFlow(0)
    override val reconnectCount = _reconnectCount.asStateFlow()

    private val _incomingMessageCount = MutableStateFlow(0L)
    override val incomingMessageCount = _incomingMessageCount.asStateFlow()

    private val _outgoingMessageCount = MutableStateFlow(0L)
    override val outgoingMessageCount = _outgoingMessageCount.asStateFlow()

    private val _activeDevice = MutableStateFlow<String?>(null)
    override val activeDevice: StateFlow<String?> = _activeDevice.asStateFlow()

    private var transportStateJob: Job? = null

    init {
        observeTransportState(currentTransport)
    }

    private fun observeTransportState(transport: CommunicationTransport) {
        transportStateJob?.cancel()
        transportStateJob = scope.launch {
            launch { transport.connectionState.collect { _connectionState.value = it } }
            launch { transport.diagnosticsLog.collect { _diagnosticsLog.value = it } }
            launch { transport.lastConnectedTime.collect { _lastConnectedTime.value = it } }
            launch { transport.lastDisconnectedTime.collect { _lastDisconnectedTime.value = it } }
            launch { transport.lastError.collect { _lastError.value = it } }
            launch { transport.reconnectCount.collect { _reconnectCount.value = it } }
            launch { transport.incomingMessageCount.collect { _incomingMessageCount.value = it } }
            launch { transport.outgoingMessageCount.collect { _outgoingMessageCount.value = it } }
            launch { transport.activeDevice.collect { _activeDevice.value = it } }
        }
    }

    override suspend fun connect(settings: CommunicationSettings) {
        Napier.i("Switching transport if needed and connecting. Mode: ${settings.mode}")
        val targetTransport = if (settings.mode == CommunicationMode.DIRECT) directTransport else mqttTransport
        
        if (targetTransport != currentTransport) {
            currentTransport.disconnect()
            currentTransport = targetTransport
            observeTransportState(currentTransport)
        }

        currentTransport.connect(settings)
    }

    override suspend fun disconnect() {
        currentTransport.disconnect()
    }

    override suspend fun reconnect() {
        currentTransport.reconnect()
    }

    override suspend fun setActiveDevice(deviceId: String?, ipAddress: String?) {
        currentTransport.setActiveDevice(deviceId, ipAddress)
    }

    override suspend fun subscribeToDiscovery() {
        currentTransport.subscribeToDiscovery()
    }

    override suspend fun unsubscribeFromDiscovery() {
        currentTransport.unsubscribeFromDiscovery()
    }

    override fun observeDiscovery(): Flow<WspInfoPayload> = currentTransport.observeDiscovery()
    override fun observeStatus(deviceId: String): Flow<WspStatusPayload> = currentTransport.observeStatus(deviceId)
    override fun observeTelemetry(deviceId: String): Flow<WspTelemetryPayload> = currentTransport.observeTelemetry(deviceId)
    override fun observeDiagnostics(deviceId: String): Flow<WspDiagnosticsPayload> = currentTransport.observeDiagnostics(deviceId)
    override fun observeFaults(deviceId: String): Flow<WspFaultPayload> = currentTransport.observeFaults(deviceId)
    override fun observeHeartbeats(deviceId: String): Flow<WspHeartbeatPayload> = currentTransport.observeHeartbeats(deviceId)
    override fun observeResponses(deviceId: String): Flow<WspResponsePayload> = currentTransport.observeResponses(deviceId)

    override suspend fun sendCommand(payload: WspCommandPayload) {
        currentTransport.sendCommand(payload)
    }

    override suspend fun sendMaintenanceRequest(payload: WspMaintenanceRequestPayload) {
        currentTransport.sendMaintenanceRequest(payload)
    }

    override suspend fun manualDiscoveryRequest() {
        currentTransport.manualDiscoveryRequest()
    }

    override suspend fun pingShuttle(deviceId: String) {
        currentTransport.pingShuttle(deviceId)
    }
}
