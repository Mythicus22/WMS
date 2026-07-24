package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.presentation.navigation.AppNavHost
import com.example.myapplication.shared.presentation.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        val navigator = remember { Navigator() }
        AppNavHost(navigator = navigator)
    }
}