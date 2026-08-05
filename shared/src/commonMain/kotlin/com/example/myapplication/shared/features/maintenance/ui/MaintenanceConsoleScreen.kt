package com.example.myapplication.shared.features.maintenance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.features.maintenance.model.TestCategory
import com.example.myapplication.shared.features.maintenance.model.TestDefinition
import com.example.myapplication.shared.features.maintenance.viewmodel.MaintenanceConsoleViewModel
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.theme.AppDimensions

@Composable
fun MaintenanceConsoleScreen(
    navigator: Navigator,
    viewModel: MaintenanceConsoleViewModel
) {

    val state by viewModel.uiState.collectAsState()
    val shuttleTitle = state.shuttle?.nameToDisplay ?: "Active Shuttle"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppToolbar(
            title = "Maintenance Console: $shuttleTitle",
            onNavigationClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimensions.spacing16)
        ) {
            if (state.isMockData) {
                Row(modifier = Modifier.fillMaxWidth().background(com.example.myapplication.shared.presentation.theme.AppColors.Warning.copy(alpha = 0.2f)).padding(8.dp), horizontalArrangement = Arrangement.Center) {
                    Text("No Active Shuttle - Showing Mock Data", color = com.example.myapplication.shared.presentation.theme.AppColors.Warning, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(AppDimensions.spacing12))
            }
            Text(
                text = "HARDWARE & SENSOR DIAGNOSTIC TEST SUITE (14 TESTS)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing12),
                modifier = Modifier.fillMaxSize()
            ) {
                val rows = state.tests.chunked(2)
                items(rows) { rowTests ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
                    ) {
                        for (test in rowTests) {
                            MaintenanceTestCard(
                                test = test,
                                onClick = {
                                    navigator.navigateTo(Screen.MaintenanceTestDetail(test.id))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowTests.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceTestCard(
    test: TestDefinition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon: ImageVector = when (test.category) {
        TestCategory.SENSORS -> Icons.Default.Sensors
        TestCategory.MOTORS -> Icons.Default.PrecisionManufacturing
        TestCategory.ELECTRICAL_RELAYS -> Icons.Default.ElectricalServices
        TestCategory.COMMUNICATION -> Icons.Default.Wifi
        TestCategory.BATTERY_POWER -> Icons.Default.BatteryFull
        TestCategory.SAFETY_ESTOP -> Icons.Default.Shield
    }

    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.spacing16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(AppDimensions.spacing8))

                    Column {
                        Text(
                            text = test.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = test.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing8))

            Text(
                text = test.objective,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )
        }
    }
}
