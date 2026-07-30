package com.example.myapplication.shared.features.reports.model

import kotlinx.serialization.Serializable

enum class ExportFormat(val displayName: String, val extension: String) {
    PDF("PDF Report (.pdf)", "pdf"),
    XLSX("Excel Workbook (.xlsx)", "xlsx"),
    CSV("CSV Archive (.zip)", "zip")
}

@Serializable
data class ExportConfig(
    val format: ExportFormat,
    val shuttleId: String,
    val shuttleName: String,
    val startDate: String,
    val endDate: String,
    val outputDirectory: String = "/var/wms/reports/export/"
)

@Serializable
data class ExportResult(
    val success: Boolean,
    val filePath: String,
    val fileName: String,
    val fileSizeKb: Long,
    val message: String,
    val exportedAt: String
)
