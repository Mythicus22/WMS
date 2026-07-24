package com.example.myapplication.shared.communication.mqtt

/**
 * Abstraction for MQTT communication.
 * No implementation provided at Phase 0 — implementations should be platform-specific.
 */
interface MqttClient {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun publish(topic: String, payload: ByteArray)
    suspend fun subscribe(topic: String)
}

