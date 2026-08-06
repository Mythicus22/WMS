package com.example.myapplication.shared.communication.transport

import com.example.myapplication.shared.communication.model.*
import com.example.myapplication.shared.communication.service.ConnectionState
import com.example.myapplication.shared.communication.service.CommunicationTransport
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.network.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class DirectTransport : CommunicationTransport {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
        install(WebSockets) {
            pingInterval = 10_000
        }
        engine {
            requestTimeout = 3000
        }
    }

    private var currentSettings: CommunicationSettings? = null

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

    private fun logEvent(msg: String) {
        val current = _diagnosticsLog.value.toMutableList()
        current.add(0, msg)
        if (current.size > 200) current.removeLast()
        _diagnosticsLog.value = current
    }

    private val _discoveryFlow = MutableSharedFlow<WspInfoPayload>(extraBufferCapacity = 10)
    private var discoveryJob: Job? = null

    private val _statusFlow = MutableSharedFlow<WspStatusPayload>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _telemetryFlow = MutableSharedFlow<WspTelemetryPayload>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _diagnosticsFlow = MutableSharedFlow<WspDiagnosticsPayload>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _reportsFlow = MutableSharedFlow<WspReportsPayload>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _faultsFlow = MutableSharedFlow<WspFaultPayload>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _heartbeatsFlow = MutableSharedFlow<WspHeartbeatPayload>(extraBufferCapacity = 10)
    private val _responsesFlow = MutableSharedFlow<WspResponsePayload>(extraBufferCapacity = 10)

    private var activeWebSocketSession: DefaultClientWebSocketSession? = null
    private var activeWebSocketJob: Job? = null
    private var activeIpAddress: String? = null

    override suspend fun connect(settings: CommunicationSettings) {
        currentSettings = settings
        _connectionState.value = ConnectionState.CONNECTING
        logEvent("connecting to ${settings.allowedShuttleIps}")
        
        _connectionState.value = ConnectionState.CONNECTED
        _lastConnectedTime.value = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        _connectionState.value = ConnectionState.READY
    }

    override suspend fun disconnect() {
        logEvent("DirectTransport disconnecting...")
        discoveryJob?.cancel()
        closeActiveSession()
        
        _connectionState.value = ConnectionState.DISCONNECTED
        _lastDisconnectedTime.value = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }

    override suspend fun reconnect() {
        _reconnectCount.update { it + 1 }
        val deviceId = _activeDevice.value
        val ip = activeIpAddress
        closeActiveSession()
        if (deviceId != null && ip != null) {
            setActiveDevice(deviceId, ip)
        }
    }

    private suspend fun closeActiveSession() {
        activeWebSocketJob?.cancelAndJoin()
        activeWebSocketJob = null
        try {
            activeWebSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Client disconnect"))
        } catch (e: Exception) {
            // Ignore
        }
        activeWebSocketSession = null
        _activeDevice.value = null
        activeIpAddress = null
    }

    override suspend fun setActiveDevice(deviceId: String?, ipAddress: String?) {
        closeActiveSession()
        
        if (deviceId == null || ipAddress == null) {
            logEvent("Active device cleared.")
            return
        }

        _activeDevice.value = deviceId
        activeIpAddress = ipAddress
        val settings = currentSettings ?: return

        activeWebSocketJob = scope.launch {
            logEvent("trying to connect with ip $ipAddress")
            try {
                httpClient.webSocket(method = HttpMethod.Get, host = ipAddress, port = settings.directWebSocketPort, path = "/ws") {
                    activeWebSocketSession = this
                    _connectionState.value = ConnectionState.CONNECTED
                    logEvent("trying to connect with ip $ipAddress -> connected")
                    logEvent("Active WebSocket connected for $deviceId")
                    
                    while (isActive) {
                        val frame = incoming.receive() as? Frame.Text ?: continue
                        val text = frame.readText()
                        _incomingMessageCount.update { it + 1 }
                        
                        try {
                            val msg = json.decodeFromString<WspWebSocketMessage>(text)
                            when (msg.type) {
                                "INFO" -> _discoveryFlow.emit(json.decodeFromJsonElement(msg.data))
                                "STATUS" -> _statusFlow.emit(json.decodeFromJsonElement(msg.data))
                                "TELEMETRY" -> _telemetryFlow.emit(json.decodeFromJsonElement(msg.data))
                                "DIAGNOSTICS" -> _diagnosticsFlow.emit(json.decodeFromJsonElement(msg.data))
                                "REPORTS" -> _reportsFlow.emit(json.decodeFromJsonElement(msg.data))
                                "FAULTS" -> _faultsFlow.emit(json.decodeFromJsonElement(msg.data))
                                "HEARTBEAT" -> _heartbeatsFlow.emit(json.decodeFromJsonElement(msg.data))
                                "COMMAND_RESPONSE" -> _responsesFlow.emit(json.decodeFromJsonElement(msg.data))
                                else -> logEvent("Unknown message type: ${msg.type}")
                            }
                        } catch (e: Exception) {
                            Napier.e("Failed to decode WS message: ${e.message}")
                            logEvent("Failed to decode message: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Napier.e("Active WebSocket failed for $deviceId", e)
                logEvent("trying to connect with ip $ipAddress -> failed")
                logEvent("Active WS Failed: ${e.message}")
                _connectionState.value = ConnectionState.ERROR
            } finally {
                _activeDevice.value = null
                activeWebSocketSession = null
                if (_connectionState.value == ConnectionState.CONNECTED) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
        }
    }

    override suspend fun subscribeToDiscovery() {
        manualDiscoveryRequest()
    }

    override suspend fun unsubscribeFromDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
    }

    override fun observeDiscovery(): Flow<WspInfoPayload> = _discoveryFlow.asSharedFlow()

    override fun observeStatus(deviceId: String): Flow<WspStatusPayload> = _statusFlow.asSharedFlow().filter { it.deviceId == deviceId }
    override fun observeTelemetry(deviceId: String): Flow<WspTelemetryPayload> = _telemetryFlow.asSharedFlow().filter { it.deviceId == deviceId }
    override fun observeDiagnostics(deviceId: String): Flow<WspDiagnosticsPayload> = _diagnosticsFlow.asSharedFlow().filter { it.deviceId == deviceId }
    override fun observeReports(deviceId: String): Flow<WspReportsPayload> = _reportsFlow.asSharedFlow().filter { it.deviceId == deviceId }
    override fun observeFaults(deviceId: String): Flow<WspFaultPayload> = _faultsFlow.asSharedFlow().filter { it.deviceId == deviceId }
    override fun observeHeartbeats(deviceId: String): Flow<WspHeartbeatPayload> = _heartbeatsFlow.asSharedFlow().filter { it.deviceId == deviceId }
    override fun observeResponses(deviceId: String): Flow<WspResponsePayload> = _responsesFlow.asSharedFlow().filter { it.deviceId == deviceId }

    override suspend fun sendCommand(payload: WspCommandPayload) {
        val session = activeWebSocketSession
        if (session == null) {
            logEvent("Cannot send command: No active WebSocket session")
            return
        }
        try {
            val msg = WspWebSocketMessage("COMMAND", json.encodeToJsonElement(payload))
            session.send(json.encodeToString(msg))
            _outgoingMessageCount.update { it + 1 }
        } catch (e: Exception) {
            logEvent("Failed to send command over WS: ${e.message}")
        }
    }

    override suspend fun sendMaintenanceRequest(payload: WspMaintenanceRequestPayload) {
        val session = activeWebSocketSession
        if (session == null) {
            logEvent("Cannot send maintenance request: No active WebSocket session")
            return
        }
        try {
            val msg = WspWebSocketMessage("MAINTENANCE", json.encodeToJsonElement(payload))
            session.send(json.encodeToString(msg))
            _outgoingMessageCount.update { it + 1 }
        } catch (e: Exception) {
            logEvent("Failed to send maintenance over WS: ${e.message}")
        }
    }

    override suspend fun manualDiscoveryRequest() {
        val settings = currentSettings ?: return
        if (settings.allowedShuttleIps.isEmpty()) {
            logEvent("Discovery aborted: No IPs configured.")
            return
        }
        
        discoveryJob?.cancel()
        discoveryJob = scope.launch {
            logEvent("Starting sequential WS discovery for IPs: ${settings.allowedShuttleIps}")
            
            for (ip in settings.allowedShuttleIps) {
                logEvent("trying to connect with ip $ip")
                try {
                    withTimeout(3000) {
                        httpClient.webSocket(method = HttpMethod.Get, host = ip, port = settings.directWebSocketPort, path = "/ws") {
                            logEvent("trying to connect with ip $ip -> connected")
                            val frame = incoming.receive() as? Frame.Text
                            if (frame != null) {
                                val text = frame.readText()
                                try {
                                    val msg = json.decodeFromString<WspWebSocketMessage>(text)
                                    if (msg.type == "INFO" || msg.type == "STATUS") {
                                        // Fake an info payload if we only got STATUS
                                        val deviceId = msg.data.jsonObject["deviceId"]?.jsonPrimitive?.content ?: "unknown"
                                        var info = WspInfoPayload(
                                            deviceId = deviceId,
                                            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                                            serialNumber = "WS-DIRECT-$ip",
                                            displayName = "Shuttle at $ip",
                                            protocolVersion = "1.0",
                                            firmwareVersion = "1.0",
                                            hardwareVersion = "Direct",
                                            manufacturer = "JKW",
                                            status = "ONLINE"
                                        )
                                        if (msg.type == "INFO") {
                                            info = json.decodeFromJsonElement<WspInfoPayload>(msg.data).copy(serialNumber = "WS-DIRECT-$ip")
                                        }
                                        _discoveryFlow.emit(info)
                                        logEvent("Discovered $ip successfully!")
                                    }
                                } catch (e: Exception) {
                                    logEvent("Received message from $ip but couldn't parse as shuttle: ${e.message}")
                                }
                            }
                            close(CloseReason(CloseReason.Codes.NORMAL, "Probe finished"))
                        }
                    }
                } catch (e: Exception) {
                    logEvent("trying to connect with ip $ip -> failed")
                    logEvent("Probe failed for $ip: ${e.message}")
                }
            }
            logEvent("Discovery scan complete.")
        }
    }

    override suspend fun pingShuttle(deviceId: String) {
        val payload = WspCommandPayload(
            deviceId = deviceId,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            requestId = "PING-${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
            command = "PING"
        )
        sendCommand(payload)
    }
}
