package com.example.myapplication.shared.features.reports.repository

import com.example.myapplication.shared.features.reports.model.*
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getAnalyticsDashboard(shuttleId: String?, filter: ReportFilter): Flow<AnalyticsDashboard>
}
