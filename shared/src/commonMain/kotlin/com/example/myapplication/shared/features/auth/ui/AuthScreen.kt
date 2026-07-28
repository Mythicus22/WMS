package com.example.myapplication.shared.features.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.features.auth.ui.login.LoginScreen
import com.example.myapplication.shared.features.auth.viewmodel.AuthViewModel
import org.koin.mp.KoinPlatform.getKoin

// Deprecated: Use SplashScreen or LoginScreen instead
@Composable
fun AuthScreen(navigator: Navigator) {
    val authViewModel: AuthViewModel = remember { getKoin().get() }
    LoginScreen(navigator = navigator, viewModel = authViewModel)
}
