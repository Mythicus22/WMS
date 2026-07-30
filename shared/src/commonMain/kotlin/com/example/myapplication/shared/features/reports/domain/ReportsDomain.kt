package com.example.myapplication.shared.features.reports.domain

import com.example.myapplication.shared.features.reports.model.*
import com.example.myapplication.shared.features.reports.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

// ── Reports Use Cases ──────────────────────────────────────────────────────
class GetSummaryUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<SummaryData>> = repo.getSummary(shuttleId, filter)
}
class GetStoreOperationsUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<OperationRecord>> = repo.getStoreOperations(shuttleId, filter)
}
class GetRetrieveOperationsUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<OperationRecord>> = repo.getRetrieveOperations(shuttleId, filter)
}
class GetTaskHistoryUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<TaskRecord>> = repo.getTaskHistory(shuttleId, filter)
}
class GetMissionHistoryUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<MissionRecord>> = repo.getMissionHistory(shuttleId, filter)
}
class GetShuttleUtilizationUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<UtilizationRecord>> = repo.getShuttleUtilization(shuttleId, filter)
}
class GetBatteryReportUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<BatteryRecord>> = repo.getBatteryReport(shuttleId, filter)
}
class GetMotorRuntimeUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<MotorRuntimeRecord>> = repo.getMotorRuntime(shuttleId, filter)
}
class GetFaultHistoryUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<FaultRecord>> = repo.getFaultHistory(shuttleId, filter)
}
class GetMaintenanceHistoryUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<MaintenanceRecord>> = repo.getMaintenanceHistory(shuttleId, filter)
}
class GetProductivityUseCase(private val repo: ReportsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<List<ProductivityRecord>> = repo.getProductivity(shuttleId, filter)
}

// ── Analytics Use Case ─────────────────────────────────────────────────────
class GetAnalyticsDashboardUseCase(private val repo: AnalyticsRepository) {
    operator fun invoke(shuttleId: String?, filter: ReportFilter): Flow<AnalyticsDashboard> = repo.getAnalyticsDashboard(shuttleId, filter)
}

// ── Export Use Case ────────────────────────────────────────────────────────
class ExportReportUseCase(
    private val exportRepo: ExportRepository,
    private val reportsRepo: ReportsRepository
) {
    suspend operator fun invoke(config: ExportConfig, filter: ReportFilter): ExportResult {
        val sid = config.shuttleId.takeIf { it != "ALL" }
        val allData = AllReportData(
            summary      = reportsRepo.getSummary(sid, filter).first(),
            storeOps     = reportsRepo.getStoreOperations(sid, filter).first(),
            retrieveOps  = reportsRepo.getRetrieveOperations(sid, filter).first(),
            tasks        = reportsRepo.getTaskHistory(sid, filter).first(),
            missions     = reportsRepo.getMissionHistory(sid, filter).first(),
            utilization  = reportsRepo.getShuttleUtilization(sid, filter).first(),
            battery      = reportsRepo.getBatteryReport(sid, filter).first(),
            motorRuntime = reportsRepo.getMotorRuntime(sid, filter).first(),
            faults       = reportsRepo.getFaultHistory(sid, filter).first(),
            maintenance  = reportsRepo.getMaintenanceHistory(sid, filter).first(),
            productivity = reportsRepo.getProductivity(sid, filter).first()
        )
        return exportRepo.export(config, allData)
    }
}
