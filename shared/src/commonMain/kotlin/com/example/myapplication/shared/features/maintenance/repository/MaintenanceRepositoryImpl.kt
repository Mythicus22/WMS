package com.example.myapplication.shared.features.maintenance.repository

import com.example.myapplication.shared.core.common.Result
import com.example.myapplication.shared.features.maintenance.model.TestCategory
import com.example.myapplication.shared.features.maintenance.model.TestDefinition
import com.example.myapplication.shared.features.maintenance.model.TestExecutionState
import com.example.myapplication.shared.features.maintenance.model.TestParameter
import com.example.myapplication.shared.features.maintenance.model.TestStatus
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MaintenanceRepositoryImpl : MaintenanceRepository {

    private val allTestDefinitions = listOf(
        TestDefinition(
            id = "TOP_PALLET_SENSOR",
            name = "Top Pallet Sensor Test",
            category = TestCategory.SENSORS,
            objective = "Verify optical detection accuracy and alignment of top pallet reflection sensors.",
            description = "Tests individual photo-eye sensors PE-01 to PE-04 mounted on the top deck to verify cargo presence and overhang alignment.",
            requiredPreconditions = listOf(
                "Shuttle parked in Maintenance Bay 01",
                "Pallet deck clear of physical obstructions",
                "24V DC auxiliary power enabled"
            ),
            expectedPlcResponse = "PLC input registers %I0.0-%I0.3 transition HIGH upon beam interruption.",
            expectedSensorResponse = "Optical reflectance signal > 4.2V DC when pallet surface is detected.",
            expectedBehaviour = "Shuttle controller sets PalletDetected flag to TRUE when 2 or more sensors trigger simultaneously.",
            troubleshootingGuide = listOf(
                "Inspect optical lens for dust, oil, or physical abrasion.",
                "Verify sensor wiring harness connector J4 is fully seated.",
                "Check 24V supply rail voltage across terminals TS-12 and TS-13."
            )
        ),
        TestDefinition(
            id = "RACK_END_SENSOR",
            name = "Rack End Sensor Test",
            category = TestCategory.SENSORS,
            objective = "Validate front and rear inductive sensors that signal physical channel boundaries.",
            description = "Simulates approaching front and rear channel stoppers to verify end-of-track deceleration and emergency mechanical stop triggers.",
            requiredPreconditions = listOf(
                "Shuttle located at least 1 meter from channel ends",
                "Brake system released in manual test mode",
                "Channel end metallic flags verified intact"
            ),
            expectedPlcResponse = "PLC registers %I0.4 (Front End) and %I0.5 (Rear End) read 1 when over metal targets.",
            expectedSensorResponse = "Inductive proximity signal output transitions from 0V to 24V DC.",
            expectedBehaviour = "Immediate zero-velocity command issued to motor controller upon boundary trip.",
            troubleshootingGuide = listOf(
                "Measure sensor gap distance (recommended 2.0mm ± 0.5mm).",
                "Clean target metal flag from metal filings or dirt accumulation.",
                "Verify LED status indicator on sensor barrel illuminates on target."
            )
        ),
        TestDefinition(
            id = "OBSTACLE_SENSOR",
            name = "Obstacle Sensor Test",
            category = TestCategory.SENSORS,
            objective = "Test all 6 perimeter optical obstacle sensors and safety stop interlocking.",
            description = "Evaluates front, rear, and side optical sensors designed to detect personnel or debris in the shuttle path.",
            requiredPreconditions = listOf(
                "Clear 1-meter radius around shuttle perimeter",
                "Safety light curtain system energized",
                "Laser sensor glass covers clean"
            ),
            expectedPlcResponse = "PLC safety inputs %I1.0 through %I1.5 register TRIP state when path blocked.",
            expectedSensorResponse = "Laser time-of-flight output drops below configured safety zone distance.",
            expectedBehaviour = "Cat 0 Immediate stop initiated; master safety relay drops out.",
            troubleshootingGuide = listOf(
                "Verify sensor optical alignment with reflector bracket.",
                "Check CANbus safety node diagnostic codes for address 0x20.",
                "Re-calibrate laser detection zones using field service software."
            )
        ),
        TestDefinition(
            id = "ENTRY_EXIT_SENSOR",
            name = "Entry / Exit Sensor Test",
            category = TestCategory.SENSORS,
            objective = "Verify aisle entry and exit handover optical sensors.",
            description = "Validates the sensors responsible for confirming shuttle alignment with the main channel elevator / transfer car.",
            requiredPreconditions = listOf(
                "Shuttle positioned at aisle entrance rail junction",
                "Elevator positioning lock engaged"
            ),
            expectedPlcResponse = "PLC input registers %I2.0 and %I2.1 register HIGH during rail transition.",
            expectedSensorResponse = "Dual optical beam sensors confirm continuous rail continuity signal.",
            expectedBehaviour = "Handshake protocol authorizes channel-to-elevator transition.",
            troubleshootingGuide = listOf(
                "Clean optical reflector mirrors on elevator transfer frame.",
                "Inspect cable carrier for pinches or broken wires on sensor line."
            )
        ),
        TestDefinition(
            id = "DT35_DISTANCE_SENSOR",
            name = "DT35 Distance Sensor Test",
            category = TestCategory.SENSORS,
            objective = "Calibrate and verify accuracy of DT35 laser distance measurement sensors.",
            description = "Reads precision distance measurements from front and rear DT35 laser units for absolute positioning accuracy down to ±1mm.",
            requiredPreconditions = listOf(
                "Laser reflector targets clean and perpendicular",
                "No ambient direct sunlight glare on reflector"
            ),
            expectedPlcResponse = "Analog register %IW100 returns 4-20mA current loop value corresponding to mm position.",
            expectedSensorResponse = "RS422 / Analog telemetry returns valid distance within ±2mm tolerance.",
            expectedBehaviour = "Deceleration profile calculated continuously based on remaining channel distance.",
            troubleshootingGuide = listOf(
                "Perform 2-point laser distance zero calibration.",
                "Verify 4-20mA loop transmitter resistor on PLC terminal block."
            )
        ),
        TestDefinition(
            id = "LIFT_SENSOR",
            name = "Lift Sensor Test",
            category = TestCategory.SENSORS,
            objective = "Validate Lift Up (top limit) and Lift Down (bottom limit) position switches.",
            description = "Exercises the mechanical lift platform switches to confirm correct platform elevation feedback to the PLC.",
            requiredPreconditions = listOf(
                "Pallet deck empty",
                "Lift mechanism free of mechanical jamming"
            ),
            expectedPlcResponse = "PLC inputs %I0.6 (Lift UP) and %I0.7 (Lift DOWN) switch state cleanly.",
            expectedSensorResponse = "Limit switch contacts switch cleanly without contact bounce (<5ms).",
            expectedBehaviour = "Lift motor power cutoff triggered automatically when limit switch activates.",
            troubleshootingGuide = listOf(
                "Check mechanical actuator arm alignment on lift cam.",
                "Verify limit switch continuity using DMM on harness terminals 8 and 9."
            )
        ),
        TestDefinition(
            id = "RAIL_SENSOR",
            name = "Rail Sensor Test",
            category = TestCategory.SENSORS,
            objective = "Verify left and right rail tracking inductive sensors.",
            description = "Ensures the shuttle maintains proper lateral alignment on channel rails to prevent derailment.",
            requiredPreconditions = listOf(
                "Shuttle placed evenly on standard rack rails",
                "Wheel flanges inspected for wear"
            ),
            expectedPlcResponse = "PLC inputs %I3.0 and %I3.1 maintain steady HIGH signal while on track.",
            expectedSensorResponse = "Inductive sensor output stays within 18-24V DC range.",
            expectedBehaviour = "Derailment warning generated if either rail sensor loses target for >100ms.",
            troubleshootingGuide = listOf(
                "Inspect rail flange clearance (standard 3.0mm gap).",
                "Check for rail deformation or loose mounting bolts."
            )
        ),
        TestDefinition(
            id = "DRIVE_MOTOR",
            name = "Drive Motor Test",
            category = TestCategory.MOTORS,
            objective = "Exercise drive motor inverter, current draw, RPM response, and thermal monitoring.",
            description = "Runs short forward/reverse velocity ramps to evaluate drive motor winding current, encoder feedback, and motor controller status.",
            requiredPreconditions = listOf(
                "Shuttle elevated on test stand or clear 2-meter track",
                "48V Main power breaker closed",
                "Drive brake manually released"
            ),
            expectedPlcResponse = "CANopen node 0x10 status word 0x6041 reports Operation Enabled.",
            expectedSensorResponse = "Quadrature encoder quadrature pulses track commanded RPM within 1.5%.",
            expectedBehaviour = "Smooth acceleration curve to 1450 RPM without overcurrent trips.",
            troubleshootingGuide = listOf(
                "Check drive inverter error codes via CAN diagnostic tool.",
                "Inspect motor power cables U, V, W for insulation breakdown."
            )
        ),
        TestDefinition(
            id = "LIFT_MOTOR",
            name = "Lift Motor Test",
            category = TestCategory.MOTORS,
            objective = "Test lift actuator motor elevation torque, speed, and thermal parameters.",
            description = "Cycles the lift platform through UP and DOWN movement to verify lifting torque capacity and encoder positioning.",
            requiredPreconditions = listOf(
                "Lift mechanism lubricated",
                "No foreign objects on scissor assembly"
            ),
            expectedPlcResponse = "Lift position word increments smoothly from 0mm to 120mm.",
            expectedSensorResponse = "Motor thermistor reads <65°C; phase current <15A peak.",
            expectedBehaviour = "Platform elevates to full height (120mm) in under 2.5 seconds.",
            troubleshootingGuide = listOf(
                "Inspect lead screw / hydraulic lift linkage for mechanical binding.",
                "Verify lift brake solenoid release voltage (24V DC)."
            )
        ),
        TestDefinition(
            id = "RELAY",
            name = "Relay Test",
            category = TestCategory.ELECTRICAL_RELAYS,
            objective = "Validate main power contactor, brake coils, and auxiliary relay switching.",
            description = "Toggles high-current control relays to verify auxiliary contact feedback and coil circuit integrity.",
            requiredPreconditions = listOf(
                "HV isolation switch in TEST mode",
                "Personnel clear of moving components"
            ),
            expectedPlcResponse = "Auxiliary contact feedback registers %I4.0 - %I4.3 mirror coil outputs.",
            expectedSensorResponse = "Relay contact resistance <0.05 Ohms when closed.",
            expectedBehaviour = "Audible latching click heard within 20ms of command issuance.",
            troubleshootingGuide = listOf(
                "Measure coil resistance across terminals A1-A2 (typical 120 Ohms).",
                "Check for pitted or welded relay contacts."
            )
        ),
        TestDefinition(
            id = "CAN_COMMUNICATION",
            name = "CAN Communication Test",
            category = TestCategory.COMMUNICATION,
            objective = "Audit CANopen bus integrity, baud rate stability, and node packet statistics.",
            description = "Sends test frames to all onboard CAN nodes (Drive Inverter, Lift Node, I/O Expansion, BMS) to measure frame rates and error counters.",
            requiredPreconditions = listOf(
                "CANbus 120-Ohm termination resistors installed at both ends",
                "Bus baud rate configured to 500 kbps"
            ),
            expectedPlcResponse = "PLC CAN status register reports CAN_STATE_OPERATIONAL.",
            expectedSensorResponse = "CAN Transceiver Rx/Tx differential voltage ±2.0V nominal.",
            expectedBehaviour = "Zero bus-off events; error frame count remains 0 during 5-second burst.",
            troubleshootingGuide = listOf(
                "Measure CAN_H to CAN_L resistance with power OFF (should read ~60 Ohms).",
                "Check for shield grounding loops along the communications harness."
            )
        ),
        TestDefinition(
            id = "BATTERY",
            name = "Battery Test",
            category = TestCategory.BATTERY_POWER,
            objective = "Analyze LiFePO4 battery pack voltage, internal resistance, BMS status, and cell balance.",
            description = "Queries the Smart Battery Management System (BMS) via Modbus/CAN to verify state-of-charge, cell voltage uniformity, and discharge rate.",
            requiredPreconditions = listOf(
                "Battery connected to BMS telemetry port",
                "Minimum battery charge > 20%"
            ),
            expectedPlcResponse = "BMS telemetry register packet received without CRC errors.",
            expectedSensorResponse = "Cell voltage spread <20mV across all 16 cells.",
            expectedBehaviour = "Pack voltage reads 48V-54V DC; BMS status flags normal.",
            troubleshootingGuide = listOf(
                "Inspect BMS communication harness plug J1.",
                "Check main battery fuse F1 (200A fast-acting)."
            )
        ),
        TestDefinition(
            id = "RADIO_RECEIVER",
            name = "Radio Receiver Test",
            category = TestCategory.COMMUNICATION,
            objective = "Verify 5GHz industrial Wi-Fi / RF transceiver signal strength, RSSI, and packet throughput.",
            description = "Performs round-trip latency ping tests between shuttle radio transceiver and warehouse Access Point.",
            requiredPreconditions = listOf(
                "Warehouse AP coverage active on Channel 36",
                "Shuttle antenna connectors torqued to specification"
            ),
            expectedPlcResponse = "Comm watchdog timer resets on every valid heartbeat frame.",
            expectedSensorResponse = "RSSI >= -65 dBm; packet loss < 0.1%.",
            expectedBehaviour = "Ping round-trip latency < 15ms continuous.",
            troubleshootingGuide = listOf(
                "Inspect RF coax cable connections for moisture or looseness.",
                "Check for co-channel interference using Wi-Fi spectrum analyzer."
            )
        ),
        TestDefinition(
            id = "EMERGENCY_STOP",
            name = "Emergency Stop Test",
            category = TestCategory.SAFETY_ESTOP,
            objective = "Test hardware E-Stop pushbuttons, safety relay dropout, and system lockout recovery.",
            description = "Simulates emergency stop actuation to confirm instant motor power isolation and software fault latching.",
            requiredPreconditions = listOf(
                "Shuttle stopped in safe maintenance area",
                "Manual reset key available"
            ),
            expectedPlcResponse = "Safety input dual-channel %I1.0 / %I1.1 transition to OPEN simultaneously.",
            expectedSensorResponse = "Safety relay outputs K1/K2 drop out within <10ms.",
            expectedBehaviour = "Drive power cut off instantly; E-STOP active banner latching on HMI.",
            troubleshootingGuide = listOf(
                "Check physical E-stop button contact blocks for mechanical damage.",
                "Verify dual-channel safety circuit loop resistance (<5 Ohms)."
            )
        )
    )

    private val testExecutions = mutableMapOf<String, MutableStateFlow<TestExecutionState>>()

    override fun getAllTestDefinitions(): List<TestDefinition> = allTestDefinitions

    override fun getTestDefinition(testId: String): TestDefinition? {
        return allTestDefinitions.find { it.id == testId }
    }

    override fun observeTestExecution(shuttleId: String, testId: String): Flow<TestExecutionState> {
        val key = "${shuttleId}_${testId}"
        val flow = testExecutions.getOrPut(key) {
            MutableStateFlow(createInitialMockState(shuttleId, testId))
        }
        return flow.asStateFlow()
    }

    override suspend fun runTest(shuttleId: String, testId: String): Result<Unit> {
        val key = "${shuttleId}_${testId}"
        val flow = testExecutions.getOrPut(key) {
            MutableStateFlow(createInitialMockState(shuttleId, testId))
        }

        Napier.d("Starting test $testId for shuttle $shuttleId", tag = "MaintenanceRepositoryImpl")

        // Progress simulation: 0.0 -> 0.4 -> 0.8 -> 1.0
        flow.update { it.copy(status = TestStatus.RUNNING, progress = 0.2f, resultSummary = "Running diagnostic routines...") }
        delay(400)
        flow.update { it.copy(progress = 0.6f, resultSummary = "Evaluating PLC & Sensor telemetry response...") }
        delay(400)
        flow.update { it.copy(progress = 0.9f, resultSummary = "Verifying tolerance thresholds...") }
        delay(300)

        // Complete test with realistic passed state
        val updatedMockState = createCompletedMockState(shuttleId, testId)
        flow.value = updatedMockState

        return Result.Success(Unit)
    }

    override suspend fun resetTest(shuttleId: String, testId: String): Result<Unit> {
        val key = "${shuttleId}_${testId}"
        val flow = testExecutions.getOrPut(key) {
            MutableStateFlow(createInitialMockState(shuttleId, testId))
        }
        flow.value = createInitialMockState(shuttleId, testId)
        return Result.Success(Unit)
    }

    private fun createInitialMockState(shuttleId: String, testId: String): TestExecutionState {
        val (plcParams, sensorParams) = getMockParametersForTest(testId)
        return TestExecutionState(
            testId = testId,
            shuttleId = shuttleId,
            status = TestStatus.IDLE,
            progress = 0.0f,
            currentPlcValues = plcParams,
            currentSensorValues = sensorParams,
            resultSummary = "Ready to execute test routine.",
            lastRunTimestamp = null
        )
    }

    private fun createCompletedMockState(shuttleId: String, testId: String): TestExecutionState {
        val (plcParams, sensorParams) = getMockParametersForTest(testId)
        return TestExecutionState(
            testId = testId,
            shuttleId = shuttleId,
            status = TestStatus.PASSED,
            progress = 1.0f,
            currentPlcValues = plcParams,
            currentSensorValues = sensorParams,
            resultSummary = "Test passed successfully. All parameters within expected operating tolerances.",
            lastRunTimestamp = "2026-07-28 14:35:12"
        )
    }

    private fun getMockParametersForTest(testId: String): Pair<List<TestParameter>, List<TestParameter>> {
        return when (testId) {
            "TOP_PALLET_SENSOR" -> Pair(
                listOf(
                    TestParameter("%I0.0 PE-01 Top Left", "HIGH", isNormal = true),
                    TestParameter("%I0.1 PE-02 Top Right", "HIGH", isNormal = true),
                    TestParameter("%I0.2 PE-03 Rear Left", "HIGH", isNormal = true),
                    TestParameter("%I0.3 PE-04 Rear Right", "HIGH", isNormal = true),
                    TestParameter("Combined Pallet Detected", "DETECTED", isNormal = true)
                ),
                listOf(
                    TestParameter("Reflectance Signal PE-01", "4.45", "V DC", isNormal = true),
                    TestParameter("Reflectance Signal PE-02", "4.38", "V DC", isNormal = true),
                    TestParameter("Trigger Count", "42", "events", isNormal = true),
                    TestParameter("Last Trigger Time", "14:32:05", isNormal = true)
                )
            )

            "RACK_END_SENSOR" -> Pair(
                listOf(
                    TestParameter("%I0.4 Front Rack Sensor", "HIGH", isNormal = true),
                    TestParameter("%I0.5 Rear Rack Sensor", "LOW", isNormal = true),
                    TestParameter("Detection State", "AT_FRONT_END", isNormal = true)
                ),
                listOf(
                    TestParameter("Front Inductive Gap", "2.1", "mm", isNormal = true),
                    TestParameter("Rear Inductive Gap", "2.3", "mm", isNormal = true),
                    TestParameter("Trigger Count", "18", "events", isNormal = true)
                )
            )

            "OBSTACLE_SENSOR" -> Pair(
                listOf(
                    TestParameter("%I1.0 Front Laser Zone 1", "NORMAL", isNormal = true),
                    TestParameter("%I1.1 Front Laser Zone 2", "NORMAL", isNormal = true),
                    TestParameter("%I1.2 Rear Laser Zone 1", "NORMAL", isNormal = true),
                    TestParameter("%I1.3 Rear Laser Zone 2", "NORMAL", isNormal = true),
                    TestParameter("%I1.4 Left Side Sensor", "NORMAL", isNormal = true),
                    TestParameter("%I1.5 Right Side Sensor", "NORMAL", isNormal = true)
                ),
                listOf(
                    TestParameter("Obstacle Detected", "FALSE", isNormal = true),
                    TestParameter("E-Stop Safety Interlock", "INACTIVE", isNormal = true),
                    TestParameter("Front Distance Clearance", "1850", "mm", isNormal = true)
                )
            )

            "ENTRY_EXIT_SENSOR" -> Pair(
                listOf(
                    TestParameter("%I2.0 Aisle Entry Sensor", "HIGH", isNormal = true),
                    TestParameter("%I2.1 Aisle Exit Sensor", "LOW", isNormal = true),
                    TestParameter("Handshake State", "ENTRY_ENGAGED", isNormal = true)
                ),
                listOf(
                    TestParameter("Entry Trigger Counter", "156", "counts", isNormal = true),
                    TestParameter("Exit Trigger Counter", "154", "counts", isNormal = true)
                )
            )

            "DT35_DISTANCE_SENSOR" -> Pair(
                listOf(
                    TestParameter("%IW100 Front Laser Loop", "12.4", "mA", isNormal = true),
                    TestParameter("%IW102 Rear Laser Loop", "8.9", "mA", isNormal = true)
                ),
                listOf(
                    TestParameter("Front Distance", "1240", "mm", isNormal = true),
                    TestParameter("Rear Distance", "820", "mm", isNormal = true),
                    TestParameter("Warning Threshold", "500", "mm", isNormal = true),
                    TestParameter("Stop Threshold", "150", "mm", isNormal = true)
                )
            )

            "LIFT_SENSOR" -> Pair(
                listOf(
                    TestParameter("%I0.6 Lift UP Limit Switch", "HIGH", isNormal = true),
                    TestParameter("%I0.7 Lift DOWN Limit Switch", "LOW", isNormal = true)
                ),
                listOf(
                    TestParameter("Current Lift Position", "120.0", "mm (UP)", isNormal = true),
                    TestParameter("Lift Contact Bounce", "1.2", "ms", isNormal = true)
                )
            )

            "RAIL_SENSOR" -> Pair(
                listOf(
                    TestParameter("%I3.0 Left Rail Sensor", "ACTIVE", isNormal = true),
                    TestParameter("%I3.1 Right Rail Sensor", "ACTIVE", isNormal = true)
                ),
                listOf(
                    TestParameter("Tracking State", "ON_TRACK", isNormal = true),
                    TestParameter("Left Rail Gap", "2.8", "mm", isNormal = true),
                    TestParameter("Right Rail Gap", "3.0", "mm", isNormal = true)
                )
            )

            "DRIVE_MOTOR" -> Pair(
                listOf(
                    TestParameter("Node 0x10 ControlWord", "0x000F", isNormal = true),
                    TestParameter("Node 0x10 StatusWord", "0x6041 (Enabled)", isNormal = true),
                    TestParameter("Commanded Velocity", "1450", "RPM", isNormal = true)
                ),
                listOf(
                    TestParameter("DC Bus Voltage", "48.2", "V", isNormal = true),
                    TestParameter("Motor Current Draw", "12.4", "A", isNormal = true),
                    TestParameter("Actual RPM", "1448", "RPM", isNormal = true),
                    TestParameter("Winding Temperature", "42.5", "°C", isNormal = true),
                    TestParameter("Running State", "RUNNING", isNormal = true),
                    TestParameter("Direction", "FORWARD", isNormal = true)
                )
            )

            "LIFT_MOTOR" -> Pair(
                listOf(
                    TestParameter("Node 0x11 StatusWord", "0x6041 (Standby)", isNormal = true),
                    TestParameter("Target Lift Height", "120", "mm", isNormal = true)
                ),
                listOf(
                    TestParameter("Supply Voltage", "48.0", "V", isNormal = true),
                    TestParameter("Motor Current", "8.1", "A", isNormal = true),
                    TestParameter("Current Elevation", "120.0", "mm", isNormal = true),
                    TestParameter("Motor Temp", "38.2", "°C", isNormal = true),
                    TestParameter("Running State", "STANDBY", isNormal = true)
                )
            )

            "RELAY" -> Pair(
                listOf(
                    TestParameter("%I4.0 Relay 1 Aux Contact", "CLOSED", isNormal = true),
                    TestParameter("%I4.1 Relay 2 Aux Contact", "CLOSED", isNormal = true)
                ),
                listOf(
                    TestParameter("Relay 1 Main Power", "CLOSED", isNormal = true),
                    TestParameter("Relay 2 Brake Coils", "CLOSED", isNormal = true),
                    TestParameter("Coil State", "ENERGIZED", isNormal = true),
                    TestParameter("Contact State", "CLOSED", isNormal = true)
                )
            )

            "CAN_COMMUNICATION" -> Pair(
                listOf(
                    TestParameter("PLC CAN Controller", "OPERATIONAL", isNormal = true),
                    TestParameter("Active Nodes Count", "5", "nodes", isNormal = true)
                ),
                listOf(
                    TestParameter("CAN Bus Status", "OPERATIONAL", isNormal = true),
                    TestParameter("Error Frame Count", "0", "errors", isNormal = true),
                    TestParameter("Tx Frames", "482910", "frames", isNormal = true),
                    TestParameter("Rx Frames", "482894", "frames", isNormal = true),
                    TestParameter("Bus Load", "14.2", "%", isNormal = true)
                )
            )

            "BATTERY" -> Pair(
                listOf(
                    TestParameter("BMS Modbus State", "COMM_OK", isNormal = true),
                    TestParameter("BMS Alarm Code", "0x0000", isNormal = true)
                ),
                listOf(
                    TestParameter("Pack Voltage", "51.4", "V", isNormal = true),
                    TestParameter("Discharge Current", "-2.1", "A", isNormal = true),
                    TestParameter("Pack Temperature", "31.0", "°C", isNormal = true),
                    TestParameter("Battery Percentage", "88", "%", isNormal = true),
                    TestParameter("Estimated Runtime", "6.5", "hours", isNormal = true),
                    TestParameter("Charging State", "DISCHARGING", isNormal = true)
                )
            )

            "RADIO_RECEIVER" -> Pair(
                listOf(
                    TestParameter("Comm Watchdog Timer", "0", "ms", isNormal = true),
                    TestParameter("AP SSID Handshake", "WMS_MESH_01", isNormal = true)
                ),
                listOf(
                    TestParameter("Signal Strength (RSSI)", "-62", "dBm", isNormal = true),
                    TestParameter("Packet Loss", "0.01", "%", isNormal = true),
                    TestParameter("Connection Status", "CONNECTED", isNormal = true),
                    TestParameter("Last Packet Time", "12", "ms", isNormal = true)
                )
            )

            "EMERGENCY_STOP" -> Pair(
                listOf(
                    TestParameter("%I1.0 Dual Safety Ch 1", "NORMAL", isNormal = true),
                    TestParameter("%I1.1 Dual Safety Ch 2", "NORMAL", isNormal = true)
                ),
                listOf(
                    TestParameter("Emergency Stop Input", "NORMAL / OPEN", isNormal = true),
                    TestParameter("Current Safety State", "OK", isNormal = true),
                    TestParameter("Shutdown Status", "INACTIVE", isNormal = true),
                    TestParameter("Recovery Status", "READY", isNormal = true)
                )
            )

            else -> Pair(
                listOf(TestParameter("PLC Signal", "OK")),
                listOf(TestParameter("Sensor Telemetry", "NORMAL"))
            )
        }
    }
}
