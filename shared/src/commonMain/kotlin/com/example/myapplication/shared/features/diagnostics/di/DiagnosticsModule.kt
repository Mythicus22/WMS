package com.example.myapplication.shared.features.diagnostics.di

import com.example.myapplication.shared.features.diagnostics.repository.DiagnosticsRepository
import com.example.myapplication.shared.features.diagnostics.repository.DiagnosticsRepositoryImpl
import com.example.myapplication.shared.features.diagnostics.viewmodel.DiagnosticsDashboardViewModel
import org.koin.dsl.module

val diagnosticsModule = module {
    single<DiagnosticsRepository> { DiagnosticsRepositoryImpl(get()) }
    
    factory { (shuttleId: String, shuttleName: String) ->
        DiagnosticsDashboardViewModel(
            shuttleId = shuttleId,
            shuttleName = shuttleName,
            repository = get()
        )
    }
}
