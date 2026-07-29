package com.example.myapplication.shared.features.maintenance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.features.maintenance.model.TestDefinition
import com.example.myapplication.shared.features.maintenance.model.TestExecutionState
import com.example.myapplication.shared.features.maintenance.model.TestParameter
import com.example.myapplication.shared.features.maintenance.model.TestStatus
import com.example.myapplication.shared.features.maintenance.viewmodel.MaintenanceTestDetailViewModel
import com.example.myapplication.shared.features.maintenance.viewmodel.MaintenanceTestEvent
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.components.ChipStatus
import com.example.myapplication.shared.presentation.components.StatusChip
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions

@Composable
fun MaintenanceTestDetailScreen(
    navigator: Navigator,
    viewModel: MaintenanceTestDetailViewModel,
    shuttleId: String,
    testId: String
) {
    LaunchedEffect(shuttleId, testId) {
        viewModel.initialize(shuttleId, testId)
    }

    val state by viewModel.uiState.collectAsState()
    val def = state.definition
    val exec = state.executionState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppToolbar(
            title = def?.name ?: "Test Detail",
            onNavigationClick = { navigator.goBack() }
        )

        if (def == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Test definition not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppDimensions.spacing16)
            ) {
                // Action message banner
                state.actionMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = AppDimensions.spacing12, vertical = AppDimensions.spacing8),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(AppDimensions.spacing8))
                            Text(text = msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        TextButton(onClick = { viewModel.onEvent(MaintenanceTestEvent.DismissMessage) }) {
                            Text("OK", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(AppDimensions.spacing12))
                }

                // 1. Header & Status Section
                TestHeaderCard(def = def, exec = exec)

                Spacer(modifier = Modifier.height(AppDimensions.spacing16))

                // 2. Control Actions (Run / Reset)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing12)
                ) {
                    Button(
                        onClick = { viewModel.onEvent(MaintenanceTestEvent.RunTest) },
                        enabled = !state.isRunning,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (state.isRunning) "Executing..." else "Run Test", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = { viewModel.onEvent(MaintenanceTestEvent.ResetTest) },
                        enabled = !state.isRunning,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Reset Test", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.spacing16))

                // 3. Animation / Visual Execution Diagram Placeholder
                AnimationPlaceholderCard()

                Spacer(modifier = Modifier.height(AppDimensions.spacing16))

                // 4. Test Objective, Description & Preconditions
                TestOverviewCard(def = def)

                Spacer(modifier = Modifier.height(AppDimensions.spacing16))

                // 5. Current Mock PLC & Sensor Values
                TelemetryValuesCard(exec = exec)

                Spacer(modifier = Modifier.height(AppDimensions.spacing16))

                // 6. Expected Responses & Behaviour
                ExpectedResponsesCard(def = def)

                Spacer(modifier = Modifier.height(AppDimensions.spacing16))

                // 7. Result Summary & Pass/Fail Status
                ResultSummaryCard(exec = exec)

                Spacer(modifier = Modifier.height(AppDimensions.spacing16))

                // 8. Troubleshooting Guide
                TroubleshootingGuideCard(guide = def.troubleshootingGuide)
            }
        }
    }
}

@Composable
private fun TestHeaderCard(def: TestDefinition, exec: TestExecutionState) {
    val chipStatus = when (exec.status) {
        TestStatus.IDLE -> ChipStatus.INFO
        TestStatus.RUNNING -> ChipStatus.WARNING
        TestStatus.PASSED -> ChipStatus.SUCCESS
        TestStatus.FAILED -> ChipStatus.ERROR
        TestStatus.WARNING -> ChipStatus.WARNING
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = def.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Category: ${def.category.displayName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                StatusChip(text = exec.status.displayName, status = chipStatus)
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Progress: ${(exec.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                exec.lastRunTimestamp?.let {
                    Text(text = "Last Run: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = exec.progress,
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun AnimationPlaceholderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Test Execution Visualization / Animation Placeholder",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun TestOverviewCard(def: TestDefinition) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Text(text = "TEST OBJECTIVE & PRECONDITIONS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Objective:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text = def.objective, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Description:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text = def.description, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Required Preconditions:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            def.requiredPreconditions.forEach { pre ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(text = "• ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = pre, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun TelemetryValuesCard(exec: TestExecutionState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Text(text = "CURRENT PLC & SENSOR TELEMETRY (MOCK)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "PLC Registers:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            exec.currentPlcValues.forEach { param ->
                ParameterRow(param = param)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Sensor Telemetry:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            exec.currentSensorValues.forEach { param ->
                ParameterRow(param = param)
            }
        }
    }
}

@Composable
private fun ParameterRow(param: TestParameter) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = param.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(
            text = "${param.value} ${param.unit}".trim(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (param.isNormal) MaterialTheme.colorScheme.onSurface else AppColors.Error
        )
    }
}

@Composable
private fun ExpectedResponsesCard(def: TestDefinition) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Text(text = "EXPECTED HARDWARE & BEHAVIOURAL RESPONSES", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Expected PLC Response:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text = def.expectedPlcResponse, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Expected Sensor Response:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text = def.expectedSensorResponse, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Expected Behaviour:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text = def.expectedBehaviour, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ResultSummaryCard(exec: TestExecutionState) {
    val isPassed = exec.status == TestStatus.PASSED
    val cardBg = if (isPassed) AppColors.Success.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = if (isPassed) AppColors.Success else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isPassed) AppColors.Success else AppColors.Warning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RESULT SUMMARY & EVALUATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPassed) AppColors.Success else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = exec.resultSummary ?: "Execute test to generate result summary.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TroubleshootingGuideCard(guide: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Text(text = "TROUBLESHOOTING & RECOVERY GUIDE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))

            guide.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Text(text = "${index + 1}. ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = step, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
