package com.example.myapplication.shared.features.settings.model

import kotlinx.serialization.Serializable

// THEME
enum class ThemeMode(val displayName: String) {
    LIGHT("Light Theme"),
    DARK("Dark Theme"),
    SYSTEM("System Default")
}

// LANGUAGE
enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English (US)", "en"),
    HINDI("Hindi (हिंदी)", "hi")
}

// FONT FAMILY
enum class AppFontFamily(val displayName: String, val fontName: String) {
    INTER("Inter (Modern Sans)", "Inter"),
    ROBOTO("Roboto (Material)", "Roboto"),
    NOTO_SANS("Noto Sans (Universal)", "NotoSans"),
    SOURCE_CODE_PRO("Source Code Pro (Mono)", "SourceCodePro"),
    OPEN_SANS("Open Sans (Readable)", "OpenSans")
}

// FONT SIZE
enum class AppFontSize(val displayName: String, val scaleFactor: Float) {
    SMALL("Small (12pt)", 0.85f),
    MEDIUM("Medium (14pt)", 1.0f),
    LARGE("Large (16pt)", 1.15f),
    EXTRA_LARGE("Extra Large (18pt)", 1.3f)
}

// MEASUREMENT UNITS
enum class MeasurementUnit(val displayName: String, val abbreviation: String) {
    METRIC("Metric (mm, m/s, kg)", "SI"),
    IMPERIAL("Imperial (in, ft/s, lb)", "IMP"),
    CUSTOM("Custom Mixed Units", "MIX")
}

// DATE FORMAT
enum class DateFormat(val displayName: String, val pattern: String) {
    ISO_8601("ISO 8601 — 2026-07-29", "yyyy-MM-dd"),
    DD_MM_YYYY("Day/Month/Year — 29/07/2026", "dd/MM/yyyy"),
    MM_DD_YYYY("Month/Day/Year — 07/29/2026", "MM/dd/yyyy"),
    DD_MON_YYYY("Verbose — 29 Jul 2026", "dd MMM yyyy")
}

// TIME FORMAT
enum class TimeFormat(val displayName: String, val pattern: String) {
    HOUR_24("24 Hour — 14:30:00", "HH:mm:ss"),
    HOUR_12("12 Hour AM/PM — 02:30 PM", "hh:mm a")
}

// REPORT EXPORT FORMAT
enum class ReportExportFormat(val displayName: String, val extension: String) {
    PDF("PDF Document (.pdf)", "pdf"),
    CSV("CSV Spreadsheet (.csv)", "csv"),
    XLSX("Excel Workbook (.xlsx)", "xlsx"),
    JSON("JSON Data Export (.json)", "json")
}

// LOG LEVEL
enum class LogLevel(val displayName: String, val description: String, val level: Int) {
    NONE("None — Logging Disabled", "No logs produced", 0),
    ERROR("Error — Critical Failures Only", "Errors and crash reports only", 1),
    WARN("Warning — Errors + Warnings", "Errors and degraded state warnings", 2),
    INFO("Info — Standard Operations", "Normal operational messages", 3),
    DEBUG("Debug — Detailed Diagnostics", "Step-by-step execution trace", 4),
    VERBOSE("Verbose — Full Telemetry", "All data including sensor readings", 5)
}

// MQTT QoS LEVEL
enum class MqttQos(val displayName: String, val level: Int) {
    AT_MOST_ONCE("QoS 0 — At Most Once (Fire & Forget)", 0),
    AT_LEAST_ONCE("QoS 1 — At Least Once (Guaranteed)", 1),
    EXACTLY_ONCE("QoS 2 — Exactly Once (Strict)", 2)
}

// BACKUP ENTRY
@Serializable
data class BackupEntry(
    val id: String,
    val timestamp: String,
    val fileName: String,
    val filePath: String,
    val sizeKb: Long,
    val status: String = "SUCCESS"
)

// SETTINGS DATA CLASSES

