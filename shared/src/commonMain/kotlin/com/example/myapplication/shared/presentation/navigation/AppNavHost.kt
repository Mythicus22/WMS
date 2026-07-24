package com.example.myapplication.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.features.auth.ui.splash.SplashScreen
import com.example.myapplication.shared.features.auth.ui.login.LoginScreen
import com.example.myapplication.shared.features.dashboard.ui.DashboardScreen
import com.example.myapplication.shared.features.shuttle.ui.ShuttleScreen
import com.example.myapplication.shared.features.operator.ui.OperatorScreen
import com.example.myapplication.shared.features.diagnostics.ui.DiagnosticsScreen
import com.example.myapplication.shared.features.maintenance.ui.MaintenanceScreen
import com.example.myapplication.shared.features.reports.ui.ReportsScreen
import com.example.myapplication.shared.features.settings.ui.SettingsScreen
import com.example.myapplication.shared.features.usermanagement.ui.UserManagementScreen

@Composable
fun AppNavHost(navigator: Navigator) {
    val screenState by navigator.current.collectAsState()

    when (screenState) {
        is Screen.Splash -> SplashScreen(navigator)
        is Screen.Login -> LoginScreen(navigator)
        is Screen.Dashboard -> DashboardScreen(navigator)
        is Screen.Shuttle -> ShuttleScreen(navigator)
        is Screen.Operator -> OperatorScreen(navigator)
        is Screen.Diagnostics -> DiagnosticsScreen(navigator)
        is Screen.Maintenance -> MaintenanceScreen(navigator)
        is Screen.Reports -> ReportsScreen(navigator)
        is Screen.Settings -> SettingsScreen(navigator)
        is Screen.UserManagement -> UserManagementScreen(navigator)
    }
}

