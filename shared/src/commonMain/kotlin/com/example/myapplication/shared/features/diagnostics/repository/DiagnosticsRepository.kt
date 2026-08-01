package com.example.myapplication.shared.features.diagnostics.repository

import com.example.myapplication.shared.features.diagnostics.model.DiagnosticsData
import kotlinx.coroutines.flow.Flow

interface DiagnosticsRepository {
    fun getDiagnostics(shuttleId: String): Flow<DiagnosticsData>
}
