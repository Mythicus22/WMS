package com.example.myapplication.shared.features.device.di

import com.example.myapplication.shared.features.device.repository.DiscoveryRepository
import com.example.myapplication.shared.features.device.repository.DiscoveryRepositoryImpl
import com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepository
import com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepositoryImpl
import com.example.myapplication.shared.features.device.viewmodel.DeviceManagementViewModel
import org.koin.dsl.module

val discoveryModule = module {
    single<RegisteredShuttleRepository> { RegisteredShuttleRepositoryImpl(get()) }
    single<DiscoveryRepository> { DiscoveryRepositoryImpl(get()) }
    factory { DeviceManagementViewModel(get(), get()) }
}
