package com.example.myapplication.shared.features.reports.repository

import com.example.myapplication.shared.features.reports.model.*

interface ExportRepository {
    suspend fun export(config: ExportConfig, data: AllReportData): ExportResult
}

data class AllReportData(
    val summary: List<SummaryData>,
    val storeOps: List<OperationRecord>,
    val retrieveOps: List<OperationRecord>,
    val tasks: List<TaskRecord>,
    val missions: List<MissionRecord>,
    val utilization: List<UtilizationRecord>,
    val battery: List<BatteryRecord>,
    val motorRuntime: List<MotorRuntimeRecord>,
    val faults: List<FaultRecord>,
    val maintenance: List<MaintenanceRecord>,
    val productivity: List<ProductivityRecord>
)
