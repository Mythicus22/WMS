package com.example.myapplication.shared.features.reports.repository

import com.example.myapplication.shared.features.reports.model.*
import io.github.aakira.napier.Napier
import java.io.File

class ExportRepositoryImpl : ExportRepository {

    override suspend fun export(config: ExportConfig, data: AllReportData): ExportResult {
        Napier.d("Starting export: ${config.format} for shuttle=${config.shuttleId}", tag = "ExportRepo")
        return try {
            val dir = File(config.outputDirectory).also { if (!it.exists()) it.mkdirs() }
            val ts  = System.currentTimeMillis().toString()
            val safeName = config.shuttleName.replace(" ", "_")
            when (config.format) {
                ExportFormat.CSV  -> exportCsv(dir, ts, safeName, config, data)
                ExportFormat.XLSX -> exportXlsx(dir, ts, safeName, config, data)
                ExportFormat.PDF  -> exportPdf(dir, ts, safeName, config, data)
            }
        } catch (e: Exception) {
            Napier.e("Export failed", e, tag = "ExportRepo")
            ExportResult(false, "", "", 0L, "Export failed: ${e.message}", "")
        }
    }

    // ── CSV → ZIP ─────────────────────────────────────────────────────────────
    private fun exportCsv(dir: File, ts: String, name: String, config: ExportConfig, data: AllReportData): ExportResult {
        val zipFile = File(dir, "WMS_Report_${name}_${ts}.zip")
        java.util.zip.ZipOutputStream(zipFile.outputStream()).use { zip ->
            fun addEntry(fileName: String, content: String) {
                zip.putNextEntry(java.util.zip.ZipEntry(fileName))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
            addEntry("summary.csv",             summaryToCsv(data.summary))
            addEntry("store_operations.csv",    operationsToCsv(data.storeOps))
            addEntry("retrieve_operations.csv", operationsToCsv(data.retrieveOps))
            addEntry("task_history.csv",        tasksToCsv(data.tasks))
            addEntry("mission_history.csv",     missionsToCsv(data.missions))
            addEntry("utilization.csv",         utilizationToCsv(data.utilization))
            addEntry("battery.csv",             batteryToCsv(data.battery))
            addEntry("motor_runtime.csv",       motorToCsv(data.motorRuntime))
            addEntry("fault_history.csv",       faultsToCsv(data.faults))
            addEntry("maintenance_history.csv", maintenanceToCsv(data.maintenance))
            addEntry("productivity.csv",        productivityToCsv(data.productivity))
        }
        return ExportResult(true, zipFile.absolutePath, zipFile.name, zipFile.length() / 1024, "CSV export completed: ${zipFile.name}", ts)
    }

    // ── XLSX (text-based tsv in .xlsx wrapper) ───────────────────────────────
    private fun exportXlsx(dir: File, ts: String, name: String, config: ExportConfig, data: AllReportData): ExportResult {
        val file = File(dir, "WMS_Report_${name}_${ts}.xlsx")
        // Write as tab-separated plain text (real XLSX needs Apache POI, not on KMP common)
        val sb = StringBuilder()
        sb.appendLine("=== WMS OPERATIONAL REPORT ===")
        sb.appendLine("Period: ${config.startDate} — ${config.endDate}")
        sb.appendLine("Shuttle: ${config.shuttleName}")
        sb.appendLine()
        sb.appendLine("--- SUMMARY ---")
        sb.appendLine(summaryToCsv(data.summary))
        sb.appendLine("--- STORE OPERATIONS ---")
        sb.appendLine(operationsToCsv(data.storeOps))
        sb.appendLine("--- RETRIEVE OPERATIONS ---")
        sb.appendLine(operationsToCsv(data.retrieveOps))
        sb.appendLine("--- TASK HISTORY ---")
        sb.appendLine(tasksToCsv(data.tasks))
        sb.appendLine("--- FAULT HISTORY ---")
        sb.appendLine(faultsToCsv(data.faults))
        sb.appendLine("--- PRODUCTIVITY ---")
        sb.appendLine(productivityToCsv(data.productivity))
        file.writeText(sb.toString())
        return ExportResult(true, file.absolutePath, file.name, file.length() / 1024, "Excel report exported: ${file.name}", ts)
    }

    // ── PDF (plain-text structured report) ───────────────────────────────────
    private fun exportPdf(dir: File, ts: String, name: String, config: ExportConfig, data: AllReportData): ExportResult {
        val file = File(dir, "WMS_Report_${name}_${ts}.pdf")
        val sb = StringBuilder()
        sb.appendLine("================================================================================")
        sb.appendLine("                    WMS OPERATIONAL REPORT — PDF")
        sb.appendLine("================================================================================")
        sb.appendLine("Report Period : ${config.startDate} to ${config.endDate}")
        sb.appendLine("Shuttle       : ${config.shuttleName} (${config.shuttleId})")
        sb.appendLine("Generated At  : $ts")
        sb.appendLine("================================================================================")
        sb.appendLine()
        fun section(title: String, content: String) {
            sb.appendLine("━━━ $title ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            sb.appendLine(content)
            sb.appendLine()
        }
        section("1. SUMMARY",              summaryToCsv(data.summary))
        section("2. STORE OPERATIONS",     operationsToCsv(data.storeOps))
        section("3. RETRIEVE OPERATIONS",  operationsToCsv(data.retrieveOps))
        section("4. TASK HISTORY",         tasksToCsv(data.tasks))
        section("5. MISSION HISTORY",      missionsToCsv(data.missions))
        section("6. SHUTTLE UTILIZATION",  utilizationToCsv(data.utilization))
        section("7. BATTERY REPORT",       batteryToCsv(data.battery))
        section("8. MOTOR RUNTIME",        motorToCsv(data.motorRuntime))
        section("9. FAULT HISTORY",        faultsToCsv(data.faults))
        section("10. MAINTENANCE HISTORY", maintenanceToCsv(data.maintenance))
        section("11. PRODUCTIVITY",        productivityToCsv(data.productivity))
        file.writeText(sb.toString())
        return ExportResult(true, file.absolutePath, file.name, file.length() / 1024, "PDF report exported: ${file.name}", ts)
    }

    // ── CSV helpers ───────────────────────────────────────────────────────────
    private fun summaryToCsv(d: List<SummaryData>) = buildString {
        appendLine("ShuttleID,ShuttleName,TotalOps,Stores,Retrieves,Missions,AvgBattery%,UptimeHrs,Faults,Period")
        d.forEach { appendLine("${it.shuttleId},${it.shuttleName},${it.totalOperations},${it.totalStores},${it.totalRetrieves},${it.totalMissions},${it.avgBattery},${it.uptimeHours},${it.faultCount},${it.period}") }
    }
    private fun operationsToCsv(d: List<OperationRecord>) = buildString {
        appendLine("ID,ShuttleID,ShuttleName,Type,Level,Position,Duration(s),Status,Timestamp,Operator")
        d.forEach { appendLine("${it.id},${it.shuttleId},${it.shuttleName},${it.operationType},${it.rackLevel},${it.rackPosition},${it.duration},${it.status},${it.timestamp},${it.operatorId}") }
    }
    private fun tasksToCsv(d: List<TaskRecord>) = buildString {
        appendLine("TaskID,ShuttleID,ShuttleName,Type,Priority,Status,CreatedAt,CompletedAt,Duration(s)")
        d.forEach { appendLine("${it.taskId},${it.shuttleId},${it.shuttleName},${it.taskType},${it.priority},${it.status},${it.createdAt},${it.completedAt?:"—"},${it.duration?:"—"}") }
    }
    private fun missionsToCsv(d: List<MissionRecord>) = buildString {
        appendLine("MissionID,ShuttleID,ShuttleName,Type,Waypoints,Status,Distance(m),Start,End,Faults")
        d.forEach { appendLine("${it.missionId},${it.shuttleId},${it.shuttleName},${it.missionType},${it.waypoints},${it.status},${it.distance},${it.startTime},${it.endTime?:"—"},${it.faultsDuringMission}") }
    }
    private fun utilizationToCsv(d: List<UtilizationRecord>) = buildString {
        appendLine("ShuttleID,ShuttleName,Date,ActiveHrs,IdleHrs,Utilization%,Cycles,Distance(m)")
        d.forEach { appendLine("${it.shuttleId},${it.shuttleName},${it.date},${it.activeHours},${it.idleHours},${it.utilizationPercent},${it.totalCycles},${it.distanceCovered}") }
    }
    private fun batteryToCsv(d: List<BatteryRecord>) = buildString {
        appendLine("RecordID,ShuttleID,ShuttleName,Timestamp,Battery%,Voltage(V),Current(A),Temp(C),Cycles,Status")
        d.forEach { appendLine("${it.recordId},${it.shuttleId},${it.shuttleName},${it.timestamp},${it.batteryPercent},${it.voltage},${it.current},${it.temperature},${it.cycleCount},${it.chargeStatus}") }
    }
    private fun motorToCsv(d: List<MotorRuntimeRecord>) = buildString {
        appendLine("RecordID,ShuttleID,ShuttleName,Date,DriveHrs,LiftHrs,DriveCycles,LiftCycles,DriveTemp,LiftTemp")
        d.forEach { appendLine("${it.recordId},${it.shuttleId},${it.shuttleName},${it.date},${it.driveMotorHours},${it.liftMotorHours},${it.driveMotorCycles},${it.liftMotorCycles},${it.driveMotorTemp},${it.liftMotorTemp}") }
    }
    private fun faultsToCsv(d: List<FaultRecord>) = buildString {
        appendLine("FaultID,ShuttleID,ShuttleName,Code,Type,Severity,Description,Timestamp,Resolved,ResolvedBy,Downtime(min)")
        d.forEach { appendLine("${it.faultId},${it.shuttleId},${it.shuttleName},${it.faultCode},${it.faultType},${it.severity},${it.description},${it.timestamp},${it.resolvedAt?:"—"},${it.resolvedBy?:"—"},${it.downtimeMinutes}") }
    }
    private fun maintenanceToCsv(d: List<MaintenanceRecord>) = buildString {
        appendLine("RecordID,ShuttleID,ShuttleName,Type,Technician,Date,Duration(min),Parts,Notes,NextScheduled")
        d.forEach { appendLine("${it.recordId},${it.shuttleId},${it.shuttleName},${it.maintenanceType},${it.technician},${it.date},${it.duration},\"${it.partsReplaced.joinToString("|")}\",${it.notes},${it.nextScheduled}") }
    }
    private fun productivityToCsv(d: List<ProductivityRecord>) = buildString {
        appendLine("Date,ShuttleID,ShuttleName,CyclesOK,CyclesFailed,AvgCycleTime(s),Throughput/hr,Efficiency%")
        d.forEach { appendLine("${it.date},${it.shuttleId},${it.shuttleName},${it.cyclesCompleted},${it.cyclesFailed},${it.avgCycleTimeSec},${it.throughputPerHour},${it.efficiencyPercent}") }
    }
}
