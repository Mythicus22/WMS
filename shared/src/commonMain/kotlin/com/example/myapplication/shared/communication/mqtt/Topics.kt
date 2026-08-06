package com.example.myapplication.shared.communication.mqtt

object Topics {
    private const val PREFIX = "warehouse/shuttle"

    fun discoveryAll(): String = "$PREFIX/+/info"
    
    fun info(deviceId: String): String = "$PREFIX/$deviceId/info"
    fun command(deviceId: String): String = "$PREFIX/$deviceId/command"
    fun response(deviceId: String): String = "$PREFIX/$deviceId/response"
    fun status(deviceId: String): String = "$PREFIX/$deviceId/status"
    fun telemetry(deviceId: String): String = "$PREFIX/$deviceId/telemetry"
    fun diagnostics(deviceId: String): String = "$PREFIX/$deviceId/diagnostics"
    fun reports(deviceId: String): String = "$PREFIX/$deviceId/reports"
    fun maintenanceRequest(deviceId: String): String = "$PREFIX/$deviceId/maintenance/request"
    fun maintenanceResult(deviceId: String): String = "$PREFIX/$deviceId/maintenance/result"
    fun fault(deviceId: String): String = "$PREFIX/$deviceId/fault"
    fun heartbeat(deviceId: String): String = "$PREFIX/$deviceId/heartbeat"
    fun log(deviceId: String): String = "$PREFIX/$deviceId/log"
    
    // Extract device ID from a topic like warehouse/shuttle/deviceId/info
    fun extractDeviceId(topic: String): String? {
        val parts = topic.split("/")
        if (parts.size >= 4 && parts[0] == "warehouse" && parts[1] == "shuttle") {
            return parts[2]
        }
        return null
    }
}
