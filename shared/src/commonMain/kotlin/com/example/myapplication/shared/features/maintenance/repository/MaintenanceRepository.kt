package com.example.myapplication.shared.features.maintenance.repository

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.maintenance.model.TestDefinition
import com.example.myapplication.shared.features.maintenance.model.TestExecutionState
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun getAllTestDefinitions(): List<TestDefinition>
    fun getTestDefinition(testId: String): TestDefinition?
    fun observeTestExecution(shuttleId: String, testId: String): Flow<TestExecutionState>
    suspend fun runTest(shuttleId: String, testId: String): Result<Unit>
    suspend fun resetTest(shuttleId: String, testId: String): Result<Unit>
}
