package com.example.myapplication.shared.features.reports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.features.reports.model.*
import com.example.myapplication.shared.features.reports.viewmodel.*
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ReportsTabScreen(state: ReportsUiState, onEvent: (ReportsUiEvent) -> Unit) {
    val filter = state.filter
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        // ── Filter Bar ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = filter.searchQuery,
                onValueChange = { onEvent(ReportsUiEvent.UpdateFilter(filter.copy(searchQuery = it))) },
                placeholder = { Text("Search...", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1f).height(48.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedButton(
                onClick = { showFilterSheet = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Filter", style = MaterialTheme.typography.labelSmall)
            }
        }

        // ── Date Range Chip ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text("${filter.startDate} — ${filter.endDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text("• ${filter.sortOrder.displayName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }

        // ── Category Side Tabs (scrollable chip row) ───────────────────────
        val visibleCategories = if (state.shuttleId == null || state.shuttleId == "ALL") {
            ReportCategory.entries
        } else {
            ReportCategory.entries.filter { it != ReportCategory.SHUTTLE_UTILIZATION && it != ReportCategory.PRODUCTIVITY }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(visibleCategories) { cat ->
                val sel = cat == state.selectedCategory
                FilterChip(
                    selected = sel,
                    onClick = { onEvent(ReportsUiEvent.SelectCategory(cat)) },
                    label = { Text(cat.displayName, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (sel) ({ Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }) else null
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

        // ── Category Content ────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing8)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            when (state.selectedCategory) {
                ReportCategory.SUMMARY -> {
                    items(state.summary) { SummaryRow(it) }
                    if (state.summary.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.STORE_OPERATIONS -> {
                    items(state.storeOps) { OperationRow(it) }
                    if (state.storeOps.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.RETRIEVE_OPERATIONS -> {
                    items(state.retrieveOps) { OperationRow(it) }
                    if (state.retrieveOps.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.TASK_HISTORY -> {
                    items(state.tasks) { TaskRow(it) }
                    if (state.tasks.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.MISSION_HISTORY -> {
                    items(state.missions) { MissionRow(it) }
                    if (state.missions.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.SHUTTLE_UTILIZATION -> {
                    items(state.utilization) { UtilizationRow(it) }
                    if (state.utilization.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.BATTERY -> {
                    items(state.battery) { BatteryRow(it) }
                    if (state.battery.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.MOTOR_RUNTIME -> {
                    items(state.motorRuntime) { MotorRow(it) }
                    if (state.motorRuntime.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.FAULT_HISTORY -> {
                    items(state.faults) { FaultRow(it) }
                    if (state.faults.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.MAINTENANCE_HISTORY -> {
                    items(state.maintenance) { MaintenanceRow(it) }
                    if (state.maintenance.isEmpty()) item { EmptyDataCard() }
                }
                ReportCategory.PRODUCTIVITY -> {
                    items(state.productivity) { ProductivityRow(it) }
                    if (state.productivity.isEmpty()) item { EmptyDataCard() }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Filter Bottom Sheet (simplified as AlertDialog)
    if (showFilterSheet) {
        var tempStart by remember { mutableStateOf(filter.startDate) }
        var tempEnd   by remember { mutableStateOf(filter.endDate) }
        var tempSort  by remember { mutableStateOf(filter.sortOrder) }
        
        var showStartDatePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }
        
        val startDateState = rememberDatePickerState()
        val endDateState = rememberDatePickerState()

        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startDateState.selectedDateMillis?.let { millis ->
                            val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                            tempStart = date.toString()
                        }
                        showStartDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = startDateState) }
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endDateState.selectedDateMillis?.let { millis ->
                            val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                            tempEnd = date.toString()
                        }
                        showEndDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = endDateState) }
        }

        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            title = { Text("Filter & Sort", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempStart,
                        onValueChange = {},
                        label = { Text("Start Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Start Date")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = tempEnd,
                        onValueChange = {},
                        label = { Text("End Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select End Date")
                            }
                        }
                    )
                    Text("Sort Order", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Column {
                        SortOrder.entries.forEach { so ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { tempSort = so }.padding(vertical = 4.dp)) {
                                RadioButton(selected = tempSort == so, onClick = { tempSort = so })
                                Text(so.displayName, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onEvent(ReportsUiEvent.UpdateFilter(filter.copy(startDate = tempStart, endDate = tempEnd, sortOrder = tempSort)))
                    showFilterSheet = false
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showFilterSheet = false }) { Text("Cancel") } }
        )
    }
}

// ---------------------------------------------------------------------------
// ROW COMPOSABLES
// ---------------------------------------------------------------------------

@Composable
private fun ReportCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp), content = content) }
}

@Composable private fun KVRow(k: String, v: String, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(k, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        Spacer(Modifier.width(8.dp))
        Text(v, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun StatusBadge(status: String) {
    val (bg, fg) = when (status.uppercase()) {
        "COMPLETED" -> AppColors.Success.copy(alpha = 0.12f) to AppColors.Success
        "FAILED"    -> AppColors.Error.copy(alpha = 0.12f) to AppColors.Error
        "ABORTED"   -> AppColors.Warning.copy(alpha = 0.18f) to AppColors.Warning
        "RUNNING"   -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) to MaterialTheme.colorScheme.primary
        "CRITICAL"  -> AppColors.Error.copy(alpha = 0.12f) to AppColors.Error
        "MAJOR"     -> AppColors.Warning.copy(alpha = 0.18f) to AppColors.Warning
        else        -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(Modifier.background(bg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(status, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.Bold)
    }
}

@Composable fun SummaryRow(d: SummaryData) = ReportCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(d.shuttleName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(d.period, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Text("${d.totalOperations} ops", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) { KVRow("Stores", "${d.totalStores}"); KVRow("Retrieves", "${d.totalRetrieves}") }
        Column(Modifier.weight(1f)) { KVRow("Missions", "${d.totalMissions}"); KVRow("Faults", "${d.faultCount}") }
        Column(Modifier.weight(1f)) { KVRow("Uptime", "${d.uptimeHours}h"); KVRow("Battery Avg", "${d.avgBattery}%") }
    }
}

@Composable fun OperationRow(d: OperationRecord) = ReportCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(d.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("${d.shuttleName} • Level ${d.rackLevel} • ${d.rackPosition}", style = MaterialTheme.typography.bodySmall)
            Text(d.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Column(horizontalAlignment = Alignment.End) { StatusBadge(d.status); Spacer(Modifier.height(4.dp)); Text("${d.duration}s", style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable fun TaskRow(d: TaskRecord) = ReportCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(d.taskId, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("${d.shuttleName} • ${d.taskType}", style = MaterialTheme.typography.bodySmall)
            Text(d.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Column(horizontalAlignment = Alignment.End) {
            StatusBadge(d.status)
            Spacer(Modifier.height(4.dp))
            Box(Modifier.background(
                if (d.priority == "HIGH") AppColors.Error.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)
            ).padding(horizontal = 5.dp, vertical = 2.dp)) {
                Text(d.priority, style = MaterialTheme.typography.labelSmall, color = if (d.priority == "HIGH") AppColors.Error else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable fun MissionRow(d: MissionRecord) = ReportCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(d.missionId, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("${d.shuttleName} • ${d.missionType}", style = MaterialTheme.typography.bodySmall)
            Text(d.startTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Column(horizontalAlignment = Alignment.End) { StatusBadge(d.status); Spacer(Modifier.height(4.dp)); Text("${d.distance}m", style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable fun UtilizationRow(d: UtilizationRecord) = ReportCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text(d.shuttleName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold); Text(d.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
        Column(horizontalAlignment = Alignment.End) {
            Text("${d.utilizationPercent.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("${d.activeHours}h active", style = MaterialTheme.typography.labelSmall)
        }
    }
    LinearProgressIndicator(progress = { d.utilizationPercent / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp), trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
}

@Composable fun BatteryRow(d: BatteryRecord) = ReportCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text(d.shuttleName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold); Text(d.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
        Column(horizontalAlignment = Alignment.End) {
            Text("${d.batteryPercent.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (d.batteryPercent < 20f) AppColors.Error else AppColors.Success)
            StatusBadge(d.chargeStatus)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        KVRow("Voltage", "${d.voltage}V"); KVRow("Current", "${d.current}A"); KVRow("Temp", "${d.temperature}°C")
    }
}

@Composable fun MotorRow(d: MotorRuntimeRecord) = ReportCard {
    Text("${d.shuttleName} • ${d.date}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) { Text("Drive Motor", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); KVRow("Hours", "${d.driveMotorHours}h", Modifier.fillMaxWidth()); KVRow("Cycles", "${d.driveMotorCycles}", Modifier.fillMaxWidth()); KVRow("Temp", "${d.driveMotorTemp}°C", Modifier.fillMaxWidth()) }
        VerticalDivider(modifier = Modifier.height(60.dp).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        Column(Modifier.weight(1f)) { Text("Lift Motor", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary); KVRow("Hours", "${d.liftMotorHours}h", Modifier.fillMaxWidth()); KVRow("Cycles", "${d.liftMotorCycles}", Modifier.fillMaxWidth()); KVRow("Temp", "${d.liftMotorTemp}°C", Modifier.fillMaxWidth()) }
    }
}

@Composable fun FaultRow(d: FaultRecord) = ReportCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(d.faultId, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AppColors.Error)
                Spacer(Modifier.width(6.dp))
                StatusBadge(d.severity)
            }
            Text("${d.shuttleName} — ${d.faultType.replace('_', ' ')}", style = MaterialTheme.typography.bodySmall)
            Text(d.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(d.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${d.downtimeMinutes}min", style = MaterialTheme.typography.labelSmall, color = AppColors.Warning, fontWeight = FontWeight.Bold)
            if (d.resolvedAt != null) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppColors.Success, modifier = Modifier.size(16.dp).padding(top = 4.dp))
        }
    }
}

@Composable fun MaintenanceRow(d: MaintenanceRecord) = ReportCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("${d.shuttleName} — ${d.maintenanceType}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Technician: ${d.technician} • ${d.date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            if (d.partsReplaced.isNotEmpty()) Text("Parts: ${d.partsReplaced.joinToString(", ")}", style = MaterialTheme.typography.labelSmall)
        }
        Text("${d.duration}min", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable fun ProductivityRow(d: ProductivityRecord) = ReportCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text(d.shuttleName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold); Text(d.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
        Text("${d.efficiencyPercent.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (d.efficiencyPercent >= 95f) AppColors.Success else MaterialTheme.colorScheme.primary)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column { KVRow("Completed", "${d.cyclesCompleted}"); KVRow("Failed", "${d.cyclesFailed}") }
        Column { KVRow("Avg Time", "${d.avgCycleTimeSec.toInt()}s"); KVRow("Throughput", "${d.throughputPerHour.toInt()}/hr") }
    }
}

@Composable private fun EmptyDataCard() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(6.dp))
            Text("No records found", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}