@Serializable
data class GeneralSettings(
    val warehouseName: String = "Central Distribution Hub 01",
    val warehouseCode: String = "WH-DEL-01",
    val companyName: String = "Mythicus Automation & Logistics Corp",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val dateFormat: DateFormat = DateFormat.ISO_8601,
    val timeFormat: TimeFormat = TimeFormat.HOUR_24,
    val defaultUnits: MeasurementUnit = MeasurementUnit.METRIC,
    val fontFamily: AppFontFamily = AppFontFamily.INTER,
    val fontSize: AppFontSize = AppFontSize.MEDIUM,
    val currentDateTimePreview: String = "2026-07-29 | 14:30 IST"
)

@Serializable
data class CommunicationSettings(
    val mqttBrokerAddress: String = "192.168.1.100",
    val mqttPort: Int = 1883,
    val clientId: String = "WMS_OPERATOR_CONSOLE_01",
    val keepAliveSeconds: Int = 60,
    val heartbeatIntervalMs: Int = 1000,
    val communicationTimeoutMs: Int = 5000,
    val autoReconnect: Boolean = true,
    val defaultQos: MqttQos = MqttQos.AT_LEAST_ONCE
)

@Serializable
data class ReportSettings(
    val defaultExportFormat: ReportExportFormat = ReportExportFormat.PDF,
    val autoReportGeneration: Boolean = true,
    val reportNamingToken1: String = "{ReportName}",
    val reportNamingToken2: String = "{Date}",
    val reportNamingToken3: String = "{Time}",
    val reportStorageLocation: String = "/var/wms/reports/export/",
    val logLevel: LogLevel = LogLevel.INFO,
    val analyticsEnabled: Boolean = true
)

@Serializable
data class BackupSettings(
    val automaticBackup: Boolean = true,
    val backupLocation: String = "/var/wms/backups/sqlite/",
    val backupHistory: List<BackupEntry> = listOf(
        BackupEntry(
            id = "BK-2026072900",
            timestamp = "2026-07-29 03:00:00",
            fileName = "wms_backup_2026-07-29_030000.db",
            filePath = "/var/wms/backups/sqlite/wms_backup_2026-07-29_030000.db",
            sizeKb = 14540L,
            status = "SUCCESS"
        ),
        BackupEntry(
            id = "BK-2026072800",
            timestamp = "2026-07-28 03:00:00",
            fileName = "wms_backup_2026-07-28_030000.db",
            filePath = "/var/wms/backups/sqlite/wms_backup_2026-07-28_030000.db",
            sizeKb = 14280L,
            status = "SUCCESS"
        )
    )
)

@Serializable
data class SystemInfo(
    val appVersion: String = "v2.4.0-industrial",
    val buildNumber: String = "2026.07.29.102",
    val targetEnvironment: String = "Linux x86_64 / Android JVM Multiplatform",
    val storageUsage: String = "Database: 14.2 MB | App Data: 120.4 MB Free",
    val companyInformation: String = "Mythicus Tech Operations Inc.\nIndustrial Automation & Robotics Division",
    val contactInformation: String = "Support Email: support@mythicus-wms.com\nHotline: +1 (800) 555-MYTH (Mon-Fri 08:00 - 18:00 EST)",
    val openSourceLicenses: List<String> = listOf(
        "Kotlin Multiplatform & Coroutines (Apache 2.0)",
        "Compose Multiplatform & Material 3 (Apache 2.0)",
        "SQLDelight SQLite Database (Apache 2.0)",
        "Koin Dependency Injection Framework (Apache 2.0)",
        "Napier Logging Library (MIT License)"
    )
)

@Serializable
data class AppSettings(
    val general: GeneralSettings = GeneralSettings(),
    val communication: CommunicationSettings = CommunicationSettings(),
    val reports: ReportSettings = ReportSettings(),
    val backup: BackupSettings = BackupSettings(),
    val systemInfo: SystemInfo = SystemInfo()
)
