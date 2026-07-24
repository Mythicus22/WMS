package com.example.myapplication.shared.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Simple navigation abstraction for shared module.
// This is intentionally lightweight so it compiles on all targets without Android Navigation dependency.

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Shuttle : Screen("shuttle")
    object Operator : Screen("operator")
    object Diagnostics : Screen("diagnostics")
    object Maintenance : Screen("maintenance")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object UserManagement : Screen("usermanagement")
}

class Navigator(initial: Screen = Screen.Splash) {
    private val _current = MutableStateFlow(initial)
    val current: StateFlow<Screen> = _current

    fun navigate(screen: Screen) {
        _current.update { screen }
    }

    fun navigateTo(screen: Screen) {
        navigate(screen)
    }
}

// A small Compose-based NavHost that switches between provided content lambdas for each screen.
@Composable
fun rememberNavigator(initial: Screen = Screen.Splash): Navigator {
    val scope = rememberCoroutineScope()
    return remember { Navigator(initial) }
}

