package com.example.myapplication.shared.features.reports.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.features.reports.model.*
import com.example.myapplication.shared.features.reports.ui.charts.*
import com.example.myapplication.shared.features.reports.viewmodel.ReportsUiState
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions

@Composable
fun AnalyticsTabScreen(state: ReportsUiState) {
    val a = state.analytics
    val loading = state.isLoadingAnalytics

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing12),
        contentPadding = PaddingValues(16.dp)
    ) {
        // ── Row 1: Battery Trend & Shuttle Utilization ─────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Battery Usage Trend", "Battery % over period", isLoading = loading, isEmpty = a.batteryTrend.series.isEmpty()) {
                        LineChart(series = a.batteryTrend.series)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Shuttle Utilization", "Active usage % per day", isLoading = loading, isEmpty = a.utilization.series.isEmpty()) {
                        LineChart(series = a.utilization.series)
                    }
                }
            }
        }

        // ── Row 2: Warehouse Throughput & Store vs Retrieve ────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Warehouse Throughput", "Cycles per hour", isLoading = loading, isEmpty = a.throughput.series.isEmpty()) {
                        BarChart(series = a.throughput.series)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Store vs Retrieve Operations", "Daily comparison", isLoading = loading, isEmpty = a.storeVsRetrieve.storePoints.isEmpty()) {
                        DualBarChart(
                            seriesA = a.storeVsRetrieve.storePoints,
                            seriesB = a.storeVsRetrieve.retrievePoints,
                            labelA = "Store", labelB = "Retrieve",
                            colorA = Color(0xFF2196F3), colorB = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }

        // ── Row 3: Fault Distribution & Fault Trend ─────────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Fault Distribution", "Breakdown by fault type", isLoading = loading, isEmpty = a.faultDistribution.slices.isEmpty()) {
                        PieChart(slices = a.faultDistribution.slices, modifier = Modifier.fillMaxWidth())
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Fault Trend", "Daily fault count", isLoading = loading, isEmpty = a.faultTrend.series.isEmpty()) {
                        BarChart(series = a.faultTrend.series)
                    }
                }
            }
        }

        // ── Row 4: Maintenance Frequency & Motor Runtime ────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Maintenance Frequency", "Events per day", isLoading = loading, isEmpty = a.maintenanceFrequency.points.isEmpty()) {
                        BarChart(series = listOf(ChartSeries("Maintenance Events", 0xFFFFC107L, a.maintenanceFrequency.points)))
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Motor Runtime", "Drive & Lift motor hours", isLoading = loading, isEmpty = a.motorRuntime.driveMotorPoints.isEmpty()) {
                        DualBarChart(
                            seriesA = a.motorRuntime.driveMotorPoints,
                            seriesB = a.motorRuntime.liftMotorPoints,
                            labelA = "Drive Motor", labelB = "Lift Motor",
                            colorA = Color(0xFF9C27B0), colorB = Color(0xFF009688)
                        )
                    }
                }
            }
        }

        // ── Row 5: Productivity Trend & Idle vs Active ──────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Productivity Trend", "Efficiency % over period", isLoading = loading, isEmpty = a.productivityTrend.series.isEmpty()) {
                        LineChart(series = a.productivityTrend.series)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Idle vs Active Time", "Hours per day comparison", isLoading = loading, isEmpty = a.idleTime.idlePoints.isEmpty()) {
                        DualBarChart(
                            seriesA = a.idleTime.activePoints,
                            seriesB = a.idleTime.idlePoints,
                            labelA = "Active Hours", labelB = "Idle Hours",
                            colorA = Color(0xFF4CAF50), colorB = Color(0xFFFF5722)
                        )
                    }
                }
            }
        }

        // ── Activity Section Header ──────────────────────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            Text("ACTIVITY HEATMAPS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        // ── Row 6: Daily & Weekly Heatmaps ───────────────────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Daily Activity", "Operations by hour of day", isLoading = loading, isEmpty = a.dailyActivity.points.isEmpty()) {
                        BarChart(series = listOf(ChartSeries("Hourly Ops", 0xFF3F51B5L, a.dailyActivity.points)), modifier = Modifier.fillMaxWidth().height(160.dp))
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Weekly Activity Pattern", "Operations by day of week", isLoading = loading, isEmpty = a.weeklyActivity.points.isEmpty()) {
                        BarChart(series = listOf(ChartSeries("Daily Ops", 0xFF00BCD4L, a.weeklyActivity.points)), modifier = Modifier.fillMaxWidth().height(160.dp))
                    }
                }
            }
        }

        // ── Row 7: Monthly Activity & Top Active Shuttles ───────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ChartCard("Monthly Activity", "Operations per month", isLoading = loading, isEmpty = a.monthlyActivity.points.isEmpty()) {
                        BarChart(series = listOf(ChartSeries("Monthly Ops", 0xFF8BC34AL, a.monthlyActivity.points)), modifier = Modifier.fillMaxWidth().height(150.dp))
                    }
                }
                if (state.shuttleId == null || state.shuttleId == "ALL") {
                    Box(modifier = Modifier.weight(1f)) {
                        ChartCard("Top Active Shuttles", "Operations share per shuttle", isLoading = loading, isEmpty = a.topActiveShuttles.slices.isEmpty()) {
                            PieChart(slices = a.topActiveShuttles.slices, modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
