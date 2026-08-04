package com.example.myapplication.shared.communication.mqtt

import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.MqttClient as PahoClient
import org.eclipse.paho.client.mqttv3.MqttMessage as PahoMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import io.github.aakira.napier.Napier

actual object MqttClientFactory {
    actual fun create(): MqttClient {
        return PahoMqttClientImpl()
    }
}

class PahoMqttClientImpl : MqttClient {
    private var client: PahoClient? = null
    
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messages = MutableSharedFlow<MqttMessage>(extraBufferCapacity = 64)

    override suspend fun connect(brokerAddress: String, port: Int, clientId: String, username: String, password: String) {
        val brokerUrl = "tcp://$brokerAddress:$port"
        val newClient = PahoClient(brokerUrl, clientId, MemoryPersistence())
        client = newClient
        val options = MqttConnectOptions().apply {
            isCleanSession = true
            if (username.isNotEmpty()) this.userName = username
            if (password.isNotEmpty()) this.password = password.toCharArray()
            connectionTimeout = 10
            keepAliveInterval = 20
        }
        try {
            newClient.connect(options)
            _isConnected.value = true
        } catch (e: Exception) {
            Napier.e("Failed to connect Paho: ${e.message}")
            throw e
        }
    }

    override suspend fun disconnect() {
        try {
            client?.let {
                if (it.isConnected) {
                    it.disconnect()
                }
            }
        } catch (e: Exception) {
            Napier.e("Disconnect error: ${e.message}")
        } finally {
            _isConnected.value = false
        }
    }

    override suspend fun publish(topic: String, payload: String, qos: Int, retained: Boolean) {
        val activeClient = client
        if (activeClient != null && activeClient.isConnected) {
            val message = PahoMessage(payload.toByteArray(Charsets.UTF_8))
            message.qos = qos
            message.isRetained = retained
            activeClient.publish(topic, message)
        } else {
            Napier.e("Cannot publish to $topic because client is disconnected.")
        }
    }

    override suspend fun subscribe(topic: String, qos: Int) {
        val activeClient = client
        if (activeClient != null && activeClient.isConnected) {
            activeClient.subscribe(topic, qos, IMqttMessageListener { recvTopic, message ->
                _messages.tryEmit(MqttMessage(recvTopic, String(message.payload, Charsets.UTF_8)))
            })
        } else {
            Napier.e("Cannot subscribe to $topic because client is disconnected.")
        }
    }

    override fun observe(topic: String): Flow<MqttMessage> {
        return _messages.filter { msg -> matchesTopic(msg.topic, topic) }
    }

    private fun matchesTopic(actualTopic: String, subscribedTopic: String): Boolean {
        val actualParts = actualTopic.split("/")
        val subParts = subscribedTopic.split("/")
        
        if (actualParts.size != subParts.size && !subscribedTopic.endsWith("#")) return false
        
        for (i in subParts.indices) {
            val subPart = subParts[i]
            if (subPart == "+") continue
            if (subPart == "#") return true
            if (i >= actualParts.size) return false
            if (actualParts[i] != subPart) return false
        }
        return true
    }
}
