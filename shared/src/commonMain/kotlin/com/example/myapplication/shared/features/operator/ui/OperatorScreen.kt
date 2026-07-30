package com.example.myapplication.shared.features.operator.ui

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
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.features.operator.viewmodel.OperatorViewModel
import com.example.myapplication.shared.features.shuttle.model.Shuttle
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.components.ChipStatus
import com.example.myapplication.shared.presentation.components.StatusChip
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions

@Composable
fun OperatorScreen(
    navigator: Navigator,
    viewModel: OperatorViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppToolbar(
            title = "Operator - Select Shuttle",
            onNavigationClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimensions.spacing16)
        ) {
            Text(
                text = "ENABLED FLEET SHUTTLES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.enabledShuttles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimensions.spacing32),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "No enabled shuttles found in database. Enable a shuttle in Shuttle Management first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing12),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val rows = state.enabledShuttles.chunked(2)
                    items(rows) { rowShuttles ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
                        ) {
                            for (shuttle in rowShuttles) {
                                OperatorShuttleCard(
                                    shuttle = shuttle,
                                    onClick = { navigator.navigateTo(Screen.OperatorConsole(shuttle.id)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowShuttles.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperatorShuttleCard(
    shuttle: Shuttle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.spacing16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(AppDimensions.spacing8))

                    Column {
                        Text(
                            text = shuttle.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ID: ${shuttle.id} • ${shuttle.plcIpAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(text = "ONLINE", status = ChipStatus.SUCCESS)
                    Spacer(modifier = Modifier.width(AppDimensions.spacing4))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Console",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(AppDimensions.spacing8),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "TASK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = "Standby", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Column {
                    Text(text = "STATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = "IDLE", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BatteryFull, contentDescription = null, tint = AppColors.Success, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "88%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "0.0m/s", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
