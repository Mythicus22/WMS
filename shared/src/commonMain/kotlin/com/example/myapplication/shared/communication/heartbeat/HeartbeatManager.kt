package com.example.myapplication.shared.communication.heartbeat

// Heartbeat mechanism placeholder for connection health monitoring
interface HeartbeatManager {
    suspend fun start()
    suspend fun stop()
    fun isAlive(): Boolean
}

