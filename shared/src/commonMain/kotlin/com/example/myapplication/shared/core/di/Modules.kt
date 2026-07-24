package com.example.myapplication.shared.core.di

import com.example.myapplication.shared.features.auth.di.authModule
import com.example.myapplication.shared.features.dashboard.di.dashboardModule
import com.example.myapplication.shared.features.shuttle.di.shuttleModule
import com.example.myapplication.shared.features.operator.di.operatorModule
import com.example.myapplication.shared.features.diagnostics.di.diagnosticsModule
import com.example.myapplication.shared.features.maintenance.di.maintenanceModule
import com.example.myapplication.shared.features.reports.di.reportsModule
import com.example.myapplication.shared.features.settings.di.settingsModule
import com.example.myapplication.shared.features.usermanagement.di.userManagementModule
import org.koin.core.module.Module
import org.koin.dsl.module

// Central place to aggregate DI modules for the shared module.
val sharedModule: Module = module {
    // Include all feature modules
    includes(
        authModule,
        dashboardModule,
        shuttleModule,
        operatorModule,
        diagnosticsModule,
        maintenanceModule,
        reportsModule,
        settingsModule,
        userManagementModule
    )
}

