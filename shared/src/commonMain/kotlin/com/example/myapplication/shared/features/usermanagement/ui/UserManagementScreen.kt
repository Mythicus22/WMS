package com.example.myapplication.shared.features.usermanagement.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.presentation.components.PrimaryButton
import com.example.myapplication.shared.presentation.theme.AppDimensions

@Composable
fun UserManagementScreen(navigator: Navigator) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.spacing16),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "User Management",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = AppDimensions.spacing32)
        )
        Text(
            text = "User management screen",
            style = MaterialTheme.typography.bodyMedium
        )
        PrimaryButton(
            text = "Back to Dashboard",
            onClick = { navigator.navigateTo(Screen.Dashboard) },
            modifier = Modifier.padding(top = AppDimensions.spacing24)
        )
    }
}

