package com.example.myapplication.shared.features.reports.repository

import com.example.myapplication.shared.features.reports.model.*
import com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepository
import com.example.myapplication.shared.communication.service.CommunicationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf

class ReportsRepositoryImpl(
    private val shuttleRepo: RegisteredShuttleRepository,
    private val communicationService: CommunicationService
) : ReportsRepository {

    override fun getSummary(shuttleId: String?, filter: ReportFilter): Flow<List<SummaryData>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            val summary = reports.summary
            listOf(
                SummaryData(
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    totalOperations = summary.totalStoreOperations + summary.totalRetrieveOperations,
                    totalStores = summary.totalStoreOperations,
                    totalRetrieves = summary.totalRetrieveOperations,
                    totalMissions = summary.totalTasksCompleted,
                    avgBattery = summary.avgBatteryLevel,
                    uptimeHours = 24.0f, // Need proper mapping
                    faultCount = summary.totalFaults,
                    period = "${filter.startDate} — ${filter.endDate}"
                )
            )
        }.catch { emit(emptyList()) }
    }

    override fun getStoreOperations(shuttleId: String?, filter: ReportFilter): Flow<List<OperationRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            reports.storeOperations.map { op ->
                OperationRecord(
                    id = op.id,
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    operationType = "STORE",
                    rackLevel = 1,
                    rackPosition = op.location,
                    duration = op.timeTakenSec,
                    status = op.status,
                    timestamp = op.timestamp,
                    operatorId = "SYSTEM"
                )
            }
        }.catch { emit(emptyList()) }
    }

    override fun getRetrieveOperations(shuttleId: String?, filter: ReportFilter): Flow<List<OperationRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            reports.retrieveOperations.map { op ->
                OperationRecord(
                    id = op.id,
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    operationType = "RETRIEVE",
                    rackLevel = 1,
                    rackPosition = op.location,
                    duration = op.timeTakenSec,
                    status = op.status,
                    timestamp = op.timestamp,
                    operatorId = "SYSTEM"
                )
            }
        }.catch { emit(emptyList()) }
    }

    override fun getTaskHistory(shuttleId: String?, filter: ReportFilter): Flow<List<TaskRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            reports.taskHistory.map { t ->
                TaskRecord(
                    taskId = t.id,
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    taskType = t.type,
                    priority = t.priority,
                    status = t.status,
                    createdAt = t.createdTime,
                    completedAt = t.completedTime,
                    duration = t.durationSec
                )
            }
        }.catch { emit(emptyList()) }
    }

    override fun getMissionHistory(shuttleId: String?, filter: ReportFilter): Flow<List<MissionRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            reports.missionHistory.map { m ->
                MissionRecord(
                    missionId = m.id,
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    missionType = m.type,
                    waypoints = m.totalTasks,
                    status = m.status,
                    distance = m.distanceTraveled,
                    startTime = m.startTime,
                    endTime = m.endTime,
                    faultsDuringMission = m.faultsEncountered
                )
            }
        }.catch { emit(emptyList()) }
    }

    override fun getShuttleUtilization(shuttleId: String?, filter: ReportFilter): Flow<List<UtilizationRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            reports.utilization.map { u ->
                UtilizationRecord(
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    date = u.date,
                    activeHours = u.activeHours,
                    idleHours = u.idleHours,
                    utilizationPercent = u.utilizationPercentage,
                    totalCycles = u.tasksCompleted,
                    distanceCovered = u.distanceTraveled
                )
            }
        }.catch { emit(emptyList()) }
    }

    override fun getBatteryReport(shuttleId: String?, filter: ReportFilter): Flow<List<BatteryRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            reports.batteryReport.map { b ->
                BatteryRecord(
                    recordId = b.id,
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    timestamp = b.timestamp,
                    batteryPercent = b.endPercentage,
                    voltage = b.endVoltage,
                    current = b.endCurrent,
                    temperature = b.endTemperature,
                    cycleCount = 0,
                    chargeStatus = b.status
                )
            }
        }.catch { emit(emptyList()) }
    }

    override fun getMotorRuntime(shuttleId: String?, filter: ReportFilter): Flow<List<MotorRuntimeRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            reports.motorRuntime.map { m ->
                MotorRuntimeRecord(
                    recordId = m.id,
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    date = m.date,
                    driveMotorHours = m.driveMotorHours,
                    liftMotorHours = m.liftMotorHours,
                    driveMotorCycles = m.driveMotorStarts,
                    liftMotorCycles = m.liftMotorStarts,
                    driveMotorTemp = m.avgDriveTemp,
                    liftMotorTemp = m.avgLiftTemp
                )
            }
        }.catch { emit(emptyList()) }
    }

    override fun getFaultHistory(shuttleId: String?, filter: ReportFilter): Flow<List<FaultRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            reports.faultHistory.map { f ->
                FaultRecord(
                    faultId = f.id,
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    faultCode = f.faultCode,
                    faultType = f.faultType,
                    severity = f.severity,
                    description = f.description,
                    timestamp = f.timeOccurred,
                    resolvedAt = f.timeResolved,
                    resolvedBy = f.resolvedBy,
                    downtimeMinutes = f.downtimeMin
                )
            }
        }.catch { emit(emptyList()) }
    }

    override fun getMaintenanceHistory(shuttleId: String?, filter: ReportFilter): Flow<List<MaintenanceRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            reports.maintenanceHistory.map { m ->
                MaintenanceRecord(
                    recordId = m.id,
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    maintenanceType = m.type,
                    technician = m.technician,
                    date = m.date,
                    duration = m.durationMin,
                    partsReplaced = m.partsReplaced,
                    notes = m.notes,
                    nextScheduled = m.nextScheduledDate
                )
            }
        }.catch { emit(emptyList()) }
    }

    override fun getProductivity(shuttleId: String?, filter: ReportFilter): Flow<List<ProductivityRecord>> {
        if (shuttleId == null || shuttleId == "ALL") return flowOf(emptyList())
        return communicationService.observeReports(shuttleId).map { reports ->
            val p = reports.productivity
            listOf(
                ProductivityRecord(
                    date = filter.startDate ?: "Now",
                    shuttleId = reports.deviceId,
                    shuttleName = reports.deviceId,
                    cyclesCompleted = p.completed,
                    cyclesFailed = p.failed,
                    avgCycleTimeSec = 0f, 
                    throughputPerHour = 0f,
                    efficiencyPercent = p.efficiencyPercentage
                )
            )
        }.catch { emit(emptyList()) }
    }
}
