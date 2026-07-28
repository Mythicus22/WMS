package com.example.myapplication.shared.features.operator.di

import com.example.myapplication.shared.features.operator.domain.GetEnabledShuttlesUseCase
import com.example.myapplication.shared.features.operator.domain.GetShuttleFaultsUseCase
import com.example.myapplication.shared.features.operator.domain.GetShuttleLiveStatusUseCase
import com.example.myapplication.shared.features.operator.domain.SendShuttleCommandUseCase
import com.example.myapplication.shared.features.operator.repository.CommandRepository
import com.example.myapplication.shared.features.operator.repository.CommandRepositoryImpl
import com.example.myapplication.shared.features.operator.viewmodel.OperatorConsoleViewModel
import com.example.myapplication.shared.features.operator.viewmodel.OperatorViewModel
import org.koin.dsl.module

val operatorModule = module {
    single<CommandRepository> { CommandRepositoryImpl() }

    factory { GetEnabledShuttlesUseCase(get()) }
    factory { GetShuttleLiveStatusUseCase(get()) }
    factory { GetShuttleFaultsUseCase(get()) }
    factory { SendShuttleCommandUseCase(get()) }

    factory { OperatorViewModel(get()) }
    factory { OperatorConsoleViewModel(get(), get(), get(), get()) }
}
