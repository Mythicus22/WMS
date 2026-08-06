package com.example.myapplication.shared.features.settings.repository

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.settings.model.AppSettings
import com.example.myapplication.shared.features.settings.model.BackupSettings
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import com.example.myapplication.shared.features.settings.model.GeneralSettings
import com.example.myapplication.shared.features.settings.model.ReportSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    fun getCurrentSettings(): AppSettings
    suspend fun updateGeneralSettings(general: GeneralSettings): Result<Unit>
    suspend fun updateCommunicationSettings(communication: CommunicationSettings): Result<Unit>
    suspend fun updateReportSettings(reports: ReportSettings): Result<Unit>
    suspend fun updateBackupSettings(backup: BackupSettings): Result<Unit>
    suspend fun backupDatabase(destinationPath: String): Result<String>
    suspend fun restoreDatabase(backupId: String): Result<String>
    fun resetSettings()
}
