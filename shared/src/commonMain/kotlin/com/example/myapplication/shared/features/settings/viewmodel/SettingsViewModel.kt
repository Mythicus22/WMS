package com.example.myapplication.shared.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.domain.model.SettingPermission
import com.example.myapplication.shared.domain.model.User
import com.example.myapplication.shared.domain.usecase.GetCurrentUserUseCase
import com.example.myapplication.shared.features.settings.domain.BackupDatabaseUseCase
import com.example.myapplication.shared.features.settings.domain.GetSettingsUseCase
import com.example.myapplication.shared.features.settings.domain.RestoreDatabaseUseCase
import com.example.myapplication.shared.features.settings.domain.UpdateBackupSettingsUseCase
import com.example.myapplication.shared.features.settings.domain.UpdateCommunicationSettingsUseCase
import com.example.myapplication.shared.features.settings.domain.UpdateGeneralSettingsUseCase
import com.example.myapplication.shared.communication.service.CommunicationService
import com.example.myapplication.shared.features.settings.domain.UpdateReportSettingsUseCase
import com.example.myapplication.shared.features.settings.model.AppSettings
import com.example.myapplication.shared.features.settings.model.BackupSettings
import com.example.myapplication.shared.features.settings.model.CommunicationSettings
import com.example.myapplication.shared.features.settings.model.GeneralSettings
import com.example.myapplication.shared.features.settings.model.ReportSettings
import com.example.myapplication.shared.features.settings.model.ConnectionDiagnostics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Tab definitions — each tab maps to a SettingPermission (null = all users)
// ---------------------------------------------------------------------------
enum class SettingsTab(val title: String, val requiredPermission: SettingPermission?) {
    GENERAL("General", SettingPermission.SYSTEM_CONFIGURATION),
    COMMUNICATION("Communication", SettingPermission.COMMUNICATION_SETTINGS),
    REPORTS("Reports", SettingPermission.REPORT_CONFIGURATION),
    BACKUP("Backup & Restore", SettingPermission.BACKUP_SETTINGS),
    ABOUT("About & System", null) // Always visible
}

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------
data class SettingsUiState(
    val currentUser: User? = null,
    val availableTabs: List<SettingsTab> = listOf(SettingsTab.ABOUT),
    val selectedTab: SettingsTab = SettingsTab.GENERAL,
    val settings: AppSettings = AppSettings(),
    val isProcessing: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val diagnostics: com.example.myapplication.shared.features.settings.model.ConnectionDiagnostics = com.example.myapplication.shared.features.settings.model.ConnectionDiagnostics()
)

// ---------------------------------------------------------------------------
// Events
// ---------------------------------------------------------------------------
sealed interface SettingsUiEvent {
    data class SelectTab(val tab: SettingsTab) : SettingsUiEvent
    data class UpdateGeneral(val general: GeneralSettings) : SettingsUiEvent
    data class UpdateCommunication(val communication: CommunicationSettings) : SettingsUiEvent
    data class UpdateReports(val reports: ReportSettings) : SettingsUiEvent
    data class UpdateBackup(val backup: BackupSettings) : SettingsUiEvent
    data class PerformBackup(val destinationPath: String) : SettingsUiEvent
    data class PerformRestore(val backupId: String) : SettingsUiEvent
    data class OnTestConnection(val comm: CommunicationSettings) : SettingsUiEvent
    object OnDisconnect : SettingsUiEvent
    object DismissStatusMessage : SettingsUiEvent
    object DismissErrorMessage : SettingsUiEvent
}

// ---------------------------------------------------------------------------
// Validation helpers
// ---------------------------------------------------------------------------
object SettingsValidator {

    data class ValidationResult(val isValid: Boolean, val errors: Map<String, String> = emptyMap())

    fun validateGeneral(general: GeneralSettings): ValidationResult {
        val errors = mutableMapOf<String, String>()
        if (general.warehouseName.isBlank()) errors["warehouseName"] = "Warehouse name is required."
        if (general.warehouseCode.isBlank()) errors["warehouseCode"] = "Warehouse code is required."
        if (general.warehouseCode.length > 20) errors["warehouseCode"] = "Warehouse code must be ≤ 20 characters."
        if (general.companyName.isBlank()) errors["companyName"] = "Company name is required."
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateCommunication(comm: CommunicationSettings): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        val ipRegex = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$".toRegex()
        val hostRegex = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()
        if (comm.mqttBrokerAddress.isBlank()) {
            errors["mqttBrokerAddress"] = "Broker address is required."
        } else if (!comm.mqttBrokerAddress.matches(ipRegex) && !comm.mqttBrokerAddress.matches(hostRegex) && comm.mqttBrokerAddress != "localhost") {
            errors["mqttBrokerAddress"] = "Must be a valid IP address or hostname."
        }

        if (comm.mqttPort !in 1..65535) errors["mqttPort"] = "Port must be between 1 and 65535."
        if (comm.clientId.isBlank()) errors["clientId"] = "Client ID is required."
        if (comm.clientId.contains(" ")) errors["clientId"] = "Client ID must not contain spaces."
        if (comm.keepAliveSeconds < 5 || comm.keepAliveSeconds > 3600) errors["keepAliveSeconds"] = "Keep alive must be 5–3600 seconds."
        if (comm.heartbeatIntervalMs < 100 || comm.heartbeatIntervalMs > 10000) errors["heartbeatIntervalMs"] = "Heartbeat must be 100–10000 ms."
        if (comm.communicationTimeoutMs < 500 || comm.communicationTimeoutMs > 30000) errors["communicationTimeoutMs"] = "Timeout must be 500–30000 ms."
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateReports(reports: ReportSettings): ValidationResult {
        val errors = mutableMapOf<String, String>()
        if (reports.reportStorageLocation.isBlank()) errors["reportStorageLocation"] = "Storage location is required."
        return ValidationResult(errors.isEmpty(), errors)
    }
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------
class SettingsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateGeneralSettingsUseCase: UpdateGeneralSettingsUseCase,
    private val updateCommunicationSettingsUseCase: UpdateCommunicationSettingsUseCase,
    private val updateReportSettingsUseCase: UpdateReportSettingsUseCase,
    private val updateBackupSettingsUseCase: UpdateBackupSettingsUseCase,
    private val backupDatabaseUseCase: BackupDatabaseUseCase,
    private val restoreDatabaseUseCase: RestoreDatabaseUseCase,
    private val communicationService: CommunicationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
        observeSettings()
        observeDiagnostics()
    }

