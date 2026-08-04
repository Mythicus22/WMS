package com.example.myapplication.shared.core.di

import com.example.myapplication.shared.database.AppDatabase
import com.example.myapplication.shared.data.local.database.DriverFactory
import com.example.myapplication.shared.core.session.SessionManager
import com.example.myapplication.shared.data.repository.UserRepositoryImpl
import com.example.myapplication.shared.domain.repository.IUserRepository
import com.example.myapplication.shared.domain.usecase.GetCurrentUserUseCase
import com.example.myapplication.shared.domain.usecase.LoginUseCase
import com.example.myapplication.shared.domain.usecase.LogoutUseCase
import com.example.myapplication.shared.domain.usecase.ManageUsersUseCase
import com.example.myapplication.shared.features.auth.di.authModule
import com.example.myapplication.shared.features.auth.viewmodel.AuthViewModel
import com.example.myapplication.shared.features.dashboard.di.dashboardModule
import com.example.myapplication.shared.features.dashboard.viewmodel.DashboardViewModel
import com.example.myapplication.shared.features.diagnostics.di.diagnosticsModule
import com.example.myapplication.shared.features.maintenance.di.maintenanceModule
import com.example.myapplication.shared.features.operator.di.operatorModule
import com.example.myapplication.shared.features.reports.di.reportsModule
import com.example.myapplication.shared.features.settings.di.settingsModule
import com.example.myapplication.shared.features.settings.viewmodel.SettingsViewModel
import com.example.myapplication.shared.features.device.di.discoveryModule
import com.example.myapplication.shared.features.usermanagement.di.userManagementModule
import com.example.myapplication.shared.features.usermanagement.viewmodel.UserManagementViewModel
import com.example.myapplication.shared.communication.di.communicationModule
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule = module {
    single { AppDatabase(get<DriverFactory>().createDriver()) }
    single { SessionManager() }
    single<IUserRepository> { UserRepositoryImpl(get()) }

    // UseCases
    factory { LoginUseCase(get(), get()) }
    factory { LogoutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { ManageUsersUseCase(get(), get()) }

    // ViewModels
    factory { AuthViewModel(get()) }
    factory { DashboardViewModel(get(), get(), get()) }
    factory { UserManagementViewModel(get()) }
}

// Central place to aggregate DI modules for the shared module.
val sharedModule: Module = module {
    includes(
        coreModule,
        authModule,
        dashboardModule,
        discoveryModule,
        operatorModule,
        diagnosticsModule,
        maintenanceModule,
        reportsModule,
        settingsModule,
        userManagementModule,
        communicationModule
    )
}
