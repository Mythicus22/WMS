package com.example.myapplication.shared.features.reports.model

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// CHART DATA PRIMITIVES
// ---------------------------------------------------------------------------
@Serializable
data class DataPoint(
    val label: String,
    val value: Float,
    val metadata: String = "" // optional tooltip extra info
)

@Serializable
data class ChartSeries(
    val name: String,
    val color: Long, // ARGB as Long for KMP compatibility
    val points: List<DataPoint>
)

@Serializable
data class PieSlice(
    val label: String,
    val value: Float,
    val color: Long
)

// ---------------------------------------------------------------------------
// ANALYTICS DASHBOARD MODELS
// ---------------------------------------------------------------------------
@Serializable
data class BatteryTrendData(val series: List<ChartSeries>)

@Serializable
data class UtilizationData(val series: List<ChartSeries>)

@Serializable
data class ThroughputData(val series: List<ChartSeries>)

@Serializable
data class StoreVsRetrieveData(
    val storePoints: List<DataPoint>,
    val retrievePoints: List<DataPoint>
)

@Serializable
data class FaultDistributionData(val slices: List<PieSlice>)

@Serializable
data class FaultTrendData(val series: List<ChartSeries>)

@Serializable
data class MaintenanceFrequencyData(val points: List<DataPoint>)

@Serializable
data class MotorRuntimeData(
    val driveMotorPoints: List<DataPoint>,
    val liftMotorPoints: List<DataPoint>
)

@Serializable
data class ProductivityTrendData(val series: List<ChartSeries>)

@Serializable
data class DailyActivityData(val points: List<DataPoint>)

@Serializable
data class WeeklyActivityData(val points: List<DataPoint>)

@Serializable
data class MonthlyActivityData(val points: List<DataPoint>)

@Serializable
data class TopActiveShuttlesData(val slices: List<PieSlice>)

@Serializable
data class IdleTimeData(
    val idlePoints: List<DataPoint>,
    val activePoints: List<DataPoint>
)

@Serializable
data class AnalyticsDashboard(
    val batteryTrend: BatteryTrendData = BatteryTrendData(emptyList()),
    val utilization: UtilizationData = UtilizationData(emptyList()),
    val throughput: ThroughputData = ThroughputData(emptyList()),
    val storeVsRetrieve: StoreVsRetrieveData = StoreVsRetrieveData(emptyList(), emptyList()),
    val faultDistribution: FaultDistributionData = FaultDistributionData(emptyList()),
    val faultTrend: FaultTrendData = FaultTrendData(emptyList()),
    val maintenanceFrequency: MaintenanceFrequencyData = MaintenanceFrequencyData(emptyList()),
    val motorRuntime: MotorRuntimeData = MotorRuntimeData(emptyList(), emptyList()),
    val productivityTrend: ProductivityTrendData = ProductivityTrendData(emptyList()),
    val dailyActivity: DailyActivityData = DailyActivityData(emptyList()),
    val weeklyActivity: WeeklyActivityData = WeeklyActivityData(emptyList()),
    val monthlyActivity: MonthlyActivityData = MonthlyActivityData(emptyList()),
    val topActiveShuttles: TopActiveShuttlesData = TopActiveShuttlesData(emptyList()),
    val idleTime: IdleTimeData = IdleTimeData(emptyList(), emptyList())
)
