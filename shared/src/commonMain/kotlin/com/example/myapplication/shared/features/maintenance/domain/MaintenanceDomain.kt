package com.example.myapplication.shared.features.maintenance.domain

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.maintenance.model.TestDefinition
import com.example.myapplication.shared.features.maintenance.model.TestExecutionState
import com.example.myapplication.shared.features.maintenance.repository.MaintenanceRepository
import com.example.myapplication.shared.features.device.model.DiscoveredDevice
import com.example.myapplication.shared.features.device.repository.DiscoveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetEnabledShuttlesForMaintenanceUseCase(
    private val discoveryRepository: DiscoveryRepository
) {
    operator fun invoke(): Flow<List<DiscoveredDevice>> {
        return discoveryRepository.getAllDevices().map { shuttles ->
            shuttles.filter { it.status == "ONLINE" }
        }
    }
}

class GetMaintenanceTestsUseCase(
    private val maintenanceRepository: MaintenanceRepository
) {
    operator fun invoke(): List<TestDefinition> {
        return maintenanceRepository.getAllTestDefinitions()
    }
}

class GetTestDetailUseCase(
    private val maintenanceRepository: MaintenanceRepository
) {
    operator fun invoke(testId: String): TestDefinition? {
        return maintenanceRepository.getTestDefinition(testId)
    }

    fun observeExecution(shuttleId: String, testId: String): Flow<TestExecutionState> {
        return maintenanceRepository.observeTestExecution(shuttleId, testId)
    }
}

class RunMaintenanceTestUseCase(
    private val maintenanceRepository: MaintenanceRepository
) {
    suspend operator fun invoke(shuttleId: String, testId: String): Result<Unit> {
        return maintenanceRepository.runTest(shuttleId, testId)
    }
}

class ResetMaintenanceTestUseCase(
    private val maintenanceRepository: MaintenanceRepository
) {
    suspend operator fun invoke(shuttleId: String, testId: String): Result<Unit> {
        return maintenanceRepository.resetTest(shuttleId, testId)
    }
}
