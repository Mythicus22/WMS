package com.example.myapplication.shared.features.reports.model

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// FILTER STATE
// ---------------------------------------------------------------------------
data class ReportFilter(
    val startDate: String = "2026-07-01",
    val endDate: String = "2026-07-30",
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST
)

enum class SortOrder(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    ASCENDING("Ascending"),
    DESCENDING("Descending")
}

// ---------------------------------------------------------------------------
// REPORT CATEGORY
// ---------------------------------------------------------------------------
enum class ReportCategory(val displayName: String, val icon: String) {
    SUMMARY("Summary", "dashboard"),
    STORE_OPERATIONS("Store Operations", "archive"),
    RETRIEVE_OPERATIONS("Retrieve Operations", "unarchive"),
    TASK_HISTORY("Task History", "task"),
    MISSION_HISTORY("Mission History", "route"),
    SHUTTLE_UTILIZATION("Shuttle Utilization", "local_shipping"),
    BATTERY("Battery Report", "battery_full"),
    MOTOR_RUNTIME("Motor Runtime", "settings"),
    FAULT_HISTORY("Fault History", "warning"),
    MAINTENANCE_HISTORY("Maintenance History", "build"),
    PRODUCTIVITY("Productivity", "speed")
}

// ---------------------------------------------------------------------------
// SUMMARY ROW
// ---------------------------------------------------------------------------
@Serializable
data class SummaryData(
    val shuttleId: String,
    val shuttleName: String,
    val totalOperations: Int,
    val totalStores: Int,
    val totalRetrieves: Int,
    val totalMissions: Int,
    val avgBattery: Float,
    val uptimeHours: Float,
    val faultCount: Int,
    val period: String
)

// ---------------------------------------------------------------------------
// STORE / RETRIEVE OPERATION ROW
// ---------------------------------------------------------------------------
@Serializable
data class OperationRecord(
    val id: String,
    val shuttleId: String,
    val shuttleName: String,
    val operationType: String, // "STORE" | "RETRIEVE"
    val rackLevel: Int,
    val rackPosition: String,
    val duration: Int, // seconds
    val status: String, // "COMPLETED" | "FAILED" | "ABORTED"
    val timestamp: String,
    val operatorId: String
)

// ---------------------------------------------------------------------------
// TASK HISTORY ROW
// ---------------------------------------------------------------------------
@Serializable
data class TaskRecord(
    val taskId: String,
    val shuttleId: String,
    val shuttleName: String,
    val taskType: String,
    val priority: String, // "HIGH" | "NORMAL" | "LOW"
    val status: String,
    val createdAt: String,
    val completedAt: String?,
    val duration: Int? // seconds
)

// ---------------------------------------------------------------------------
// MISSION HISTORY ROW
// ---------------------------------------------------------------------------
@Serializable
data class MissionRecord(
    val missionId: String,
    val shuttleId: String,
    val shuttleName: String,
    val missionType: String,
    val waypoints: Int,
    val status: String,
    val distance: Float, // metres
    val startTime: String,
    val endTime: String?,
    val faultsDuringMission: Int
)

// ---------------------------------------------------------------------------
// UTILIZATION ROW
// ---------------------------------------------------------------------------
@Serializable
data class UtilizationRecord(
    val shuttleId: String,
    val shuttleName: String,
    val date: String,
    val activeHours: Float,
    val idleHours: Float,
    val utilizationPercent: Float,
    val totalCycles: Int,
    val distanceCovered: Float
)

// ---------------------------------------------------------------------------
// BATTERY REPORT ROW
// ---------------------------------------------------------------------------
@Serializable
data class BatteryRecord(
    val recordId: String,
    val shuttleId: String,
    val shuttleName: String,
    val timestamp: String,
    val batteryPercent: Float,
    val voltage: Float,
    val current: Float,
    val temperature: Float,
    val cycleCount: Int,
    val chargeStatus: String
)

// ---------------------------------------------------------------------------
// MOTOR RUNTIME ROW
// ---------------------------------------------------------------------------
@Serializable
data class MotorRuntimeRecord(
    val recordId: String,
    val shuttleId: String,
    val shuttleName: String,
    val date: String,
    val driveMotorHours: Float,
    val liftMotorHours: Float,
    val driveMotorCycles: Int,
    val liftMotorCycles: Int,
    val driveMotorTemp: Float,
    val liftMotorTemp: Float
)

// ---------------------------------------------------------------------------
// FAULT HISTORY ROW
// ---------------------------------------------------------------------------
@Serializable
data class FaultRecord(
    val faultId: String,
    val shuttleId: String,
    val shuttleName: String,
    val faultCode: String,
    val faultType: String,
    val severity: String, // "CRITICAL" | "MAJOR" | "MINOR"
    val description: String,
    val timestamp: String,
    val resolvedAt: String?,
    val resolvedBy: String?,
    val downtimeMinutes: Int
)

// ---------------------------------------------------------------------------
// MAINTENANCE HISTORY ROW
// ---------------------------------------------------------------------------
@Serializable
data class MaintenanceRecord(
    val recordId: String,
    val shuttleId: String,
    val shuttleName: String,
    val maintenanceType: String, // "SCHEDULED" | "CORRECTIVE" | "PREVENTIVE"
    val technician: String,
    val date: String,
    val duration: Int, // minutes
    val partsReplaced: List<String>,
    val notes: String,
    val nextScheduled: String
)

// ---------------------------------------------------------------------------
// PRODUCTIVITY ROW
// ---------------------------------------------------------------------------
@Serializable
data class ProductivityRecord(
    val date: String,
    val shuttleId: String,
    val shuttleName: String,
    val cyclesCompleted: Int,
    val cyclesFailed: Int,
    val avgCycleTimeSec: Float,
    val throughputPerHour: Float,
    val efficiencyPercent: Float
)
