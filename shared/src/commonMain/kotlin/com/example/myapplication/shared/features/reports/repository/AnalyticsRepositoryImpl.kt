package com.example.myapplication.shared.features.reports.repository

import com.example.myapplication.shared.features.reports.model.*
import com.example.myapplication.shared.features.device.repository.DiscoveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnalyticsRepositoryImpl(
    private val shuttleRepo: DiscoveryRepository
) : AnalyticsRepository {

    private val blue  = 0xFF2196F3L
    private val green = 0xFF4CAF50L
    private val amber = 0xFFFFC107L
    private val red   = 0xFFF44336L
    private val purple= 0xFF9C27B0L
    private val teal  = 0xFF009688L
    private val orange= 0xFFFF5722L
    private val indigo= 0xFF3F51B5L

    private val days7 = (22..28).map { it.toString().padStart(2,'0') }

    override fun getAnalyticsDashboard(shuttleId: String?, filter: ReportFilter): Flow<AnalyticsDashboard> {
        return shuttleRepo.getAllDevices().map { shuttles ->
            val shuttleNames = if (shuttleId == null || shuttleId == "ALL") {
                if (shuttles.isEmpty()) listOf("No Shuttles") else shuttles.map { it.nameToDisplay }
            } else {
                listOf(shuttles.find { it.deviceId == shuttleId }?.nameToDisplay ?: shuttleId)
            }

            val batteryPoints = { offset: Float ->
                days7.mapIndexed { i, d -> DataPoint("Jul $d", 90f - i * 7f + offset) }
            }
            val batterySeries = shuttleNames.mapIndexed { i, n ->
                ChartSeries(n, listOf(blue, green, amber, purple, teal, orange, indigo, red)[i % 8], batteryPoints(i * -3f))
            }

            val utilPoints = { offset: Float ->
                days7.mapIndexed { i, d -> DataPoint("Jul $d", 72f + (i % 4) * 4f + offset) }
            }
            val utilSeries = shuttleNames.mapIndexed { i, n ->
                ChartSeries(n, listOf(green, teal, indigo, orange, blue, amber, red, purple)[i % 8], utilPoints(i * 2f))
            }

            val throughputSeries = listOf(ChartSeries("Throughput (cycles/hr)", blue,
                days7.mapIndexed { i, d -> DataPoint("Jul $d", 14f + (i % 5) * 1.8f) }))

            val storePoints = days7.mapIndexed { i, d -> DataPoint("Jul $d", 38f + i * 3f) }
            val retriPoints = days7.mapIndexed { i, d -> DataPoint("Jul $d", 32f + i * 2.5f) }

            val faultSlices = listOf(
                PieSlice("Sensor Faults", 28f, red),
                PieSlice("Motor Overload", 22f, orange),
                PieSlice("Comm Lost", 18f, amber),
                PieSlice("Battery Low", 15f, purple),
                PieSlice("E-Stop", 17f, indigo)
            )

            val faultTrendSeries = listOf(ChartSeries("Faults per Day", red,
                days7.mapIndexed { i, d -> DataPoint("Jul $d", (i % 3 + 1).toFloat()) }))

            val maintPoints = days7.mapIndexed { i, d -> DataPoint("Jul $d", (i % 4).toFloat()) }

            val drivePoints = days7.mapIndexed { i, d -> DataPoint("Jul $d", 6.2f + i * 0.4f) }
            val liftPoints  = days7.mapIndexed { i, d -> DataPoint("Jul $d", 4.8f + i * 0.3f) }

            val prodSeries = listOf(ChartSeries("Efficiency %", green,
                days7.mapIndexed { i, d -> DataPoint("Jul $d", 92f + (i % 3 - 1) * 2.5f) }))

            val hourly = (0..23).map { h -> DataPoint("${h.toString().padStart(2,'0')}:00", (8..18).let {
                if (h in it) 12f + (h - 8) * 1.5f else 2f + h * 0.3f }
            ) }

            val weekly = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
                .mapIndexed { i, d -> DataPoint(d, 75f + i * 5f) }

            val monthly = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul")
                .mapIndexed { i, d -> DataPoint(d, 1800f + i * 120f) }

            val totalOpsSimulated = 100f
            val topShuttleSlices = shuttleNames.mapIndexed { i, n ->
                PieSlice(n, totalOpsSimulated / shuttleNames.size + (i * 2), listOf(blue, green, amber, purple, teal, orange, indigo, red)[i % 8])
            }

            val idlePoints   = days7.mapIndexed { i, d -> DataPoint("Jul $d", 5.5f - i * 0.3f) }
            val activePoints = days7.mapIndexed { i, d -> DataPoint("Jul $d", 18.5f + i * 0.3f) }

            AnalyticsDashboard(
                batteryTrend = BatteryTrendData(batterySeries),
                utilization  = UtilizationData(utilSeries),
                throughput   = ThroughputData(throughputSeries),
                storeVsRetrieve = StoreVsRetrieveData(storePoints, retriPoints),
                faultDistribution = FaultDistributionData(faultSlices),
                faultTrend   = FaultTrendData(faultTrendSeries),
                maintenanceFrequency = MaintenanceFrequencyData(maintPoints),
                motorRuntime = MotorRuntimeData(drivePoints, liftPoints),
                productivityTrend = ProductivityTrendData(prodSeries),
                dailyActivity = DailyActivityData(hourly),
                weeklyActivity = WeeklyActivityData(weekly),
                monthlyActivity = MonthlyActivityData(monthly),
                topActiveShuttles = TopActiveShuttlesData(topShuttleSlices),
                idleTime = IdleTimeData(idlePoints, activePoints)
            )
        }
    }
}
