package com.example.myapplication.shared.features.reports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.features.reports.viewmodel.ReportsMainTab
import com.example.myapplication.shared.features.reports.viewmodel.ReportsUiEvent
import com.example.myapplication.shared.features.reports.viewmodel.ReportsViewModel
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.theme.AppColors

@Composable
fun ReportsMainScreen(
    navigator: Navigator,
    viewModel: ReportsViewModel,
    shuttleName: String
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Toolbar with Export action ─────────────────────────────────────
        AppToolbar(
            title = "Reports & Analytics",
            onNavigationClick = { navigator.goBack() },
            actions = {
                // Shuttle context chip
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(shuttleName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                // Single Export button
                IconButton(
                    onClick = { viewModel.onEvent(ReportsUiEvent.RequestExport) },
                    enabled = !state.isExporting
                ) {
                    if (state.isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        )

        // ── Status Banner ─────────────────────────────────────────────────
        state.statusMessage?.let { msg ->
            val isSuccess = state.exportResult?.success != false
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(if (isSuccess) AppColors.Success.copy(alpha = 0.10f) else AppColors.Error.copy(alpha = 0.10f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isSuccess) AppColors.Success else AppColors.Error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(msg, style = MaterialTheme.typography.bodySmall, color = if (isSuccess) AppColors.Success else AppColors.Error)
                }
                TextButton(onClick = { viewModel.onEvent(ReportsUiEvent.DismissStatus) }) {
                    Text("OK", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // ── Tab Row ───────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = ReportsMainTab.entries.indexOf(state.selectedTab),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            ReportsMainTab.entries.forEach { tab ->
                val icon = when (tab) {
                    ReportsMainTab.REPORTS -> Icons.Default.Description
                    ReportsMainTab.ANALYTICS -> Icons.Default.Analytics
                }
                Tab(
                    selected = tab == state.selectedTab,
                    onClick = { viewModel.onEvent(ReportsUiEvent.SelectTab(tab)) },
                    text = { Text(tab.title, fontWeight = if (tab == state.selectedTab) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(icon, contentDescription = tab.title, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // ── Tab Content ───────────────────────────────────────────────────
        when (state.selectedTab) {
            ReportsMainTab.REPORTS   -> ReportsTabScreen(state = state, onEvent = viewModel::onEvent)
            ReportsMainTab.ANALYTICS -> AnalyticsTabScreen(state = state)
        }
    }
}
