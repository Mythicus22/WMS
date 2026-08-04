package com.example.myapplication.shared.core.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop does not have a system back button, do nothing
}
