package com.example.myapplication.shared.features.reports.repository

import com.example.myapplication.shared.features.reports.model.*
import com.example.myapplication.shared.features.shuttle.repository.ShuttleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportsRepositoryImpl(
    private val shuttleRepo: ShuttleRepository
) : ReportsRepository {

    private fun getShuttlePool(shuttleId: String?): Flow<List<Pair<String, String>>> {
        return shuttleRepo.getAllShuttles().map { shuttles ->
            val pairs = shuttles.map { it.id to it.name }
            if (shuttleId == null || shuttleId == "ALL") pairs else pairs.filter { it.first == shuttleId }
        }
    }

    override fun getSummary(shuttleId: String?, filter: ReportFilter): Flow<List<SummaryData>> {
        return getShuttlePool(shuttleId).map { pool ->
            pool.mapIndexed { i, (id, name) ->
                SummaryData(id, name, 820 + i * 120, 410 + i * 60, 410 + i * 60, 195 + i * 30,
                    82.4f - i * 2.5f, 18.5f + i * 1.2f, 3 + i, "${filter.startDate} — ${filter.endDate}")
            }
        }
    }

    override fun getStoreOperations(shuttleId: String?, filter: ReportFilter): Flow<List<OperationRecord>> {
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<OperationRecord>()
            var idx = 1
            pool.forEach { (id, name) ->
                repeat(8) { i ->
                    data.add(OperationRecord(
                        "OP-S${idx++}", id, name, "STORE", (i % 5) + 1, "R${(i % 20) + 1}-C${(i % 10) + 1}",
                        45 + i * 5, if (i == 3) "FAILED" else "COMPLETED",
                        "2026-07-${(i + 1).toString().padStart(2,'0')} 0${i}:${i * 3}:00", "OPR-01"
                    ))
                }
            }
            data
        }
    }

    override fun getRetrieveOperations(shuttleId: String?, filter: ReportFilter): Flow<List<OperationRecord>> {
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<OperationRecord>()
            var idx = 1
            pool.forEach { (id, name) ->
                repeat(8) { i ->
                    data.add(OperationRecord(
                        "OP-R${idx++}", id, name, "RETRIEVE", (i % 5) + 1, "R${(i % 20) + 1}-C${(i % 10) + 1}",
                        38 + i * 4, if (i == 5) "ABORTED" else "COMPLETED",
                        "2026-07-${(i + 1).toString().padStart(2,'0')} 1${i % 4}:${i * 5}:00", "OPR-02"
                    ))
                }
            }
            data
        }
    }

    override fun getTaskHistory(shuttleId: String?, filter: ReportFilter): Flow<List<TaskRecord>> {
        val types = listOf("STORE", "RETRIEVE", "COMPACT_PUSH", "COMPACT_PULL", "COUNT_ITEMS")
        val statuses = listOf("COMPLETED", "COMPLETED", "COMPLETED", "FAILED", "ABORTED")
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<TaskRecord>()
            var idx = 1
            pool.forEach { (id, name) ->
                repeat(10) { i ->
                    data.add(TaskRecord(
                        "TK-${idx++}", id, name, types[i % types.size],
                        if (i < 3) "HIGH" else "NORMAL", statuses[i % statuses.size],
                        "2026-07-${(i + 1).toString().padStart(2,'0')} 08:00:00",
                        "2026-07-${(i + 1).toString().padStart(2,'0')} 08:${(i * 5 + 45).toString().padStart(2,'0')}:00",
                        i * 5 + 45
                    ))
                }
            }
            data
        }
    }

    override fun getMissionHistory(shuttleId: String?, filter: ReportFilter): Flow<List<MissionRecord>> {
        val types = listOf("BATCH_STORE", "BATCH_RETRIEVE", "MAINTENANCE_PASS", "INVENTORY_SCAN")
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<MissionRecord>()
            var idx = 1
            pool.forEach { (id, name) ->
                repeat(6) { i ->
                    data.add(MissionRecord(
                        "MS-${idx++}", id, name, types[i % types.size], (i + 3) * 4,
                        if (i == 2) "FAILED" else "COMPLETED", 14.5f + i * 2.8f,
                        "2026-07-${(i + 1).toString().padStart(2,'0')} 09:00:00",
                        "2026-07-${(i + 1).toString().padStart(2,'0')} 11:${i * 10}:00",
                        if (i == 2) 2 else 0
                    ))
                }
            }
            data
        }
    }

    override fun getShuttleUtilization(shuttleId: String?, filter: ReportFilter): Flow<List<UtilizationRecord>> {
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<UtilizationRecord>()
            pool.forEach { (id, name) ->
                repeat(7) { i ->
                    val active = 6.5f + i * 0.8f
                    val idle = 17.5f - active
                    data.add(UtilizationRecord(
                        id, name, "2026-07-${(i + 22).toString().padStart(2,'0')}",
                        active, idle, (active / 24f) * 100f, 80 + i * 12, 180f + i * 22f
                    ))
                }
            }
            data
        }
    }

    override fun getBatteryReport(shuttleId: String?, filter: ReportFilter): Flow<List<BatteryRecord>> {
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<BatteryRecord>()
            var idx = 1
            pool.forEach { (id, name) ->
                repeat(8) { i ->
                    data.add(BatteryRecord(
                        "BT-${idx++}", id, name,
                        "2026-07-${(i + 22).toString().padStart(2,'0')} ${(i % 24).toString().padStart(2,'0')}:00:00",
                        (90f - i * 8f).coerceAtLeast(40f), 48.2f - i * 0.5f, 12.4f + i * 0.2f,
                        28.5f + i * 0.8f, 142 + i, if (i % 3 == 0) "CHARGING" else "DISCHARGING"
                    ))
                }
            }
            data
        }
    }

    override fun getMotorRuntime(shuttleId: String?, filter: ReportFilter): Flow<List<MotorRuntimeRecord>> {
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<MotorRuntimeRecord>()
            var idx = 1
            pool.forEach { (id, name) ->
                repeat(7) { i ->
                    data.add(MotorRuntimeRecord(
                        "MR-${idx++}", id, name, "2026-07-${(i + 22).toString().padStart(2,'0')}",
                        6.2f + i * 0.4f, 4.8f + i * 0.3f, 410 + i * 30, 195 + i * 20,
                        52.4f + i * 0.6f, 48.1f + i * 0.5f
                    ))
                }
            }
            data
        }
    }

    override fun getFaultHistory(shuttleId: String?, filter: ReportFilter): Flow<List<FaultRecord>> {
        val faultTypes = listOf("SENSOR_FAULT", "MOTOR_OVERLOAD", "COMMUNICATION_LOST", "BATTERY_LOW", "E_STOP_TRIGGERED")
        val severities = listOf("MINOR", "MAJOR", "CRITICAL", "MINOR", "MAJOR")
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<FaultRecord>()
            var idx = 1
            pool.forEach { (id, name) ->
                repeat(5) { i ->
                    data.add(FaultRecord(
                        "FT-${idx++}", id, name, "E${1000 + i}", faultTypes[i % faultTypes.size],
                        severities[i % severities.size], "Fault on ${faultTypes[i % faultTypes.size].replace('_',' ')}",
                        "2026-07-${(i + 22).toString().padStart(2,'0')} 14:${i * 10}:00",
                        if (i < 4) "2026-07-${(i + 22).toString().padStart(2,'0')} 15:${i * 10}:00" else null,
                        if (i < 4) "TECH-01" else null, if (i < 4) (i + 1) * 15 else 0
                    ))
                }
            }
            data
        }
    }

    override fun getMaintenanceHistory(shuttleId: String?, filter: ReportFilter): Flow<List<MaintenanceRecord>> {
        val types = listOf("SCHEDULED", "CORRECTIVE", "PREVENTIVE")
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<MaintenanceRecord>()
            var idx = 1
            pool.forEach { (id, name) ->
                repeat(4) { i ->
                    data.add(MaintenanceRecord(
                        "MH-${idx++}", id, name, types[i % types.size], "TECH-0${i + 1}",
                        "2026-07-${(i * 7 + 1).toString().padStart(2,'0')}",
                        90 + i * 30, if (i == 1) listOf("Drive Belt", "Sensor Module") else listOf("Filter"),
                        "Routine ${types[i % types.size].lowercase()} maintenance completed.",
                        "2026-08-${(i * 7 + 1).toString().padStart(2,'0')}"
                    ))
                }
            }
            data
        }
    }

    override fun getProductivity(shuttleId: String?, filter: ReportFilter): Flow<List<ProductivityRecord>> {
        return getShuttlePool(shuttleId).map { pool ->
            val data = mutableListOf<ProductivityRecord>()
            pool.forEach { (id, name) ->
                repeat(7) { i ->
                    val completed = 95 + i * 8
                    val failed = if (i == 3) 4 else 1
                    val avgTime = 48.5f - i * 1.2f
                    data.add(ProductivityRecord(
                        "2026-07-${(i + 22).toString().padStart(2,'0')}",
                        id, name, completed, failed, avgTime,
                        completed / (avgTime / 3600f), (completed.toFloat() / (completed + failed)) * 100f
                    ))
                }
            }
            data
        }
    }
}
