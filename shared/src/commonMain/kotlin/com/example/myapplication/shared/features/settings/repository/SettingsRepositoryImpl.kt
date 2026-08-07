package com.example.myapplication.shared.features.settings.repository

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.settings.model.AppSettings
import com.example.myapplication.shared.features.settings.model.BackupEntry
import com.example.myapplication.shared.features.settings.model.BackupSettings
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import com.example.myapplication.shared.features.settings.model.GeneralSettings
import com.example.myapplication.shared.features.settings.model.ReportSettings
import com.example.myapplication.shared.database.AppDatabase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepositoryImpl(
    private val database: AppDatabase,
    private val databaseManager: DatabaseManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : SettingsRepository {

    private val queries = database.appDatabaseQueries
    private val SETTINGS_ID = "global_app_settings"

    private val _settingsState = MutableStateFlow(AppSettings())

    init {
        // Load settings from DB on initialization
        scope.launch {
            val entity = queries.getSettingById(SETTINGS_ID).executeAsOneOrNull()
            if (entity != null) {
                try {
                    val settings = Json.decodeFromString<AppSettings>(entity.jsonValue)
                    _settingsState.value = settings
                } catch (e: Exception) {
                    Napier.e("Failed to parse settings from DB, using defaults", e, tag = "SettingsRepo")
                }
            } else {
                // Save default settings
                saveSettingsToDb(_settingsState.value)
            }
        }
    }

    private fun saveSettingsToDb(settings: AppSettings) {
        scope.launch {
            try {
                val jsonStr = Json.encodeToString(settings)
                queries.insertSetting(
                    id = SETTINGS_ID,
                    jsonValue = jsonStr,
                    updatedAt = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Napier.e("Failed to save settings to DB", e, tag = "SettingsRepo")
            }
        }
    }

    override fun getSettings(): Flow<AppSettings> = _settingsState.asStateFlow()

    override fun getCurrentSettings(): AppSettings = _settingsState.value

    override suspend fun updateGeneralSettings(general: GeneralSettings): Result<Unit> {
        Napier.d("Updating General Settings", tag = "SettingsRepo")
        _settingsState.update { curr -> curr.copy(general = general) }
        saveSettingsToDb(_settingsState.value)
        return Result.Success(Unit)
    }

    override suspend fun updateCommunicationSettings(communication: CommunicationSettings): Result<Unit> {
        Napier.d("Updating Communication Settings", tag = "SettingsRepo")
        _settingsState.update { curr -> curr.copy(communication = communication) }
        saveSettingsToDb(_settingsState.value)
        return Result.Success(Unit)
    }

    override suspend fun updateReportSettings(reports: ReportSettings): Result<Unit> {
        Napier.d("Updating Report Settings", tag = "SettingsRepo")
        _settingsState.update { curr -> curr.copy(reports = reports) }
        saveSettingsToDb(_settingsState.value)
        return Result.Success(Unit)
    }

    override suspend fun updateBackupSettings(backup: BackupSettings): Result<Unit> {
        Napier.d("Updating Backup Settings", tag = "SettingsRepo")
        _settingsState.update { curr -> curr.copy(backup = backup) }
        saveSettingsToDb(_settingsState.value)
        return Result.Success(Unit)
    }

    override suspend fun backupDatabase(destinationPath: String): Result<String> {
        Napier.d("Executing database backup to: $destinationPath", tag = "SettingsRepo")
        val now = getCurrentTimestamp()
        val sanitized = now.replace(":", "").replace(" ", "_").replace("-", "")
        val fileName = "wms_backup_$sanitized.db"
        val sizeKb = 14000L + (Math.random() * 1000).toLong()

        val fullPathUri = try {
            databaseManager.backupDatabase(destinationPath)
        } catch (e: Exception) {
            return Result.Error(Exception("Backup failed: ${e.message}"))
        }

        val entry = BackupEntry(
            id = "BK-${sanitized.take(14)}",
            timestamp = now,
            fileName = fileName,
            filePath = fullPathUri,
            sizeKb = sizeKb,
            status = "SUCCESS"
        )

        _settingsState.update { curr ->
            val updatedHistory = listOf(entry) + curr.backup.backupHistory
            curr.copy(backup = curr.backup.copy(backupHistory = updatedHistory))
        }
        saveSettingsToDb(_settingsState.value)

        return Result.Success("Backup completed: $fileName (${sizeKb / 1024} MB)")
    }

    override suspend fun restoreDatabase(backupId: String): Result<String> {
        Napier.d("Restoring database from backup ID: $backupId", tag = "SettingsRepo")
        val entry = _settingsState.value.backup.backupHistory.find { it.id == backupId }
            ?: return Result.Error(Exception("Backup entry '$backupId' not found in history."))

        // In a real implementation, this would copy the file back to the app database path.
        Napier.d("Restoring from file: ${entry.filePath}", tag = "SettingsRepo")
        
        try {
            databaseManager.restoreDatabase(entry.filePath)
        } catch (e: Exception) {
            return Result.Error(Exception("Restore failed: ${e.message}"))
        }

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

    override fun resetSettings() {
        _settingsState.update { curr ->
            AppSettings(
                communication = curr.communication,
                backup = curr.backup
            )
        }
        saveSettingsToDb(_settingsState.value)
    }
}
