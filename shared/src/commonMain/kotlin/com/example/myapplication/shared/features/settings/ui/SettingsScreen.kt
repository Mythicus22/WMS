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
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
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
                    onRequestEdit = { isEditing = true }
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
                    onCancel = { isEditing = false }
                )
                SettingsTab.REPORTS -> ReportSettingsView(
                    reports = state.settings.reports,
                    isEditing = isEditing,
                    onSave = { updated ->
                        viewModel.onEvent(SettingsUiEvent.UpdateReports(updated))
                        isEditing = false
                    },
                    onCancel = { isEditing = false }
                )
                SettingsTab.BACKUP -> BackupSettingsView(
                    backup = state.settings.backup,
                    isProcessing = state.isProcessing,
                    onAutoBackupToggle = { enabled ->
                        viewModel.onEvent(SettingsUiEvent.UpdateBackup(
                            state.settings.backup.copy(automaticBackup = enabled)
                        ))
                    },
                    onBackupNow = { path -> viewModel.onEvent(SettingsUiEvent.PerformBackup(path)) },
                    onRestore = { backupId -> viewModel.onEvent(SettingsUiEvent.PerformRestore(backupId)) }
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
// SECTION HEADER
// ---------------------------------------------------------------------------
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

// ---------------------------------------------------------------------------
// SETTINGS CARD WRAPPER
// ---------------------------------------------------------------------------
@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
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
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
    onRequestEdit: () -> Unit
) {
    // Draft state (only mutated when editing)
    var draft by remember(general) { mutableStateOf(general) }
    // Reset draft on cancel
    LaunchedEffect(isEditing) { if (!isEditing) draft = general }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // ── Theme Toggle (always active – immediate effect) ──────────────────
        SectionHeader("APPLICATION THEME")
        Card(
            modifier = Modifier.fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Current Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(draft.themeMode.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemeToggleButton(mode = ThemeMode.LIGHT, icon = Icons.Default.LightMode, label = "Light", selected = draft.themeMode == ThemeMode.LIGHT) {
                        draft = draft.copy(themeMode = ThemeMode.LIGHT)
                        onSave(draft.copy(themeMode = ThemeMode.LIGHT))
                    }
                    ThemeToggleButton(mode = ThemeMode.DARK, icon = Icons.Default.DarkMode, label = "Dark", selected = draft.themeMode == ThemeMode.DARK) {
                        draft = draft.copy(themeMode = ThemeMode.DARK)
                        onSave(draft.copy(themeMode = ThemeMode.DARK))
                    }
                    ThemeToggleButton(mode = ThemeMode.SYSTEM, icon = Icons.Default.SettingsSystemDaydream, label = "Auto", selected = draft.themeMode == ThemeMode.SYSTEM) {
                        draft = draft.copy(themeMode = ThemeMode.SYSTEM)
                        onSave(draft.copy(themeMode = ThemeMode.SYSTEM))
                    }
                }
            }
        }

        // ── Warehouse & Company ──────────────────────────────────────────────
        SectionHeader("WAREHOUSE & COMPANY")
        SettingsCard {
            if (isEditing) {
                OutlinedTextField(value = draft.warehouseName, onValueChange = { draft = draft.copy(warehouseName = it) }, label = { Text("Warehouse Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = draft.warehouseCode, onValueChange = { draft = draft.copy(warehouseCode = it) }, label = { Text("Warehouse Code (max 20 chars)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = draft.companyName, onValueChange = { draft = draft.copy(companyName = it) }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            } else {
                ViewRow("Warehouse Name", general.warehouseName, Icons.Default.Warehouse)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Warehouse Code", general.warehouseCode)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Company Name", general.companyName, Icons.Default.Business)
            }
        }

        // ── Language ─────────────────────────────────────────────────────────
        SectionHeader("LANGUAGE PREFERENCE")
        SettingsCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AppLanguage.entries.forEach { lang ->
                    val isSel = draft.language == lang
                    OutlinedButton(
                        onClick = {
                            draft = draft.copy(language = lang)
                            if (!isEditing) onSave(draft)
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = isEditing || draft.language == lang,
                        colors = if (isSel) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(lang.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        // ── Date & Time ───────────────────────────────────────────────────────
        SectionHeader("DATE & TIME FORMAT")
        SettingsCard {
            if (isEditing) {
                SettingsDropdown(label = "Date Format", selected = draft.dateFormat, options = DateFormat.entries, displayName = { it.displayName }, enabled = true) { draft = draft.copy(dateFormat = it) }
                SettingsDropdown(label = "Time Format", selected = draft.timeFormat, options = TimeFormat.entries, displayName = { it.displayName }, enabled = true) { draft = draft.copy(timeFormat = it) }
            } else {
                ViewRow("Date Format", general.dateFormat.displayName, Icons.Default.DateRange)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Time Format", general.timeFormat.displayName)
            }
            // Device time refresh (always shown)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Device Date & Time", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(general.currentDateTimePreview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { /* timestamp refresh — future: use kotlinx-datetime */ },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Refresh", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // ── Display Preferences ───────────────────────────────────────────────
        SectionHeader("DISPLAY PREFERENCES")
        SettingsCard {
            if (isEditing) {
                SettingsDropdown(label = "Default Measurement Units", selected = draft.defaultUnits, options = MeasurementUnit.entries, displayName = { it.displayName }, enabled = true) { draft = draft.copy(defaultUnits = it) }
                SettingsDropdown(label = "Font Family", selected = draft.fontFamily, options = AppFontFamily.entries, displayName = { it.displayName }, enabled = true) { draft = draft.copy(fontFamily = it) }
                // Font Size — segmented buttons
                Column {
                    Text("Font Size", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        AppFontSize.entries.forEach { fs ->
                            val sel = draft.fontSize == fs
                            OutlinedButton(
                                onClick = { draft = draft.copy(fontSize = fs) },
                                shape = RoundedCornerShape(8.dp),
                                colors = if (sel) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) else ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) { Text(fs.displayName.substringBefore(" "), style = MaterialTheme.typography.labelSmall, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) }
                        }
                    }
                }
            } else {
                ViewRow("Measurement Units", general.defaultUnits.displayName, Icons.Default.Straighten)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Font Family", general.fontFamily.displayName, Icons.Default.TextFields)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Font Size", general.fontSize.displayName)
            }
        }

        // Save / Cancel buttons when editing
        if (isEditing) {
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
    onCancel: () -> Unit
) {
    var draft by remember(comm) { mutableStateOf(comm) }
    LaunchedEffect(isEditing) { if (!isEditing) draft = comm }

    var portError by remember { mutableStateOf<String?>(null) }
    var keepAliveError by remember { mutableStateOf<String?>(null) }
    var heartbeatError by remember { mutableStateOf<String?>(null) }
    var timeoutError by remember { mutableStateOf<String?>(null) }
    
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader("MQTT BROKER CONFIGURATION")
        SettingsCard {
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
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("MQTT Port", comm.mqttPort.toString())
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Client ID", comm.clientId)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Username", if (comm.username.isNotBlank()) comm.username else "(Not Set)")
            }
            
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

        SectionHeader("TIMING & RELIABILITY")
        SettingsCard {
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
                ViewRow("Keep Alive", "${comm.keepAliveSeconds}s")
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Heartbeat", "${comm.heartbeatIntervalMs}ms")
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Timeout", "${comm.communicationTimeoutMs}ms")
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Auto Reconnect", if (comm.autoReconnect) "Enabled" else "Disabled")
            }
        }

        SectionHeader("COMMUNICATION DIAGNOSTICS")
        SettingsCard {
            ViewRow("Connection State", diagnostics.connectionState)
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ViewRow("Last Error", diagnostics.lastError)
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ViewRow("Reconnect Count", diagnostics.reconnectCount.toString())
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ViewRow("MQTT Library", diagnostics.libraryVersion)
            
            Spacer(Modifier.height(8.dp))
            Text("Live Connection Log", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                // Using LazyColumn would be better, but simpler column for scrolling
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

        if (isEditing) {
            val hasError = portError != null || keepAliveError != null || heartbeatError != null || timeoutError != null
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

// ---------------------------------------------------------------------------
// 3. REPORT SETTINGS VIEW
// ---------------------------------------------------------------------------
@Composable
private fun ReportSettingsView(
    reports: ReportSettings,
    isEditing: Boolean,
    onSave: (ReportSettings) -> Unit,
    onCancel: () -> Unit
) {
    var draft by remember(reports) { mutableStateOf(reports) }
    LaunchedEffect(isEditing) { if (!isEditing) draft = reports }

    // Token labels available for naming convention
    val availableTokens = listOf("{ReportName}", "{Date}", "{Time}", "{ShuttleID}", "{UserID}", "{Shift}")

    val namingPreview = "${draft.reportNamingToken1}_${draft.reportNamingToken2}_${draft.reportNamingToken3}.${draft.defaultExportFormat.extension}"

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader("EXPORT & FORMAT")
        SettingsCard {
            if (isEditing) {
                SettingsDropdown("Default Export Format", draft.defaultExportFormat, ReportExportFormat.entries, { it.displayName }, true) { draft = draft.copy(defaultExportFormat = it) }
                SwitchRow("Automatic Scheduled Reports", "Auto-generate reports at end of each shift.", draft.autoReportGeneration, true) { draft = draft.copy(autoReportGeneration = it) }
            } else {
                ViewRow("Export Format", reports.defaultExportFormat.displayName, Icons.Default.FilePresent)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Auto Report Generation", if (reports.autoReportGeneration) "Enabled" else "Disabled")
            }
        }

        SectionHeader("NAMING CONVENTION")
        SettingsCard {
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

        SectionHeader("STORAGE LOCATION")
        SettingsCard {
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
                        IconButton(onClick = { showPathPicker = true }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Pick Folder")
                        }
                    },
                    singleLine = true
                )
                if (showPathPicker) {
                    AlertDialog(
                        onDismissRequest = { showPathPicker = false },
                        title = { Text("Select Report Storage Folder", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                presetPaths.forEach { path ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            draft = draft.copy(reportStorageLocation = path)
                                            showPathPicker = false
                                        }.padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(path, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { showPathPicker = false }) { Text("Cancel") } }
                    )
                }
            } else {
                ViewRow("Storage Location", reports.reportStorageLocation, Icons.Default.Folder)
            }
        }

        SectionHeader("ANALYTICS & LOGGING")
        SettingsCard {
            if (isEditing) {
                SettingsDropdown("Log Level", draft.logLevel, LogLevel.entries, { it.displayName }, true) { draft = draft.copy(logLevel = it) }
                Text(draft.logLevel.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                SwitchRow("Analytics Data Collection", "Collect operational telemetry and performance metrics.", draft.analyticsEnabled, true) { draft = draft.copy(analyticsEnabled = it) }
            } else {
                ViewRow("Log Level", reports.logLevel.displayName, Icons.Default.BugReport)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ViewRow("Analytics", if (reports.analyticsEnabled) "Enabled" else "Disabled")
            }
        }

        if (isEditing) {
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

// ---------------------------------------------------------------------------
// 4. BACKUP & RESTORE VIEW
// ---------------------------------------------------------------------------
@Composable
private fun BackupSettingsView(
    backup: BackupSettings,
    isProcessing: Boolean,
    onAutoBackupToggle: (Boolean) -> Unit,
    onBackupNow: (String) -> Unit,
    onRestore: (String) -> Unit
) {
    var selectedPath by remember(backup.backupLocation) { mutableStateOf(backup.backupLocation) }
    var showPathPicker by remember { mutableStateOf(false) }
    var restoreTarget by remember { mutableStateOf<BackupEntry?>(null) }

    val presetPaths = listOf("/var/wms/backups/sqlite/", "/home/wms/backups/", "/opt/wms/db_backups/", "/tmp/wms_backup/")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // ── Auto Backup ───────────────────────────────────────────────────────
        SectionHeader("AUTOMATIC BACKUP")
        SettingsCard {
            SwitchRow(
                "Nightly Auto-Backup",
                "Automatically backup the database every 24 hours.",
                backup.automaticBackup,
                true
            ) { onAutoBackupToggle(it) }
        }

        // ── Manual Backup ─────────────────────────────────────────────────────
        SectionHeader("MANUAL BACKUP")
        SettingsCard {
            // Destination path row
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Backup Destination", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(selectedPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                OutlinedButton(onClick = { showPathPicker = true }, shape = RoundedCornerShape(8.dp)) {
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
        SectionHeader("BACKUP HISTORY")
        if (backup.backupHistory.isEmpty()) {
            SettingsCard {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("No backups yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
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

    // Path picker dialog
    if (showPathPicker) {
        AlertDialog(
            onDismissRequest = { showPathPicker = false },
            title = { Text("Select Backup Destination", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetPaths.forEach { path ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedPath = path
                                showPathPicker = false
                            }.padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(path, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPathPicker = false }) { Text("Cancel") } }
        )
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
        SectionHeader("APPLICATION BUILD INFORMATION")
        SettingsCard {
            ViewRow("Application Version", systemInfo.appVersion, Icons.Default.Info)
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ViewRow("Build Number", systemInfo.buildNumber)
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ViewRow("Target Environment", systemInfo.targetEnvironment)
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ViewRow("Storage Usage", systemInfo.storageUsage, Icons.Default.Storage)
        }

        SectionHeader("COMPANY & SUPPORT")
        SettingsCard {
            Text("Company Information:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(systemInfo.companyInformation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
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