    private fun observeDiagnostics() {
        kotlinx.coroutines.flow.combine<com.example.myapplication.shared.communication.service.ConnectionState, List<String>, ConnectionDiagnostics>(
            communicationService.connectionState,
            communicationService.diagnosticsLog
        ) { state, logs ->
            ConnectionDiagnostics(
                connectionState = state.name,
                logs = logs
            )
        }.onEach { diag ->
            _uiState.update { it.copy(diagnostics = diag) }
        }.launchIn(viewModelScope)
    }

    private fun observeCurrentUser() {
        getCurrentUserUseCase.currentUser.onEach { user ->
            val visibleTabs = SettingsTab.entries.filter { tab ->
                val perm = tab.requiredPermission
                when {
                    perm == null -> true               // About & System always visible
                    user?.isAdmin == true -> true      // Admin sees all
                    else -> user?.grantedSettings?.contains(perm) == true
                }
            }
            _uiState.update { curr ->
                val initialTab = if (visibleTabs.contains(curr.selectedTab)) curr.selectedTab
                                 else visibleTabs.firstOrNull() ?: SettingsTab.ABOUT
                curr.copy(currentUser = user, availableTabs = visibleTabs, selectedTab = initialTab)
            }
        }.launchIn(viewModelScope)
    }

    private fun observeSettings() {
        getSettingsUseCase().onEach { settingsObj ->
            _uiState.update { curr -> curr.copy(settings = settingsObj) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.SelectTab -> {
                _uiState.update { curr -> curr.copy(selectedTab = event.tab) }
            }

            is SettingsUiEvent.UpdateGeneral -> {
                val validation = SettingsValidator.validateGeneral(event.general)
                if (!validation.isValid) {
                    val msg = validation.errors.values.joinToString("\n")
                    _uiState.update { curr -> curr.copy(errorMessage = msg) }
                    return
                }
                viewModelScope.launch {
                    updateGeneralSettingsUseCase(event.general)
                    _uiState.update { curr -> curr.copy(statusMessage = "General settings saved successfully.") }
                }
            }

            is SettingsUiEvent.UpdateCommunication -> {
                val validation = SettingsValidator.validateCommunication(event.communication)
                if (!validation.isValid) {
                    val msg = validation.errors.values.joinToString("\n")
                    _uiState.update { curr -> curr.copy(errorMessage = msg) }
                    return
                }
                viewModelScope.launch {
                    updateCommunicationSettingsUseCase(event.communication)
                    communicationService.connect(event.communication)
                    _uiState.update { curr -> curr.copy(statusMessage = "Communication settings saved.") }
                }
            }

            is SettingsUiEvent.UpdateReports -> {
                val validation = SettingsValidator.validateReports(event.reports)
                if (!validation.isValid) {
                    val msg = validation.errors.values.joinToString("\n")
                    _uiState.update { curr -> curr.copy(errorMessage = msg) }
                    return
                }
                viewModelScope.launch {
                    updateReportSettingsUseCase(event.reports)
                    _uiState.update { curr -> curr.copy(statusMessage = "Report preferences saved.") }
                }
            }

            is SettingsUiEvent.UpdateBackup -> {
                viewModelScope.launch {
                    updateBackupSettingsUseCase(event.backup)
                    _uiState.update { curr -> curr.copy(statusMessage = "Backup preferences saved.") }
                }
            }

            is SettingsUiEvent.PerformBackup -> {
                viewModelScope.launch {
                    _uiState.update { curr -> curr.copy(isProcessing = true) }
                    when (val res = backupDatabaseUseCase(event.destinationPath)) {
                        is Result.Success -> _uiState.update { curr -> curr.copy(isProcessing = false, statusMessage = res.data) }
                        is Result.Error -> _uiState.update { curr -> curr.copy(isProcessing = false, errorMessage = "Backup failed: ${res.exception.message}") }
                        is Result.Loading -> {}
                    }
                }
            }

            is SettingsUiEvent.PerformRestore -> {
                viewModelScope.launch {
                    _uiState.update { curr -> curr.copy(isProcessing = true) }
                    when (val res = restoreDatabaseUseCase(event.backupId)) {
                        is Result.Success -> _uiState.update { curr -> curr.copy(isProcessing = false, statusMessage = res.data) }
                        is Result.Error -> _uiState.update { curr -> curr.copy(isProcessing = false, errorMessage = "Restore failed: ${res.exception.message}") }
                        is Result.Loading -> {}
                    }
                }
            }

            is SettingsUiEvent.OnTestConnection -> {
                viewModelScope.launch {
                    communicationService.connect(event.comm)
                }
            }

            is SettingsUiEvent.OnDisconnect -> {
                viewModelScope.launch {
                    communicationService.disconnect()
                }
            }

            SettingsUiEvent.DismissStatusMessage -> {
                _uiState.update { curr -> curr.copy(statusMessage = null) }
            }

            SettingsUiEvent.DismissErrorMessage -> {
                _uiState.update { curr -> curr.copy(errorMessage = null) }
            }
        }
    }
}
