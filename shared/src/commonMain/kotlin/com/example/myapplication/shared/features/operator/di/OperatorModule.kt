package com.example.myapplication.shared.features.operator.di

import com.example.myapplication.shared.features.operator.domain.GetEnabledDiscoveredDevicesUseCase
import com.example.myapplication.shared.features.operator.domain.GetDiscoveredDeviceFaultsUseCase
import com.example.myapplication.shared.features.operator.domain.GetDiscoveredDeviceLiveStatusUseCase
import com.example.myapplication.shared.features.operator.domain.SendDiscoveredDeviceCommandUseCase
import com.example.myapplication.shared.features.operator.repository.CommandRepository
import com.example.myapplication.shared.features.operator.repository.CommandRepositoryImpl
import com.example.myapplication.shared.features.operator.viewmodel.OperatorConsoleViewModel
import org.koin.dsl.module

val operatorModule = module {
    single<CommandRepository> { CommandRepositoryImpl() }

    factory { GetEnabledDiscoveredDevicesUseCase(get(), get()) }
    factory { GetDiscoveredDeviceLiveStatusUseCase(get()) }
    factory { GetDiscoveredDeviceFaultsUseCase(get()) }
    factory { SendDiscoveredDeviceCommandUseCase(get()) }

    factory { OperatorConsoleViewModel(get(), get(), get(), get()) }
}
