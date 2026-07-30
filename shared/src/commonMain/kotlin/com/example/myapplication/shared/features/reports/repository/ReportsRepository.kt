package com.example.myapplication.shared.features.reports.repository

import com.example.myapplication.shared.features.reports.model.*
import kotlinx.coroutines.flow.Flow

interface ReportsRepository {
    // Pass null shuttleId to get aggregated data for ALL shuttles
    fun getSummary(shuttleId: String?, filter: ReportFilter): Flow<List<SummaryData>>
    fun getStoreOperations(shuttleId: String?, filter: ReportFilter): Flow<List<OperationRecord>>
    fun getRetrieveOperations(shuttleId: String?, filter: ReportFilter): Flow<List<OperationRecord>>
    fun getTaskHistory(shuttleId: String?, filter: ReportFilter): Flow<List<TaskRecord>>
    fun getMissionHistory(shuttleId: String?, filter: ReportFilter): Flow<List<MissionRecord>>
    fun getShuttleUtilization(shuttleId: String?, filter: ReportFilter): Flow<List<UtilizationRecord>>
    fun getBatteryReport(shuttleId: String?, filter: ReportFilter): Flow<List<BatteryRecord>>
    fun getMotorRuntime(shuttleId: String?, filter: ReportFilter): Flow<List<MotorRuntimeRecord>>
    fun getFaultHistory(shuttleId: String?, filter: ReportFilter): Flow<List<FaultRecord>>
    fun getMaintenanceHistory(shuttleId: String?, filter: ReportFilter): Flow<List<MaintenanceRecord>>
    fun getProductivity(shuttleId: String?, filter: ReportFilter): Flow<List<ProductivityRecord>>
}
