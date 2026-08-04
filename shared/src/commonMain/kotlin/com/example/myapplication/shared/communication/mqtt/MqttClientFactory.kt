package com.example.myapplication.shared.communication.mqtt

expect object MqttClientFactory {
    fun create(): MqttClient
}
