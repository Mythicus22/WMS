package com.example.myapplication.shared.features.reports.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.features.reports.domain.*
import com.example.myapplication.shared.features.reports.model.*
import com.example.myapplication.shared.features.settings.model.ReportExportFormat
import com.example.myapplication.shared.features.settings.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ReportsMainTab(val title: String) { REPORTS("Reports"), ANALYTICS("Analytics") }

data class ReportsUiState(
    val selectedTab: ReportsMainTab = ReportsMainTab.REPORTS,
    val selectedCategory: ReportCategory = ReportCategory.SUMMARY,
    val filter: ReportFilter = ReportFilter(),
    val shuttleId: String? = null,
    val shuttleName: String = "All Shuttles",
    // Reports data
    val summary: List<SummaryData> = emptyList(),
    val storeOps: List<OperationRecord> = emptyList(),
    val retrieveOps: List<OperationRecord> = emptyList(),
    val tasks: List<TaskRecord> = emptyList(),
    val missions: List<MissionRecord> = emptyList(),
    val utilization: List<UtilizationRecord> = emptyList(),
    val battery: List<BatteryRecord> = emptyList(),
    val motorRuntime: List<MotorRuntimeRecord> = emptyList(),
    val faults: List<FaultRecord> = emptyList(),
    val maintenance: List<MaintenanceRecord> = emptyList(),
    val productivity: List<ProductivityRecord> = emptyList(),
    // Analytics
    val analytics: AnalyticsDashboard = AnalyticsDashboard(),
    // Export state
    val isExporting: Boolean = false,
    val exportResult: ExportResult? = null,
    val statusMessage: String? = null,
    val isLoadingReports: Boolean = false,
    val isLoadingAnalytics: Boolean = false
)

sealed interface ReportsUiEvent {
    data class SelectTab(val tab: ReportsMainTab) : ReportsUiEvent
    data class SelectCategory(val category: ReportCategory) : ReportsUiEvent
    data class UpdateFilter(val filter: ReportFilter) : ReportsUiEvent
    object RequestExport : ReportsUiEvent
    object DismissStatus : ReportsUiEvent
    object RefreshData : ReportsUiEvent
}

class ReportsViewModel(
    private val shuttleId: String?,
    private val shuttleName: String,
    private val getSummary: GetSummaryUseCase,
    private val getStoreOps: GetStoreOperationsUseCase,
    private val getRetrieveOps: GetRetrieveOperationsUseCase,
    private val getTasks: GetTaskHistoryUseCase,
    private val getMissions: GetMissionHistoryUseCase,
    private val getUtilization: GetShuttleUtilizationUseCase,
    private val getBattery: GetBatteryReportUseCase,
    private val getMotor: GetMotorRuntimeUseCase,
    private val getFaults: GetFaultHistoryUseCase,
    private val getMaintenance: GetMaintenanceHistoryUseCase,
    private val getProductivity: GetProductivityUseCase,
    private val getAnalytics: GetAnalyticsDashboardUseCase,
    private val exportReport: ExportReportUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState(shuttleId = shuttleId, shuttleName = shuttleName))
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        val filter = _uiState.value.filter
        _uiState.update { it.copy(isLoadingReports = true, isLoadingAnalytics = true) }

        viewModelScope.launch {
            try {
                kotlinx.coroutines.withTimeout(5000) {
                    launch { getSummary(shuttleId, filter).collect { data -> _uiState.update { it.copy(summary = data) } } }
                    launch { getStoreOps(shuttleId, filter).collect { data -> _uiState.update { it.copy(storeOps = data) } } }
                    launch { getRetrieveOps(shuttleId, filter).collect { data -> _uiState.update { it.copy(retrieveOps = data) } } }
                    launch { getTasks(shuttleId, filter).collect { data -> _uiState.update { it.copy(tasks = data) } } }
                    launch { getMissions(shuttleId, filter).collect { data -> _uiState.update { it.copy(missions = data) } } }
                    launch { getUtilization(shuttleId, filter).collect { data -> _uiState.update { it.copy(utilization = data) } } }
                    launch { getBattery(shuttleId, filter).collect { data -> _uiState.update { it.copy(battery = data) } } }
                    launch { getMotor(shuttleId, filter).collect { data -> _uiState.update { it.copy(motorRuntime = data) } } }
                    launch { getFaults(shuttleId, filter).collect { data -> _uiState.update { it.copy(faults = data) } } }
                    launch { getMaintenance(shuttleId, filter).collect { data -> _uiState.update { it.copy(maintenance = data) } } }
                    launch { getProductivity(shuttleId, filter).collect { data ->
                        _uiState.update { it.copy(productivity = data) }
                    } }
                    launch { getAnalytics(shuttleId, filter).collect { data ->
                        _uiState.update { it.copy(analytics = data) }
                    } }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _uiState.update { it.copy(statusMessage = "Loading timed out. Please check the database connection.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Error loading reports: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoadingReports = false, isLoadingAnalytics = false) }
            }
        }
    }

    fun onEvent(event: ReportsUiEvent) {
        when (event) {
            is ReportsUiEvent.SelectTab -> _uiState.update { it.copy(selectedTab = event.tab) }
            is ReportsUiEvent.SelectCategory -> _uiState.update { it.copy(selectedCategory = event.category) }
            is ReportsUiEvent.UpdateFilter -> {
                _uiState.update { it.copy(filter = event.filter) }
                loadAll()
            }
            ReportsUiEvent.RequestExport -> performExport()
            ReportsUiEvent.DismissStatus -> _uiState.update { it.copy(statusMessage = null, exportResult = null) }
            ReportsUiEvent.RefreshData -> loadAll()
        }
    }

    private fun performExport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val settings = settingsRepository.getCurrentSettings()
            val exportFormat = when (settings.reports.defaultExportFormat) {
                ReportExportFormat.PDF  -> ExportFormat.PDF
                ReportExportFormat.XLSX -> ExportFormat.XLSX
                ReportExportFormat.CSV  -> ExportFormat.CSV
                else -> ExportFormat.CSV
            }
            val state = _uiState.value
            val config = ExportConfig(
                format = exportFormat,
                shuttleId = shuttleId ?: "ALL",
                shuttleName = shuttleName,
                startDate = state.filter.startDate,
                endDate = state.filter.endDate,
                outputDirectory = settings.reports.reportStorageLocation
            )
            val result = exportReport(config, state.filter)
            _uiState.update { it.copy(isExporting = false, exportResult = result,
                statusMessage = if (result.success) "Exported: ${result.fileName}" else "Export failed: ${result.message}") }
        }
    }
}
