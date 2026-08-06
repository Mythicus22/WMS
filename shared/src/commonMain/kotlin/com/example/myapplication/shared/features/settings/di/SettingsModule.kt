package com.example.myapplication.shared.features.settings.di

import com.example.myapplication.shared.features.settings.domain.BackupDatabaseUseCase
import com.example.myapplication.shared.features.settings.domain.GetSettingsUseCase
import com.example.myapplication.shared.features.settings.domain.RestoreDatabaseUseCase
import com.example.myapplication.shared.features.settings.domain.UpdateBackupSettingsUseCase
import com.example.myapplication.shared.features.settings.domain.UpdateCommunicationSettingsUseCase
import com.example.myapplication.shared.features.settings.domain.UpdateGeneralSettingsUseCase
import com.example.myapplication.shared.features.settings.domain.UpdateReportSettingsUseCase
import com.example.myapplication.shared.features.settings.repository.SettingsRepository
import com.example.myapplication.shared.features.settings.repository.SettingsRepositoryImpl
import com.example.myapplication.shared.features.settings.viewmodel.SettingsViewModel
import org.koin.dsl.module

val settingsModule = module {
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get()) }

    factory { GetSettingsUseCase(get()) }
    factory { UpdateGeneralSettingsUseCase(get()) }
    factory { UpdateCommunicationSettingsUseCase(get()) }
    factory { UpdateReportSettingsUseCase(get()) }
    factory { UpdateBackupSettingsUseCase(get()) }
    factory { BackupDatabaseUseCase(get()) }
    factory { RestoreDatabaseUseCase(get()) }

    factory { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
