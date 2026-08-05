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
    print(f"[{time.strftime('%X')}] Client connected.")
    
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
                
                await websocket.send(json.dumps(telemetry_msg))
                await websocket.send(json.dumps(status_msg))
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
                    response_msg = {
                        "type": "COMMAND_RESPONSE",
                        "data": {
                            "deviceId": DEVICE_ID,
                            "timestamp": int(time.time() * 1000),
                            "requestId": msg_data.get("requestId", ""),
                            "status": "ACCEPTED",
                            "message": f"Executing {msg_data.get('command')}"
                        }
                    }
                    await websocket.send(json.dumps(response_msg))
                    print(f"[{time.strftime('%X')}] Sent COMMAND_RESPONSE.")
                    
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
