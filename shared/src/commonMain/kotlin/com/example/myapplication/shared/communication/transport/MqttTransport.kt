package com.example.myapplication.shared.communication.transport

import com.example.myapplication.shared.communication.service.ConnectionState
import com.example.myapplication.shared.communication.service.CommunicationTransport
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import com.example.myapplication.shared.communication.model.*
import com.example.myapplication.shared.communication.mqtt.MqttClient
import com.example.myapplication.shared.communication.mqtt.Topics
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MqttTransport(
    private val mqttClient: MqttClient
) : CommunicationTransport {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
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

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    init {
        scope.launch {
            mqttClient.isConnected.collect { connected ->
                if (connected) {
                    _connectionState.value = ConnectionState.CONNECTED
                    _lastConnectedTime.value = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    logEvent("Status: Connected to Broker")
                    setupSubscriptions()
                } else {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _lastDisconnectedTime.value = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    logEvent("Status: Disconnected from Broker")
                }
            }
        }
    }

    override suspend fun connect(settings: CommunicationSettings) {
        _connectionState.value = ConnectionState.CONNECTING
        logEvent("Attempting connection to ${settings.mqttBrokerAddress}:${settings.mqttPort} as ${settings.clientId}")
        try {
            mqttClient.connect(settings.mqttBrokerAddress, settings.mqttPort, settings.clientId, settings.username, settings.password)
            logEvent("Connection attempt successful")
        } catch (e: Exception) {
            Napier.e("Failed to connect MQTT: ${e.message}", e)
            _connectionState.value = ConnectionState.ERROR
            _lastError.value = e.message ?: "Connection failed"
            logEvent("Error: Connection failed - ${e.message}")
        }
    }

    override suspend fun disconnect() {
        try {
            logEvent("Disconnecting from broker...")
            mqttClient.disconnect()
            _connectionState.value = ConnectionState.DISCONNECTED
            logEvent("Disconnected.")
        } catch (e: Exception) {
            Napier.e("Failed to disconnect MQTT: ${e.message}", e)
            logEvent("Error disconnecting: ${e.message}")
        }
    }

    override suspend fun reconnect() {
        disconnect()
        _reconnectCount.update { it + 1 }
        // For reconnect, we'd ideally read from saved settings, but Settings UI uses connect directly for Test Connection.
    }

    private suspend fun setupSubscriptions() {
        _connectionState.value = ConnectionState.READY
    }

    private inline fun <reified T> observeTopicAsFlow(topicPattern: String): Flow<T> {
        return mqttClient.observe(topicPattern)
            .onEach { _incomingMessageCount.update { it + 1 } }
            .mapNotNull { message ->
                try {
                    json.decodeFromString<T>(message.payload)
                } catch (e: Exception) {
                    Napier.e("Failed to decode message for topic ${message.topic}: ${e.message}")
                    null
                }
            }
    }

    override suspend fun setActiveDevice(deviceId: String?, ipAddress: String?) {
        _activeDevice.value = deviceId
        logEvent("MQTT Active Device set to: ${deviceId ?: "None"}")
    }

    override suspend fun subscribeToDiscovery() {
        try {
            logEvent("Subscribing to discovery topics...")
            mqttClient.subscribe(Topics.discoveryAll(), qos = 1)
        } catch (e: Exception) {
            Napier.e("Failed to subscribe to discovery: ${e.message}", e)
        }
    }

    override suspend fun unsubscribeFromDiscovery() {
        try {
            logEvent("Unsubscribing from discovery topics...")
            // Not implemented in MqttClient yet, skip or implement
        } catch (e: Exception) {
            Napier.e("Failed to unsubscribe: ${e.message}", e)
        }
    }

    override fun observeDiscovery(): Flow<WspInfoPayload> {
        return observeTopicAsFlow(Topics.discoveryAll())
    }

    override fun observeStatus(deviceId: String): Flow<WspStatusPayload> {
        return observeTopicAsFlow(Topics.status(deviceId))
    }

    override fun observeTelemetry(deviceId: String): Flow<WspTelemetryPayload> {
        return observeTopicAsFlow(Topics.telemetry(deviceId))
    }

    override fun observeDiagnostics(deviceId: String): Flow<WspDiagnosticsPayload> {
        return observeTopicAsFlow(Topics.diagnostics(deviceId))
    }

    override fun observeFaults(deviceId: String): Flow<WspFaultPayload> {
        return observeTopicAsFlow(Topics.fault(deviceId))
    }

    override fun observeHeartbeats(deviceId: String): Flow<WspHeartbeatPayload> {
        return observeTopicAsFlow(Topics.heartbeat(deviceId))
    }

    override fun observeResponses(deviceId: String): Flow<WspResponsePayload> {
        return observeTopicAsFlow(Topics.response(deviceId))
    }

    // Publications
    override suspend fun sendCommand(payload: WspCommandPayload) {
        val jsonPayload = json.encodeToString(payload)
        mqttClient.publish(Topics.command(payload.deviceId), jsonPayload, qos = 1, retained = false)
        _outgoingMessageCount.update { it + 1 }
    }

    override suspend fun sendMaintenanceRequest(payload: WspMaintenanceRequestPayload) {
        val jsonPayload = json.encodeToString(payload)
        mqttClient.publish(Topics.maintenanceRequest(payload.deviceId), jsonPayload, qos = 1, retained = false)
        _outgoingMessageCount.update { it + 1 }
    }

    override suspend fun manualDiscoveryRequest() {
        // Manual discovery just publishes empty or specific payload to ping info refresh.
        // The protocol implies sending a command "PING" to all or specific if we had a server topic, 
        // but since we only have shuttle topics, we could send PING to known ones. 
        // For now, this is a placeholder as protocol says "The application sends a discovery request" but doesn't define the discovery request topic clearly.
        Napier.i("Manual discovery requested")
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
