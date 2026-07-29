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
    data class OperatorConsole(val shuttleId: String) : Screen("operatorConsole")
    object Diagnostics : Screen("diagnostics")
    object Maintenance : Screen("maintenance")
    data class MaintenanceConsole(val shuttleId: String) : Screen("maintenanceConsole")
    data class MaintenanceTestDetail(val shuttleId: String, val testId: String) : Screen("maintenanceTestDetail")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object UserManagement : Screen("usermanagement")
    data class AddEditShuttle(val id: String? = null) : Screen("addEditShuttle")
}

class Navigator(initial: Screen = Screen.Splash) {
    private val _backStack = MutableStateFlow<List<Screen>>(listOf(initial))
    private val _current = MutableStateFlow(initial)
    val current: StateFlow<Screen> = _current

    fun navigateTo(screen: Screen) {
        if (_current.value == screen) return
        _backStack.update { stack -> stack + screen }
        _current.update { screen }
    }

    fun navigate(screen: Screen) {
        navigateTo(screen)
    }

    fun goBack(): Boolean {
        val stack = _backStack.value
        if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            val previousScreen = newStack.last()
            _backStack.value = newStack
            _current.value = previousScreen
            return true
        }
        return false
    }

    fun popBackStack() {
        goBack()
    }
}

// A small Compose-based NavHost that switches between provided content lambdas for each screen.
@Composable
fun rememberNavigator(initial: Screen = Screen.Splash): Navigator {
    val scope = rememberCoroutineScope()
    return remember { Navigator(initial) }
}


