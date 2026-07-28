package com.example.myapplication.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.domain.usecase.GetCurrentUserUseCase
import com.example.myapplication.shared.features.auth.ui.login.LoginScreen
import com.example.myapplication.shared.features.auth.ui.splash.SplashScreen
import com.example.myapplication.shared.features.auth.viewmodel.AuthViewModel
import com.example.myapplication.shared.features.dashboard.ui.DashboardScreen
import com.example.myapplication.shared.features.dashboard.viewmodel.DashboardViewModel
import com.example.myapplication.shared.features.diagnostics.ui.DiagnosticsScreen
import com.example.myapplication.shared.features.maintenance.ui.MaintenanceScreen
import com.example.myapplication.shared.features.operator.ui.OperatorScreen
import com.example.myapplication.shared.features.reports.ui.ReportsScreen
import com.example.myapplication.shared.features.settings.ui.SettingsScreen
import com.example.myapplication.shared.features.settings.viewmodel.SettingsViewModel
import com.example.myapplication.shared.features.shuttle.ui.ShuttleScreen
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
    if (screenState != Screen.Splash && screenState != Screen.Login && isUnauthenticated) {
        LaunchedEffect(Unit) {
            navigator.navigateTo(Screen.Login)
        }
        val authViewModel: AuthViewModel = remember { getKoin().get() }
        LoginScreen(navigator = navigator, viewModel = authViewModel)
    } else {
        when (screenState) {
            is Screen.Splash -> SplashScreen(navigator)
            is Screen.Login -> {
                if (currentUser != null) {
                    LaunchedEffect(Unit) {
                        navigator.navigateTo(Screen.Dashboard)
                    }
                }
                val authViewModel: AuthViewModel = remember { getKoin().get() }
                LoginScreen(navigator = navigator, viewModel = authViewModel)
            }
            is Screen.Dashboard -> {
                val dashboardViewModel: DashboardViewModel = remember { getKoin().get() }
                DashboardScreen(navigator = navigator, viewModel = dashboardViewModel)
            }
            is Screen.Shuttle -> ShuttleScreen(navigator)
            is Screen.Operator -> OperatorScreen(navigator)
            is Screen.Diagnostics -> DiagnosticsScreen(navigator)
            is Screen.Maintenance -> MaintenanceScreen(navigator)
            is Screen.Reports -> ReportsScreen(navigator)
            is Screen.Settings -> {
                val settingsViewModel: SettingsViewModel = remember { getKoin().get() }
                SettingsScreen(navigator = navigator, viewModel = settingsViewModel)
            }
            is Screen.UserManagement -> {
                val userManagementViewModel: UserManagementViewModel = remember { getKoin().get() }
                UserManagementScreen(navigator = navigator, viewModel = userManagementViewModel)
            }
        }
    }
}
