import asyncio
import json
import time
import uuid

import websockets

# Configuration
HOST = "0.0.0.0"
PORT = 8081

DEVICE_ID = str(uuid.uuid4())

async def mock_shuttle_handler(websocket):
    try:
        path = websocket.request.path
    except AttributeError:
        path = getattr(websocket, "path", "/")

    if path != "/ws":
        print(f"[{time.strftime('%X')}] Rejected connection to invalid path: {path}")
        await websocket.close()
        return

    print(f"[{time.strftime('%X')}] Client connected on {path}.")
    
    # Broadcast INFO immediately upon connection
    info_msg = {
        "type": "INFO",
        "data": {
            "deviceId": DEVICE_ID,
            "timestamp": int(time.time() * 1000),
            "serialNumber": "WS-2026-MOCK",
            "displayName": "Laptop Mock Shuttle",
            "protocolVersion": "1.0",
            "firmwareVersion": "1.0.0",
            "hardwareVersion": "Mock-Server",
            "manufacturer": "JKW Innovatics",
            "status": "ONLINE"
        }
    }
    
    await websocket.send(json.dumps(info_msg))
    print(f"[{time.strftime('%X')}] Sent INFO payload.")

    # Background task to send telemetry periodically
    async def send_telemetry():
        while True:
            try:
                telemetry_msg = {
                    "type": "TELEMETRY",
                    "data": {
                        "deviceId": DEVICE_ID,
                        "timestamp": int(time.time() * 1000),
                        "motorVoltage": 24.5,
                        "motorCurrent": 2.1,
                        "batteryVoltage": 25.1,
                        "batteryCurrent": 1.5,
                        "speed": 0.0,
                        "motorTemperature": 45.2
                    }
                }
                
                status_msg = {
                    "type": "STATUS",
                    "data": {
                        "deviceId": DEVICE_ID,
                        "timestamp": int(time.time() * 1000),
                        "state": "IDLE",
                        "currentTask": "None",
                        "batteryPercentage": 85,
                        "speed": 0.0,
                        "direction": "FORWARD",
                        "emergencyStop": False,
                        "activeFaultCount": 0
                    }
                }
                
                diagnostics_msg = {
                    "type": "DIAGNOSTICS",
                    "data": {
                        "deviceId": DEVICE_ID,
                        "timestamp": int(time.time() * 1000),
                        "motors": [
                            {
                                "voltage": 24.5, "current": 2.1, "power": 51.45, "temperature": 45.2,
                                "rpm": 1500, "torque": 12.4, "direction": "FORWARD", "runtime": 360000,
                                "status": "HEALTHY", "faultCode": "NONE"
                            }
                        ],
                        "battery": {
                            "voltage": 25.1, "current": 1.5, "temperature": 32.1, "percentage": 85,
                            "remainingCapacity": 40.5, "estimatedRuntime": 420, "chargeCycles": 124,
                            "health": "HEALTHY", "charging": False
                        },
                        "plc": {
                            "status": "HEALTHY", "scanTime": 12, "cpuUtilization": 45,
                            "memoryUsage": 60, "watchdog": "OK", "uptime": 86400
                        },
                        "communication": {
                            "mqtt": "HEALTHY", "wifiSignal": -55, "radio": "HEALTHY", "can": "HEALTHY",
                            "framesSent": 15000, "framesReceived": 14500, "packetLoss": 0, "busLoad": 15
                        },
                        "radio": {
                            "status": "HEALTHY", "signalStrength": 80, "packetCount": 5000, "packetLoss": 1, "lastPacket": int(time.time() * 1000)
                        },
                        "emergencyStop": {
                            "active": False, "lastTriggered": int(time.time() * 1000) - 86400, "recovered": True
                        }
                    }
                }
                
                await websocket.send(json.dumps(telemetry_msg))
                await websocket.send(json.dumps(status_msg))
                await websocket.send(json.dumps(diagnostics_msg))
                
                # Report mock data
                reports_msg = {
                    "type": "REPORTS",
                    "data": {
                        "deviceId": DEVICE_ID,
                        "timestamp": int(time.time() * 1000),
                        "summary": {
                            "totalStoreOperations": 1200,
                            "totalRetrieveOperations": 950,
                            "totalTasksCompleted": 2150,
                            "totalFaults": 15,
                            "avgBatteryLevel": 85.4,
                            "avgProductivity": 98.2,
                            "activeFaults": 1
                        },
                        "storeOperations": [
                            {"id": "OP-S1", "location": "R1-C1", "timeTakenSec": 45, "status": "COMPLETED", "timestamp": "2026-07-28 08:00:00"}
                        ],
                        "retrieveOperations": [
                            {"id": "OP-R1", "location": "R2-C3", "timeTakenSec": 40, "status": "COMPLETED", "timestamp": "2026-07-28 08:05:00"}
                        ],
                        "productivity": {
                            "completed": 2150,
                            "failed": 25,
                            "efficiencyPercentage": 98.8
                        },
                        "taskHistory": [
                            {"id": "TK-1", "type": "STORE", "priority": "HIGH", "status": "COMPLETED", "createdTime": "2026-07-28 08:00:00", "completedTime": "2026-07-28 08:05:00", "durationSec": 300}
                        ],
                        "missionHistory": [
                            {"id": "MS-1", "type": "BATCH_STORE", "totalTasks": 12, "status": "COMPLETED", "distanceTraveled": 14.5, "startTime": "2026-07-28 09:00:00", "endTime": "2026-07-28 11:00:00", "faultsEncountered": 0}
                        ],
                        "utilization": [
                            {"date": "2026-07-28", "activeHours": 6.5, "idleHours": 17.5, "utilizationPercentage": 27.0, "tasksCompleted": 80, "distanceTraveled": 180.0}
                        ],
                        "batteryReport": [
                            {"id": "BT-1", "timestamp": "2026-07-28 12:00:00", "endPercentage": 85.0, "endVoltage": 48.2, "endCurrent": 12.4, "endTemperature": 28.5, "dischargeTimeMin": 142, "status": "DISCHARGING"}
                        ],
                        "motorRuntime": [
                            {"id": "MR-1", "date": "2026-07-28", "driveMotorHours": 6.2, "liftMotorHours": 4.8, "driveMotorStarts": 410, "liftMotorStarts": 195, "avgDriveTemp": 52.4, "avgLiftTemp": 48.1}
                        ],
                        "faultHistory": [
                            {"id": "FT-1", "faultCode": "E1000", "faultType": "SENSOR_FAULT", "severity": "MINOR", "description": "Fault on SENSOR FAULT", "timeOccurred": "2026-07-28 14:00:00", "timeResolved": "2026-07-28 15:00:00", "resolvedBy": "TECH-01", "downtimeMin": 15}
                        ],
                        "maintenanceHistory": [
                            {"id": "MH-1", "type": "SCHEDULED", "technician": "TECH-01", "date": "2026-07-28", "durationMin": 90, "partsReplaced": ["Filter"], "notes": "Routine scheduled maintenance completed.", "nextScheduledDate": "2026-08-28"}
                        ]
                    }
                }
                await websocket.send(json.dumps(reports_msg))
                
                await asyncio.sleep(2)
            except websockets.exceptions.ConnectionClosed:
                break
            except Exception as e:
                print(f"Error in telemetry loop: {e}")
                break

    telemetry_task = asyncio.create_task(send_telemetry())

    try:
        async for message in websocket:
            print(f"[{time.strftime('%X')}] Received: {message}")
            try:
                payload = json.loads(message)
                msg_type = payload.get("type")
                msg_data = payload.get("data", {})
                
                if msg_type == "COMMAND":
                    # Send response back
                    command = msg_data.get('command')
                    print(f"[{time.strftime('%X')}] Received COMMAND: {command}")
                    response_msg = {
                        "type": "COMMAND_RESPONSE",
                        "data": {
                            "deviceId": DEVICE_ID,
                            "timestamp": int(time.time() * 1000),
                            "requestId": msg_data.get("requestId", ""),
                            "status": "ACCEPTED",
                            "message": f"Executing {command}"
                        }
                    }
                    await websocket.send(json.dumps(response_msg))
                    print(f"[{time.strftime('%X')}] Sent COMMAND_RESPONSE.")
                    
                    # Simulate command completion
                    await asyncio.sleep(1.0)
                    complete_msg = {
                        "type": "COMMAND_RESPONSE",
                        "data": {
                            "deviceId": DEVICE_ID,
                            "timestamp": int(time.time() * 1000),
                            "requestId": msg_data.get("requestId", ""),
                            "status": "COMPLETED",
                            "message": f"{command} executed successfully"
                        }
                    }
                    await websocket.send(json.dumps(complete_msg))
                    print(f"[{time.strftime('%X')}] Sent COMMAND_RESPONSE COMPLETED.")
                    
            except json.JSONDecodeError:
                print(f"[{time.strftime('%X')}] Invalid JSON received.")
                
    except websockets.exceptions.ConnectionClosed as e:
        print(f"[{time.strftime('%X')}] Client disconnected: {e}")
    finally:
        telemetry_task.cancel()

async def main():
    print(f"Starting mock shuttle server on ws://{HOST}:{PORT}")
    async with websockets.serve(mock_shuttle_handler, HOST, PORT):
        await asyncio.Future()  # run forever

if __name__ == "__main__":
    asyncio.run(main())
