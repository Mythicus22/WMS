package com.example.myapplication.shared.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.features.settings.model.*
import com.example.myapplication.shared.features.settings.viewmodel.*
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions

// ---------------------------------------------------------------------------
// ROOT SCREEN
// ---------------------------------------------------------------------------
@Composable
fun SettingsScreen(navigator: Navigator, viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsState()

    // Per-tab edit mode (UI-local, not in ViewModel)
    var isEditing by remember(state.selectedTab) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar with dynamic actions
        AppToolbar(
            title = "Settings — ${state.selectedTab.title}",
            onNavigationClick = { navigator.goBack() },
            actions = {
                when {
                    state.selectedTab == SettingsTab.ABOUT -> Unit
                    state.selectedTab == SettingsTab.BACKUP -> Unit
                    isEditing -> {
                        // The user requested to remove the top Save and Cancel buttons.
                        // We leave this empty so only the bottom buttons are shown during edit.
                    }
                    else -> {
                        TextButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )

        // Status Banner
        state.statusMessage?.let { msg ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(AppColors.Success.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppColors.Success, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(msg, style = MaterialTheme.typography.bodySmall, color = AppColors.Success)
                }
                TextButton(onClick = { viewModel.onEvent(SettingsUiEvent.DismissStatusMessage) }) {
                    Text("OK", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Error Banner
        state.errorMessage?.let { msg ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(AppColors.Error.copy(alpha = 0.10f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(msg, style = MaterialTheme.typography.bodySmall, color = AppColors.Error)
                }
                TextButton(onClick = { viewModel.onEvent(SettingsUiEvent.DismissErrorMessage) }) {
                    Text("Dismiss", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Tab row
        if (state.availableTabs.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = state.availableTabs.indexOf(state.selectedTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                state.availableTabs.forEach { tab ->
                    val isSelected = tab == state.selectedTab
                    val icon: ImageVector = when (tab) {
                        SettingsTab.GENERAL -> Icons.Default.Settings
                        SettingsTab.COMMUNICATION -> Icons.Default.Wifi
                        SettingsTab.REPORTS -> Icons.Default.Analytics
                        SettingsTab.BACKUP -> Icons.Default.Backup
                        SettingsTab.ABOUT -> Icons.Default.Info
                    }
                    Tab(
                        selected = isSelected,
                        onClick = {
                            isEditing = false
                            viewModel.onEvent(SettingsUiEvent.SelectTab(tab))
                        },
                        text = { Text(tab.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(icon, contentDescription = tab.title, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }

        // Tab content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (state.selectedTab) {
                SettingsTab.GENERAL -> GeneralSettingsView(
                    general = state.settings.general,
                    isEditing = isEditing,
                    onSave = { updated ->
                        viewModel.onEvent(SettingsUiEvent.UpdateGeneral(updated))
                        isEditing = false
                    },
                    onCancel = { isEditing = false },
                    onRequestEdit = { isEditing = true },
                    onRevertToDefault = { viewModel.onEvent(SettingsUiEvent.ResetToDefault(SettingsTab.GENERAL)) }
                )
                SettingsTab.COMMUNICATION -> CommunicationSettingsView(
                    comm = state.settings.communication,
                    diagnostics = state.diagnostics,
                    isEditing = isEditing,
                    onSave = { updated ->
                        viewModel.onEvent(SettingsUiEvent.UpdateCommunication(updated))
                        isEditing = false
                    },
                    onTestConnection = { comm -> viewModel.onEvent(SettingsUiEvent.OnTestConnection(comm)) },
                    onDisconnect = { viewModel.onEvent(SettingsUiEvent.OnDisconnect) },
                    onCancel = { isEditing = false },
                    onRevertToDefault = { viewModel.onEvent(SettingsUiEvent.ResetToDefault(SettingsTab.COMMUNICATION)) }
                )
                SettingsTab.REPORTS -> ReportSettingsView(
                    reports = state.settings.reports,
                    isEditing = isEditing,
                    onSave = { updated ->
                        viewModel.onEvent(SettingsUiEvent.UpdateReports(updated))
                        isEditing = false
                    },
                    onCancel = { isEditing = false },
                    onRevertToDefault = { viewModel.onEvent(SettingsUiEvent.ResetToDefault(SettingsTab.REPORTS)) }
                )
                SettingsTab.BACKUP -> BackupSettingsView(
                    backup = state.settings.backup,
                    isProcessing = state.isProcessing,
                    isEditing = isEditing,
                    onAutoBackupToggle = { enabled ->
                        viewModel.onEvent(SettingsUiEvent.UpdateBackup(
                            state.settings.backup.copy(automaticBackup = enabled)
                        ))
                    },
                    onBackupNow = { path -> viewModel.onEvent(SettingsUiEvent.PerformBackup(path)) },
                    onRestore = { backupId -> viewModel.onEvent(SettingsUiEvent.PerformRestore(backupId)) },
                    onRevertToDefault = { viewModel.onEvent(SettingsUiEvent.ResetToDefault(SettingsTab.BACKUP)) }
                )
                SettingsTab.ABOUT -> AboutSystemView(systemInfo = state.settings.systemInfo)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// REUSABLE DROPDOWN SELECTOR
// ---------------------------------------------------------------------------
@Composable
private fun <T> SettingsDropdown(
    label: String,
    selected: T,
    options: List<T>,
    displayName: (T) -> String,
    enabled: Boolean,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = displayName(selected),
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                if (enabled) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand"
                        )
                    }
                }
            }
        )
        if (enabled) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(displayName(option)) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                        leadingIcon = if (option == selected) ({
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }) else null
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SEGMENTED BUTTON ROW
// ---------------------------------------------------------------------------
@Composable
private fun <T> SegmentedButtonRow(
    options: List<T>,
    selected: T,
    displayName: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (T) -> ImageVector? = { null }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                        shape = when (index) {
                            0 -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                            options.size - 1 -> RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                            else -> RoundedCornerShape(0.dp)
                        }
                    )
                    .clickable(enabled = enabled && !isSelected) { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val optionIcon = icon(option)
                    if (optionIcon != null) {
                        Icon(
                            imageVector = optionIcon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = displayName(option),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            if (index < options.size - 1 && !isSelected && options[index + 1] != selected) {
                Divider(
                    modifier = Modifier.width(1.dp).fillMaxHeight(0.6f),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SETTINGS CARD WRAPPER
// ---------------------------------------------------------------------------
@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// READ-ONLY VALUE ROW
// ---------------------------------------------------------------------------
@Composable
private fun ViewRow(label: String, value: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}

// ---------------------------------------------------------------------------
// SWITCH ROW
// ---------------------------------------------------------------------------
@Composable
private fun SwitchRow(label: String, subtitle: String, checked: Boolean, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Switch(checked = checked, onCheckedChange = onToggle, enabled = enabled)
    }
}

// ---------------------------------------------------------------------------
// 1. GENERAL SETTINGS VIEW
// ---------------------------------------------------------------------------
@Composable
private fun GeneralSettingsView(
    general: GeneralSettings,
    isEditing: Boolean,
    onSave: (GeneralSettings) -> Unit,
    onCancel: () -> Unit,
    onRequestEdit: () -> Unit,
    onRevertToDefault: () -> Unit
) {
    // Draft state (only mutated when editing)
    var draft by remember(general) { mutableStateOf(general) }
    // Reset draft on cancel
    LaunchedEffect(isEditing) { if (!isEditing) draft = general }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // ── Theme Toggle (always active – immediate effect) ──────────────────
        SettingsCard("APPLICATION THEME") {
            SegmentedButtonRow(
                options = ThemeMode.entries,
                selected = draft.themeMode,
                displayName = { it.displayName },
                onSelect = { mode ->
                    draft = draft.copy(themeMode = mode)
                    onSave(draft) // Immediate save for theme
                },
                icon = { mode ->
                    when (mode) {
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                    }
                }
            )
        }

        // ── Warehouse & Company ──────────────────────────────────────────────
        SettingsCard("WAREHOUSE & COMPANY") {
            if (isEditing) {
                OutlinedTextField(value = draft.warehouseName, onValueChange = { draft = draft.copy(warehouseName = it) }, label = { Text("Warehouse Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = draft.warehouseCode, onValueChange = { draft = draft.copy(warehouseCode = it) }, label = { Text("Warehouse Code (max 20 chars)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = draft.companyName, onValueChange = { draft = draft.copy(companyName = it) }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            } else {
                ViewRow("Warehouse Name", general.warehouseName)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Warehouse Code", general.warehouseCode)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Company Name", general.companyName)
            }
        }

        // ── Language ─────────────────────────────────────────────────────────
        SettingsCard("LANGUAGE PREFERENCE") {
            SegmentedButtonRow(
                options = AppLanguage.entries,
                selected = draft.language,
                displayName = { it.displayName },
                onSelect = { lang ->
                    draft = draft.copy(language = lang)
                    if (!isEditing) onSave(draft)
                },
                enabled = isEditing || true // Always enabled for quick toggle like theme? No, keep it as isEditing unless we want it quick. Let's make it quick toggle like design.
            )
        }

        // ── Date & Time ───────────────────────────────────────────────────────
        SettingsCard("DATE & TIME FORMAT") {
            if (isEditing) {
                SettingsDropdown(label = "Date Format", selected = draft.dateFormat, options = DateFormat.entries, displayName = { it.displayName }, enabled = true) { draft = draft.copy(dateFormat = it) }
                SettingsDropdown(label = "Time Format", selected = draft.timeFormat, options = TimeFormat.entries, displayName = { it.displayName }, enabled = true) { draft = draft.copy(timeFormat = it) }
            } else {
                ViewRow("Date Format", general.dateFormat.displayName, Icons.Default.DateRange)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Time Format", general.timeFormat.displayName, Icons.Default.Schedule)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            // Device time refresh (always shown)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Device Date & Time", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(general.currentDateTimePreview, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { /* timestamp refresh */ },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Refresh", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // ── Display Preferences ───────────────────────────────────────────────
        SettingsCard("DISPLAY PREFERENCES") {
            if (isEditing) {
                SettingsDropdown(label = "Default Measurement Units", selected = draft.defaultUnits, options = MeasurementUnit.entries, displayName = { it.displayName }, enabled = true) { draft = draft.copy(defaultUnits = it) }
                SettingsDropdown(label = "Font Family", selected = draft.fontFamily, options = AppFontFamily.entries, displayName = { it.displayName }, enabled = true) { draft = draft.copy(fontFamily = it) }
                // Font Size — segmented buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Font Size", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    SegmentedButtonRow(
                        options = AppFontSize.entries,
                        selected = draft.fontSize,
                        displayName = { it.displayName.substringBefore(" ") },
                        onSelect = { draft = draft.copy(fontSize = it) },
                        enabled = true
                    )
                }
            } else {
                ViewRow("Measurement Units", general.defaultUnits.displayName, Icons.Default.Straighten)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Font Family", general.fontFamily.displayName, Icons.Default.TextFields)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Font Size", general.fontSize.displayName, Icons.Default.FormatSize)
            }
        }

        // Save / Cancel / Revert buttons when editing
        if (isEditing) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onRevertToDefault,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Revert to Default Settings")
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { onCancel() }, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cancel")
                    }
                    Button(onClick = { onSave(draft) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeToggleButton(
    mode: ThemeMode, icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// 2. COMMUNICATION SETTINGS VIEW
// ---------------------------------------------------------------------------
@Composable
private fun CommunicationSettingsView(
    comm: CommunicationSettings,
    diagnostics: ConnectionDiagnostics,
    isEditing: Boolean,
    onSave: (CommunicationSettings) -> Unit,
    onTestConnection: (CommunicationSettings) -> Unit,
    onDisconnect: () -> Unit,
    onCancel: () -> Unit,
    onRevertToDefault: () -> Unit
) {
    var draft by remember(comm) { mutableStateOf(comm) }
    LaunchedEffect(isEditing) { if (!isEditing) draft = comm }

    var portError by remember { mutableStateOf<String?>(null) }
    var directWsPortError by remember { mutableStateOf<String?>(null) }
    var keepAliveError by remember { mutableStateOf<String?>(null) }
    var heartbeatError by remember { mutableStateOf<String?>(null) }
    var timeoutError by remember { mutableStateOf<String?>(null) }
    
    // For Direct Mode IP input
    var newIpInput by remember { mutableStateOf("") }
    var ipError by remember { mutableStateOf<String?>(null) }
    val ipRegex = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$".toRegex()
    
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsCard("COMMUNICATION MODE") {
            if (isEditing) {
                SegmentedButtonRow(
                    options = CommunicationMode.entries,
                    selected = draft.mode,
                    displayName = { it.displayName },
                    onSelect = { draft = draft.copy(mode = it) },
                    enabled = true,
                    icon = { mode ->
                        when (mode) {
                            CommunicationMode.MQTT_BROKER -> Icons.Default.Cloud
                            CommunicationMode.DIRECT -> Icons.Default.Router
                        }
                    }
                )
            } else {
                ViewRow("Current Mode", comm.mode.displayName, Icons.Default.NetworkWifi)
            }
        }

        if (draft.mode == CommunicationMode.MQTT_BROKER) {
            SettingsCard("MQTT BROKER CONFIGURATION") {
                if (isEditing) {
                    OutlinedTextField(value = draft.mqttBrokerAddress, onValueChange = { draft = draft.copy(mqttBrokerAddress = it) }, label = { Text("Broker IP / Hostname") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(
                        value = draft.mqttPort.toString(),
                        onValueChange = { v ->
                            val n = v.toIntOrNull()
                            portError = if (n == null || n !in 1..65535) "Port must be 1–65535" else null
                            draft = draft.copy(mqttPort = n ?: draft.mqttPort)
                        },
                        label = { Text("MQTT Port") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = portError != null,
                        supportingText = portError?.let { { Text(it, color = AppColors.Error) } },
                        singleLine = true
                    )
                    OutlinedTextField(value = draft.clientId, onValueChange = { draft = draft.copy(clientId = it.trim()) }, label = { Text("Client Identifier") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = draft.username, onValueChange = { draft = draft.copy(username = it.trim()) }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(
                        value = draft.password,
                        onValueChange = { draft = draft.copy(password = it) },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                            }
                        }
                    )
                } else {
                    ViewRow("Broker Address", comm.mqttBrokerAddress, Icons.Default.Wifi)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    ViewRow("MQTT Port", comm.mqttPort.toString())
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    ViewRow("Client ID", comm.clientId)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    ViewRow("Username", if (comm.username.isNotBlank()) comm.username else "(Not Set)")
                }
            }
        } else {
            SettingsCard("DIRECT MODE CONFIGURATION") {
                if (isEditing) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Allowed Shuttle IP Addresses", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        
                        draft.allowedShuttleIps.forEach { ip ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ip, style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { draft = draft.copy(allowedShuttleIps = draft.allowedShuttleIps - ip) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove IP", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newIpInput,
                                onValueChange = { 
                                    newIpInput = it
                                    ipError = if (it.isNotBlank() && !it.matches(ipRegex)) "Invalid IP Format" else null
                                },
                                label = { Text("Add IP Address") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = ipError != null,
                                supportingText = ipError?.let { { Text(it, color = AppColors.Error) } }
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newIpInput.matches(ipRegex) && !draft.allowedShuttleIps.contains(newIpInput)) {
                                        draft = draft.copy(allowedShuttleIps = draft.allowedShuttleIps + newIpInput)
                                        newIpInput = ""
                                        ipError = null
                                    }
                                },
                                enabled = newIpInput.isNotBlank() && ipError == null,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = draft.directWebSocketPort.toString(),
                        onValueChange = { v ->
                            val n = v.toIntOrNull()
                            directWsPortError = if (n == null || n !in 1..65535) "Port must be 1–65535" else null
                            draft = draft.copy(directWebSocketPort = n ?: draft.directWebSocketPort)
                        },
                        label = { Text("WebSocket Port") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = directWsPortError != null,
                        supportingText = directWsPortError?.let { { Text(it, color = AppColors.Error) } },
                        singleLine = true
                    )
                } else {
                    ViewRow("Allowed IPs", if (comm.allowedShuttleIps.isEmpty()) "None configured" else "${comm.allowedShuttleIps.size} IPs configured", Icons.Default.NetworkPing)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    ViewRow("WebSocket Port", comm.directWebSocketPort.toString())
                }
            }
        }

        if (draft.mode == CommunicationMode.MQTT_BROKER) {
            SettingsCard("CONNECTION CONTROLS") {
                // Connection Controls
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { onDisconnect() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Disconnect")
                    }
                    Button(
                        onClick = { onTestConnection(if (isEditing) draft else comm) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Test Connection")
                    }
                }
            }
        }

        SettingsCard("TIMING & RELIABILITY") {
            if (isEditing) {
                OutlinedTextField(
                    value = draft.keepAliveSeconds.toString(),
                    onValueChange = { v ->
                        val n = v.toIntOrNull()
                        keepAliveError = if (n == null || n !in 5..3600) "Must be 5–3600 seconds" else null
                        draft = draft.copy(keepAliveSeconds = n ?: draft.keepAliveSeconds)
                    },
                    label = { Text("Keep Alive (seconds)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = keepAliveError != null,
                    supportingText = keepAliveError?.let { { Text(it, color = AppColors.Error) } },
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.heartbeatIntervalMs.toString(),
                    onValueChange = { v ->
                        val n = v.toIntOrNull()
                        heartbeatError = if (n == null || n !in 100..10000) "Must be 100–10000 ms" else null
                        draft = draft.copy(heartbeatIntervalMs = n ?: draft.heartbeatIntervalMs)
                    },
                    label = { Text("Heartbeat Interval (ms)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = heartbeatError != null,
                    supportingText = heartbeatError?.let { { Text(it, color = AppColors.Error) } },
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.communicationTimeoutMs.toString(),
                    onValueChange = { v ->
                        val n = v.toIntOrNull()
                        timeoutError = if (n == null || n !in 500..30000) "Must be 500–30000 ms" else null
                        draft = draft.copy(communicationTimeoutMs = n ?: draft.communicationTimeoutMs)
                    },
                    label = { Text("Communication Timeout (ms)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = timeoutError != null,
                    supportingText = timeoutError?.let { { Text(it, color = AppColors.Error) } },
                    singleLine = true
                )
                SwitchRow("Automatic Reconnect", "Retry connection on disconnect.", draft.autoReconnect, true) { draft = draft.copy(autoReconnect = it) }
            } else {
                ViewRow("Keep Alive", "${comm.keepAliveSeconds}s", Icons.Default.Timer)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Heartbeat", "${comm.heartbeatIntervalMs}ms", Icons.Default.FavoriteBorder)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Timeout", "${comm.communicationTimeoutMs}ms", Icons.Default.HourglassEmpty)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Auto Reconnect", if (comm.autoReconnect) "Enabled" else "Disabled", Icons.Default.Autorenew)
            }
        }

        if (draft.mode == CommunicationMode.MQTT_BROKER) {
            SettingsCard("COMMUNICATION DIAGNOSTICS") {
                ViewRow("Connection State", diagnostics.connectionState, Icons.Default.PowerSettingsNew)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Last Error", diagnostics.lastError, Icons.Default.ErrorOutline)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Reconnect Count", diagnostics.reconnectCount.toString(), Icons.Default.Repeat)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("MQTT Library", diagnostics.libraryVersion, Icons.Default.Code)
                
                Spacer(Modifier.height(8.dp))
                Text("Live Connection Log", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        diagnostics.logs.forEach { logLine ->
                            Text(
                                text = logLine,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        if (isEditing) {
            val hasError = portError != null || keepAliveError != null || heartbeatError != null || timeoutError != null || directWsPortError != null
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onRevertToDefault,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Revert to Default Settings")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Cancel")
                    }
                    Button(onClick = { if (!hasError) onSave(draft) }, enabled = !hasError, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Save")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. REPORT SETTINGS VIEW
// ---------------------------------------------------------------------------
@Composable
private fun ReportSettingsView(
    reports: ReportSettings,
    isEditing: Boolean,
    onSave: (ReportSettings) -> Unit,
    onCancel: () -> Unit,
    onRevertToDefault: () -> Unit
) {
    var draft by remember(reports) { mutableStateOf(reports) }
    LaunchedEffect(isEditing) { if (!isEditing) draft = reports }

    // Token labels available for naming convention
    val availableTokens = listOf("{ReportName}", "{Date}", "{Time}", "{ShuttleID}", "{UserID}", "{Shift}")

    val namingPreview = "${draft.reportNamingToken1}_${draft.reportNamingToken2}_${draft.reportNamingToken3}.${draft.defaultExportFormat.extension}"

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsCard("EXPORT & FORMAT") {
            if (isEditing) {
                SegmentedButtonRow(
                    options = ReportExportFormat.entries,
                    selected = draft.defaultExportFormat,
                    displayName = { it.displayName },
                    onSelect = { draft = draft.copy(defaultExportFormat = it) },
                    enabled = true,
                    icon = { format ->
                        when (format) {
                            ReportExportFormat.PDF -> Icons.Default.PictureAsPdf
                            ReportExportFormat.CSV -> Icons.Default.TableChart
                            ReportExportFormat.XLSX -> Icons.Default.GridOn
                            ReportExportFormat.JSON -> Icons.Default.DataObject
                        }
                    }
                )
                SwitchRow("Automatic Scheduled Reports", "Auto-generate reports at end of each shift.", draft.autoReportGeneration, true) { draft = draft.copy(autoReportGeneration = it) }
            } else {
                ViewRow("Export Format", reports.defaultExportFormat.displayName, Icons.Default.FilePresent)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Auto Report Generation", if (reports.autoReportGeneration) "Enabled" else "Disabled", Icons.Default.ScheduleSend)
            }
        }

        SettingsCard("NAMING CONVENTION") {
            if (isEditing) {
                Text("Select tokens for the report filename pattern:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                // Token 1, 2, 3
                SettingsDropdown("Token 1 (First Segment)", draft.reportNamingToken1, availableTokens, { it }, true) { draft = draft.copy(reportNamingToken1 = it) }
                SettingsDropdown("Token 2 (Second Segment)", draft.reportNamingToken2, availableTokens, { it }, true) { draft = draft.copy(reportNamingToken2 = it) }
                SettingsDropdown("Token 3 (Third Segment)", draft.reportNamingToken3, availableTokens, { it }, true) { draft = draft.copy(reportNamingToken3 = it) }
            } else {
                ViewRow("Naming Pattern", "${reports.reportNamingToken1}_${reports.reportNamingToken2}_${reports.reportNamingToken3}", Icons.Default.DriveFileRenameOutline)
            }
            // Live preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Preview, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Preview: $namingPreview", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        SettingsCard("STORAGE LOCATION") {
            if (isEditing) {
                // Preset path picker dialog
                var showPathPicker by remember { mutableStateOf(false) }
                val presetPaths = listOf("/var/wms/reports/export/", "/home/wms/reports/", "/opt/wms/data/reports/", "/tmp/wms_reports/")
                OutlinedTextField(
                    value = draft.reportStorageLocation,
                    onValueChange = { draft = draft.copy(reportStorageLocation = it) },
                    label = { Text("Report Storage Path") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val launcher = com.example.myapplication.shared.features.settings.components.rememberDirectoryPicker { uri ->
                            if (uri != null) {
                                draft = draft.copy(reportStorageLocation = uri)
                            }
                        }
                        IconButton(onClick = { launcher.launch() }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Pick Folder")
                        }
                    },
                    singleLine = true
                )

            } else {
                ViewRow("Storage Location", reports.reportStorageLocation, Icons.Default.Folder)
            }
        }

        SettingsCard("ANALYTICS & LOGGING") {
            if (isEditing) {
                SettingsDropdown("Log Level", draft.logLevel, LogLevel.entries, { it.displayName }, true) { draft = draft.copy(logLevel = it) }
                Text(draft.logLevel.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                SwitchRow("Analytics Data Collection", "Collect operational telemetry and performance metrics.", draft.analyticsEnabled, true) { draft = draft.copy(analyticsEnabled = it) }
            } else {
                ViewRow("Log Level", reports.logLevel.displayName, Icons.Default.BugReport)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Analytics", if (reports.analyticsEnabled) "Enabled" else "Disabled", Icons.Default.Analytics)
            }
        }

        if (isEditing) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onRevertToDefault,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Revert to Default Settings")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Cancel")
                    }
                    Button(onClick = { onSave(draft) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Save")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. BACKUP & RESTORE VIEW
// ---------------------------------------------------------------------------
@Composable
private fun BackupSettingsView(
    backup: BackupSettings,
    isProcessing: Boolean,
    isEditing: Boolean,
    onAutoBackupToggle: (Boolean) -> Unit,
    onBackupNow: (String) -> Unit,
    onRestore: (String) -> Unit,
    onRevertToDefault: () -> Unit
) {
    var selectedPath by remember(backup.backupLocation) { mutableStateOf(backup.backupLocation) }
    var showPathPicker by remember { mutableStateOf(false) }
    var restoreTarget by remember { mutableStateOf<BackupEntry?>(null) }

    val presetPaths = listOf("/var/wms/backups/sqlite/", "/home/wms/backups/", "/opt/wms/db_backups/", "/tmp/wms_backup/")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // ── Auto Backup ───────────────────────────────────────────────────────
        SettingsCard("AUTOMATIC BACKUP") {
            SwitchRow(
                "Nightly Auto-Backup",
                "Automatically backup the database every 24 hours.",
                backup.automaticBackup,
                true
            ) { onAutoBackupToggle(it) }
        }

        // ── Manual Backup ─────────────────────────────────────────────────────
        SettingsCard("MANUAL BACKUP") {
            // Destination path row
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Backup Destination", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(selectedPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                val backupLauncher = com.example.myapplication.shared.features.settings.components.rememberDirectoryPicker { uri ->
                    if (uri != null) {
                        selectedPath = uri
                    }
                }
                OutlinedButton(onClick = { backupLauncher.launch() }, shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Change", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { onBackupNow(selectedPath) },
                enabled = !isProcessing,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Creating Backup...")
                } else {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Backup Database Now")
                }
            }
        }

        // ── Backup History ────────────────────────────────────────────────────
        if (backup.backupHistory.isEmpty()) {
            SettingsCard("BACKUP HISTORY") {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("No backups yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            SettingsCard("BACKUP HISTORY") {
                backup.backupHistory.forEach { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status badge
                            Box(
                                modifier = Modifier.size(36.dp)
                                    .background(
                                        if (entry.status == "SUCCESS") AppColors.Success.copy(alpha = 0.12f) else AppColors.Error.copy(alpha = 0.12f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (entry.status == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (entry.status == "SUCCESS") AppColors.Success else AppColors.Error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.fileName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${entry.timestamp} · ${entry.sizeKb / 1024} MB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                            }
                            // Restore button
                            OutlinedButton(
                                onClick = { restoreTarget = entry },
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isProcessing,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Restore", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        if (isEditing) {
            OutlinedButton(
                onClick = onRevertToDefault,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Revert to Default Settings")
            }
        }
    }



    // Restore confirmation dialog
    restoreTarget?.let { entry ->
        AlertDialog(
            onDismissRequest = { restoreTarget = null },
            title = { Text("Confirm Database Restore", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("You are about to restore from:")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(entry.fileName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("Backed up: ${entry.timestamp}", style = MaterialTheme.typography.labelSmall)
                            Text("Size: ${entry.sizeKb / 1024} MB", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text("⚠ All data added after this backup will be overwritten. This cannot be undone.", style = MaterialTheme.typography.bodySmall, color = AppColors.Error)
                }
            },
            confirmButton = {
                Button(
                    onClick = { onRestore(entry.id); restoreTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)
                ) { Text("Confirm Restore") }
            },
            dismissButton = {
                TextButton(onClick = { restoreTarget = null }) { Text("Cancel") }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// 5. ABOUT & SYSTEM VIEW (READ-ONLY)
// ---------------------------------------------------------------------------
@Composable
private fun AboutSystemView(systemInfo: SystemInfo) {
    var showLicensesDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsCard("APPLICATION BUILD INFORMATION") {
            ViewRow("Application Version", systemInfo.appVersion, Icons.Default.Info)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ViewRow("Build Number", systemInfo.buildNumber)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ViewRow("Target Environment", systemInfo.targetEnvironment)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ViewRow("Storage Usage", systemInfo.storageUsage, Icons.Default.Storage)
        }

        SettingsCard("COMPANY & SUPPORT") {
            Text("Company Information:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(systemInfo.companyInformation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            Text("Contact Information:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(systemInfo.contactInformation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showLicensesDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("View Open Source Licenses")
            }
        }
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Licenses", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    systemInfo.openSourceLicenses.forEach { lic ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(lic, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLicensesDialog = false }) { Text("Close") } }
        )
    }
}
