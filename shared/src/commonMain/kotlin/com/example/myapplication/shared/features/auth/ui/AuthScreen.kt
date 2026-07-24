package com.example.myapplication.shared.features.auth.ui

import androidx.compose.runtime.Composable
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.features.auth.ui.login.LoginScreen

// Deprecated: Use SplashScreen or LoginScreen instead
@Composable
fun AuthScreen(navigator: Navigator) {
    LoginScreen(navigator)
}

