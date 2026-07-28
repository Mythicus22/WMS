package com.example.myapplication.shared.features.shuttle.di

import com.example.myapplication.shared.features.shuttle.repository.ShuttleRepository
import com.example.myapplication.shared.features.shuttle.repository.ShuttleRepositoryImpl
import com.example.myapplication.shared.features.shuttle.viewmodel.ShuttleViewModel
import com.example.myapplication.shared.features.shuttle.viewmodel.AddEditShuttleViewModel
import org.koin.dsl.module

val shuttleModule = module {
    single<ShuttleRepository> { ShuttleRepositoryImpl(get()) }
    factory { ShuttleViewModel(get()) }
    factory { AddEditShuttleViewModel(get()) }
}
