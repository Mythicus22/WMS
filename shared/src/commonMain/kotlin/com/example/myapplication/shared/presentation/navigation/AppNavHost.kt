package com.example.myapplication.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.domain.usecase.GetCurrentUserUseCase
import com.example.myapplication.shared.features.auth.ui.login.LoginScreen
import com.example.myapplication.shared.features.auth.ui.splash.SplashScreen
import com.example.myapplication.shared.features.auth.viewmodel.AuthViewModel
import com.example.myapplication.shared.features.dashboard.ui.DashboardScreen
import com.example.myapplication.shared.features.dashboard.viewmodel.DashboardViewModel
import com.example.myapplication.shared.features.diagnostics.ui.DiagnosticsDashboardScreen
import com.example.myapplication.shared.features.diagnostics.viewmodel.DiagnosticsDashboardViewModel
import com.example.myapplication.shared.features.maintenance.ui.MaintenanceConsoleScreen
import com.example.myapplication.shared.features.maintenance.ui.MaintenanceTestDetailScreen
import com.example.myapplication.shared.features.maintenance.viewmodel.MaintenanceConsoleViewModel
import com.example.myapplication.shared.features.maintenance.viewmodel.MaintenanceTestDetailViewModel
import com.example.myapplication.shared.features.operator.ui.OperatorConsoleScreen
import com.example.myapplication.shared.features.operator.viewmodel.OperatorConsoleViewModel
import com.example.myapplication.shared.features.reports.ui.ReportsMainScreen
import com.example.myapplication.shared.features.reports.ui.ReportsScreen
import com.example.myapplication.shared.features.reports.viewmodel.ReportsViewModel
import com.example.myapplication.shared.features.device.repository.RegisteredShuttleRepository
import com.example.myapplication.shared.features.settings.ui.SettingsScreen
import com.example.myapplication.shared.features.settings.viewmodel.SettingsViewModel

import com.example.myapplication.shared.features.usermanagement.ui.UserManagementScreen
import com.example.myapplication.shared.features.usermanagement.viewmodel.UserManagementViewModel
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun AppNavHost(navigator: Navigator) {
    val screenState by navigator.current.collectAsState()

    val getCurrentUserUseCase: GetCurrentUserUseCase = remember { getKoin().get() }
    val currentUser by getCurrentUserUseCase.currentUser.collectAsState(initial = getCurrentUserUseCase.get())

    val isUnauthenticated = currentUser == null

    // Navigation Protection Guard: Block access to protected screens if unauthenticated
    if (screenState != Screen.Splash && screenState != Screen.Login && screenState != Screen.Setup && isUnauthenticated) {
        LaunchedEffect(Unit) {
            val configProvider: com.example.myapplication.shared.core.security.ConfigProvider = getKoin().get()
            if (!configProvider.isConfigured()) {
                navigator.navigateTo(Screen.Setup)
            } else {
                navigator.navigateTo(Screen.Login)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        when (screenState) {
            is Screen.Splash -> SplashScreen(navigator = navigator)
            is Screen.Setup -> {
                val setupViewModel: com.example.myapplication.shared.features.auth.viewmodel.SetupViewModel = remember { getKoin().get() }
                com.example.myapplication.shared.features.auth.ui.setup.SetupScreen(navigator = navigator, viewModel = setupViewModel)
            }
            is Screen.Login -> {
                val authViewModel: AuthViewModel = remember { getKoin().get() }
                LoginScreen(navigator = navigator, viewModel = authViewModel)
            }

            else -> {
                // BackHandler only for screens beyond root/login
                if (screenState != Screen.Dashboard) {
                    com.example.myapplication.shared.core.navigation.BackHandler {
                        navigator.goBack()
                    }
                }

                when (screenState) {
                is Screen.Dashboard -> {
                    val dashboardViewModel: DashboardViewModel = remember { getKoin().get() }
                    DashboardScreen(navigator = navigator, viewModel = dashboardViewModel)
                }
                is Screen.Shuttle -> {
                    val shuttleViewModel: com.example.myapplication.shared.features.device.viewmodel.ShuttleManagementViewModel = remember { getKoin().get() }
                    com.example.myapplication.shared.features.device.ui.ShuttleManagementScreen(navigator = navigator, viewModel = shuttleViewModel)
                }
                is Screen.OperatorConsole -> {
                    val operatorConsoleViewModel: OperatorConsoleViewModel = remember { getKoin().get() }
                    OperatorConsoleScreen(
                        navigator = navigator,
                        viewModel = operatorConsoleViewModel
                    )
                }
                is Screen.DiagnosticsDashboard -> {
                    val diagnosticsDashboardViewModel: DiagnosticsDashboardViewModel = remember { getKoin().get() }
                    DiagnosticsDashboardScreen(
                        navigator = navigator,
                        viewModel = diagnosticsDashboardViewModel
                    )
                }
                is Screen.MaintenanceConsole -> {
                    val maintenanceConsoleViewModel: MaintenanceConsoleViewModel = remember { getKoin().get() }
                    MaintenanceConsoleScreen(
                        navigator = navigator,
                        viewModel = maintenanceConsoleViewModel
                    )
                }
                is Screen.MaintenanceTestDetail -> {
                    val maintenanceTestDetailViewModel: MaintenanceTestDetailViewModel = remember { getKoin().get() }
                    val currentScreen = screenState as Screen.MaintenanceTestDetail
                    MaintenanceTestDetailScreen(
                        navigator = navigator,
                        viewModel = maintenanceTestDetailViewModel,
                        testId = currentScreen.testId
                    )
                }
                is Screen.ReportsShuttleSelect -> ReportsScreen(navigator)
                is Screen.Reports -> {
                    val currentScreen = screenState as Screen.Reports
                    val sid = currentScreen.shuttleId.takeIf { it != "ALL" }
                    val shuttleRepo: RegisteredShuttleRepository = remember { getKoin().get() }
                    val shuttles by shuttleRepo.getAllRegisteredShuttles().collectAsState(initial = emptyList())
                    val shuttleName = if (currentScreen.shuttleId == "ALL") "All Shuttles"
                        else shuttles.find { it.deviceId == currentScreen.shuttleId }?.nameToDisplay ?: currentScreen.shuttleId
                    val reportsViewModel: ReportsViewModel = remember(currentScreen.shuttleId) {
                        getKoin().get { org.koin.core.parameter.parametersOf(sid, shuttleName) }
                    }
                    ReportsMainScreen(navigator = navigator, viewModel = reportsViewModel, shuttleName = shuttleName)
                }
                is Screen.Settings -> {
                    val settingsViewModel: SettingsViewModel = remember { getKoin().get() }
                    SettingsScreen(navigator = navigator, viewModel = settingsViewModel)
                }
                is Screen.UserManagement -> {
                    val userManagementViewModel: UserManagementViewModel = remember { getKoin().get() }
                    UserManagementScreen(navigator = navigator, viewModel = userManagementViewModel)
                }
                else -> {}
            }
        }
    }
}
}
