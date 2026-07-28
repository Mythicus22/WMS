package com.example.myapplication

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import org.koin.core.context.startKoin
import org.koin.dsl.module
import com.example.myapplication.shared.core.di.sharedModule
import com.example.myapplication.shared.data.local.database.DriverFactory
import com.example.myapplication.shared.core.logging.Logger

fun main() = application {
    // Initialize shared logging
    Logger.init()

    // Start Koin with shared DI modules and Desktop-specific DriverFactory
    startKoin {
        modules(sharedModule, module {
            single { DriverFactory() }
        })
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "MyApplication",
    ) {
        App()
    }
}