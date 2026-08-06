package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.example.myapplication.shared.core.di.sharedModule
import com.example.myapplication.shared.data.local.database.DriverFactory
import com.example.myapplication.shared.core.logging.Logger
import com.example.myapplication.shared.core.security.SecurityProvider
import com.example.myapplication.shared.core.security.SecurityProviderImpl
import com.example.myapplication.shared.core.security.ConfigProvider
import com.example.myapplication.shared.core.security.ConfigProviderImpl
import com.example.myapplication.shared.features.reports.repository.PlatformFileExporter
import com.example.myapplication.shared.features.reports.repository.AndroidPlatformFileExporter
import com.example.myapplication.shared.features.settings.repository.DatabaseManager
import com.example.myapplication.shared.features.settings.repository.AndroidDatabaseManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize shared logging
        Logger.init()

        // Start Koin with shared DI modules and Android-specific DriverFactory
        startKoin {
            androidContext(this@MainActivity)
            modules(sharedModule, module {
                single { DriverFactory(androidContext()) }
                single<SecurityProvider> { SecurityProviderImpl() }
                single<ConfigProvider> { ConfigProviderImpl(androidContext()) }
                single<PlatformFileExporter> { AndroidPlatformFileExporter(androidContext()) }
                single<DatabaseManager> { AndroidDatabaseManager(androidContext()) }
            })
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}