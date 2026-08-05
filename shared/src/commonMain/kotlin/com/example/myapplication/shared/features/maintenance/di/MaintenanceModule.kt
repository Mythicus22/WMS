package com.example.myapplication.shared.features.maintenance.di

import com.example.myapplication.shared.features.maintenance.domain.GetEnabledShuttlesForMaintenanceUseCase
import com.example.myapplication.shared.features.maintenance.domain.GetMaintenanceTestsUseCase
import com.example.myapplication.shared.features.maintenance.domain.GetTestDetailUseCase
import com.example.myapplication.shared.features.maintenance.domain.ResetMaintenanceTestUseCase
import com.example.myapplication.shared.features.maintenance.domain.RunMaintenanceTestUseCase
import com.example.myapplication.shared.features.maintenance.repository.MaintenanceRepository
import com.example.myapplication.shared.features.maintenance.repository.MaintenanceRepositoryImpl
import com.example.myapplication.shared.features.maintenance.viewmodel.MaintenanceConsoleViewModel
import com.example.myapplication.shared.features.maintenance.viewmodel.MaintenanceTestDetailViewModel
import org.koin.dsl.module

val maintenanceModule = module {
    single<MaintenanceRepository> { MaintenanceRepositoryImpl() }

    factory { GetEnabledShuttlesForMaintenanceUseCase(get()) }
    factory { GetMaintenanceTestsUseCase(get()) }
    factory { GetTestDetailUseCase(get()) }
    factory { RunMaintenanceTestUseCase(get()) }
    factory { ResetMaintenanceTestUseCase(get()) }

    factory { MaintenanceConsoleViewModel(get(), get()) }
    factory { MaintenanceTestDetailViewModel(get(), get(), get(), get(), get()) }
}
