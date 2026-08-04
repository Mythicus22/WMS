package com.example.myapplication.shared.communication.mqtt

import kotlinx.coroutines.flow.Flow

data class MqttMessage(
    val topic: String,
    val payload: String
)

interface MqttClient {
    suspend fun connect(brokerAddress: String, port: Int, clientId: String, username: String = "", password: String = "")
    suspend fun disconnect()
    suspend fun publish(topic: String, payload: String, qos: Int = 0, retained: Boolean = false)
    suspend fun subscribe(topic: String, qos: Int = 0)
    fun observe(topic: String): Flow<MqttMessage>
    val isConnected: Flow<Boolean>
}

