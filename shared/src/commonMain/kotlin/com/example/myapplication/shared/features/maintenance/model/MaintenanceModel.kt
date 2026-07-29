package com.example.myapplication.shared.features.maintenance.model

import kotlinx.serialization.Serializable

enum class TestCategory(val displayName: String) {
    SENSORS("Optical & Distance Sensors"),
    MOTORS("Drive & Lift Actuation Motors"),
    ELECTRICAL_RELAYS("Relays & Power Switches"),
    COMMUNICATION("CANbus & Radio RF"),
    BATTERY_POWER("Battery & Power Management"),
    SAFETY_ESTOP("Safety & Emergency Systems")
}

enum class TestStatus(val displayName: String) {
    IDLE("Idle - Not Started"),
    RUNNING("Test Running..."),
    PASSED("TEST PASSED"),
    FAILED("TEST FAILED"),
    WARNING("PASSED WITH WARNINGS")
}

@Serializable
data class TestParameter(
    val label: String,
    val value: String,
    val unit: String = "",
    val isNormal: Boolean = true
)

@Serializable
data class TestDefinition(
    val id: String,
    val name: String,
    val category: TestCategory,
    val objective: String,
    val description: String,
    val requiredPreconditions: List<String>,
    val expectedPlcResponse: String,
    val expectedSensorResponse: String,
    val expectedBehaviour: String,
    val troubleshootingGuide: List<String>
)

@Serializable
data class TestExecutionState(
    val testId: String,
    val shuttleId: String,
    val status: TestStatus = TestStatus.IDLE,
    val progress: Float = 0.0f,
    val currentPlcValues: List<TestParameter> = emptyList(),
    val currentSensorValues: List<TestParameter> = emptyList(),
    val resultSummary: String? = null,
    val lastRunTimestamp: String? = null
)
