package com.example.myapplication.shared.communication.plc

/**
 * Abstraction for PLC communication. Platform-specific implementations must be provided later.
 */
interface PlcClient {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun read(address: String): ByteArray
    suspend fun write(address: String, data: ByteArray)
}

