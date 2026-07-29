package com.example.myapplication.shared.features.settings.domain

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.settings.model.AppSettings
import com.example.myapplication.shared.features.settings.model.BackupSettings
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import com.example.myapplication.shared.features.settings.model.GeneralSettings
import com.example.myapplication.shared.features.settings.model.ReportSettings
import com.example.myapplication.shared.features.settings.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.getSettings()
}

class UpdateGeneralSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(generalSettings: GeneralSettings): Result<Unit> =
        settingsRepository.updateGeneralSettings(generalSettings)
}

class UpdateCommunicationSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(communicationSettings: CommunicationSettings): Result<Unit> =
        settingsRepository.updateCommunicationSettings(communicationSettings)
}

class UpdateReportSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(reportSettings: ReportSettings): Result<Unit> =
        settingsRepository.updateReportSettings(reportSettings)
}

class UpdateBackupSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(backupSettings: BackupSettings): Result<Unit> =
        settingsRepository.updateBackupSettings(backupSettings)
}

class BackupDatabaseUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(destinationPath: String): Result<String> =
        settingsRepository.backupDatabase(destinationPath)
}

class RestoreDatabaseUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(backupId: String): Result<String> =
        settingsRepository.restoreDatabase(backupId)
}
