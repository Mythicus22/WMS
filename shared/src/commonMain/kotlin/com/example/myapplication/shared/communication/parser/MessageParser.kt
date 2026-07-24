package com.example.myapplication.shared.communication.parser

// Message parser placeholder for MQTT/PLC messages
interface MessageParser<T> {
    fun parse(raw: String): T?
    fun serialize(message: T): String
}

