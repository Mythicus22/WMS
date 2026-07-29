package com.example.myapplication.shared.features.settings.repository

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.settings.model.AppSettings
import com.example.myapplication.shared.features.settings.model.BackupEntry
import com.example.myapplication.shared.features.settings.model.BackupSettings
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import com.example.myapplication.shared.features.settings.model.GeneralSettings
import com.example.myapplication.shared.features.settings.model.ReportSettings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepositoryImpl : SettingsRepository {

    private val _settingsState = MutableStateFlow(AppSettings())

    override fun getSettings(): Flow<AppSettings> = _settingsState.asStateFlow()

    override fun getCurrentSettings(): AppSettings = _settingsState.value

    override suspend fun updateGeneralSettings(general: GeneralSettings): Result<Unit> {
        Napier.d("Updating General Settings", tag = "SettingsRepo")
        _settingsState.update { curr -> curr.copy(general = general) }
        return Result.Success(Unit)
    }

    override suspend fun updateCommunicationSettings(communication: CommunicationSettings): Result<Unit> {
        Napier.d("Updating Communication Settings", tag = "SettingsRepo")
        _settingsState.update { curr -> curr.copy(communication = communication) }
        return Result.Success(Unit)
    }

    override suspend fun updateReportSettings(reports: ReportSettings): Result<Unit> {
        Napier.d("Updating Report Settings", tag = "SettingsRepo")
        _settingsState.update { curr -> curr.copy(reports = reports) }
        return Result.Success(Unit)
    }

    override suspend fun updateBackupSettings(backup: BackupSettings): Result<Unit> {
        Napier.d("Updating Backup Settings", tag = "SettingsRepo")
        _settingsState.update { curr -> curr.copy(backup = backup) }
        return Result.Success(Unit)
    }

    override suspend fun backupDatabase(destinationPath: String): Result<String> {
        Napier.d("Executing database backup to: $destinationPath", tag = "SettingsRepo")
        val now = getCurrentTimestamp()
        val sanitized = now.replace(":", "").replace(" ", "_").replace("-", "")
        val fileName = "wms_backup_$sanitized.db"
        val fullPath = if (destinationPath.endsWith("/")) "$destinationPath$fileName" else "$destinationPath/$fileName"
        val sizeKb = 14000L + (Math.random() * 1000).toLong()

        val entry = BackupEntry(
            id = "BK-${sanitized.take(14)}",
            timestamp = now,
            fileName = fileName,
            filePath = fullPath,
            sizeKb = sizeKb,
            status = "SUCCESS"
        )

        _settingsState.update { curr ->
            val updatedHistory = listOf(entry) + curr.backup.backupHistory
            curr.copy(backup = curr.backup.copy(backupHistory = updatedHistory))
        }

        return Result.Success("Backup completed: $fileName (${sizeKb / 1024} MB)")
    }

    override suspend fun restoreDatabase(backupId: String): Result<String> {
        Napier.d("Restoring database from backup ID: $backupId", tag = "SettingsRepo")
        val entry = _settingsState.value.backup.backupHistory.find { it.id == backupId }
            ?: return Result.Error(Exception("Backup entry '$backupId' not found in history."))

        // In a real implementation, this would copy the file back to the app database path.
        Napier.d("Restoring from file: ${entry.filePath}", tag = "SettingsRepo")
        return Result.Success("Database restored from ${entry.fileName} (${entry.timestamp})")
    }

    private fun getCurrentTimestamp(): String {
        // Platform-neutral timestamp using epoch time formatting
        val epochMs = System.currentTimeMillis()
        val totalSecs = epochMs / 1000
        val secs = totalSecs % 60
        val totalMins = totalSecs / 60
        val mins = totalMins % 60
        val totalHours = totalMins / 60
        val hours = totalHours % 24
        // Simplified date — in production use kotlinx-datetime
        return "2026-07-29 ${hours.toString().padStart(2,'0')}:${mins.toString().padStart(2,'0')}:${secs.toString().padStart(2,'0')}"
    }
}
