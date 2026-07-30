package com.example.myapplication.shared.features.reports.di

import com.example.myapplication.shared.features.reports.domain.*
import com.example.myapplication.shared.features.reports.repository.*
import com.example.myapplication.shared.features.reports.viewmodel.ReportsViewModel
import org.koin.dsl.module

val reportsModule = module {
    single<ReportsRepository>  { ReportsRepositoryImpl(get()) }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }
    single<ExportRepository>   { ExportRepositoryImpl() }

    factory { GetSummaryUseCase(get()) }
    factory { GetStoreOperationsUseCase(get()) }
    factory { GetRetrieveOperationsUseCase(get()) }
    factory { GetTaskHistoryUseCase(get()) }
    factory { GetMissionHistoryUseCase(get()) }
    factory { GetShuttleUtilizationUseCase(get()) }
    factory { GetBatteryReportUseCase(get()) }
    factory { GetMotorRuntimeUseCase(get()) }
    factory { GetFaultHistoryUseCase(get()) }
    factory { GetMaintenanceHistoryUseCase(get()) }
    factory { GetProductivityUseCase(get()) }
    factory { GetAnalyticsDashboardUseCase(get()) }
    factory { ExportReportUseCase(get(), get()) }

    // ViewModel factory with parameters (shuttleId, shuttleName injected at call site)
    factory { (shuttleId: String?, shuttleName: String) ->
        ReportsViewModel(
            shuttleId, shuttleName,
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            get(), get(), get()
        )
    }
}
