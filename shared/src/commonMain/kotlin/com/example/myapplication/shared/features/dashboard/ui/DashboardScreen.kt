package com.example.myapplication.shared.features.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.presentation.components.AppCard
import com.example.myapplication.shared.presentation.theme.AppDimensions

@Composable
fun DashboardScreen(navigator: Navigator) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppDimensions.spacing16),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = AppDimensions.spacing24)
        )

        // Operator Card
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppDimensions.spacing16),
            onClick = { navigator.navigateTo(Screen.Operator) }
        ) {
            Text(
                text = "Operator",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Manage operations",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = AppDimensions.spacing8)
            )
        }

        // Diagnostics Card
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppDimensions.spacing16),
            onClick = { navigator.navigateTo(Screen.Diagnostics) }
        ) {
            Text(
                text = "Diagnostics",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "System health and diagnostics",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = AppDimensions.spacing8)
            )
        }

        // Maintenance Card
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppDimensions.spacing16),
            onClick = { navigator.navigateTo(Screen.Maintenance) }
        ) {
            Text(
                text = "Maintenance",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Maintenance tasks and schedules",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = AppDimensions.spacing8)
            )
        }

        // Reports Card
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppDimensions.spacing16),
            onClick = { navigator.navigateTo(Screen.Reports) }
        ) {
            Text(
                text = "Reports",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "View and generate reports",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = AppDimensions.spacing8)
            )
        }

        // Settings Card
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppDimensions.spacing16),
            onClick = { navigator.navigateTo(Screen.Settings) }
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Application settings",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = AppDimensions.spacing8)
            )
        }
    }
}

