# WebSocket Direct Mode Testing Guide

This guide explains how to test the Warehouse Operations Suite in Direct Mode without requiring the physical Raspberry Pi shuttle. You can use the provided Python script to simulate the shuttle's behavior.

## Overview
In Direct Mode, the app acts as a WebSocket **Client**, attempting to connect to the IP addresses configured in the Settings (`ws://<IP>:<PORT>`). 
To simulate a shuttle, you must run a local **WebSocket Server** on your machine.

### How to test using the Mock Server

1. **Start the Mock Server:**
   Run the included Python script in the root directory:
   ```bash
   pip install websockets
   python mock_shuttle_server.py
   ```
   This will start a WebSocket server on `ws://0.0.0.0:8081`.

2. **Configure the App:**
   - Open Settings > Communication.
   - Switch to "Direct Mode".
   - Set Port to `8081`.
   - Add your laptop's IP address (e.g., `127.0.0.1` or your LAN IP) to the "Allowed Shuttle IPs".

3. **Discovery Flow:**
   When you open Shuttle Management in the app and click "Refresh", it will attempt to connect to your IP.
   The mock server automatically broadcasts `INFO` and `TELEMETRY` payloads, which the app will use to discover and display the Mock Shuttle.

---

## Protocol Format

Direct mode uses a unified JSON wrapper for all payloads, eliminating the need for MQTT topics:
```json
{
  "type": "<MESSAGE_TYPE>",
  "data": { ... }
}
```

### 1. Discovery / Info (Server -> App)

**Trigger:** Sent periodically or immediately upon connection.
**Payload:**
```json
{
  "type": "INFO",
  "data": {
    "deviceId": "MOCK-001",
    "timestamp": 1754040000,
    "model": "WMS-Direct-Shuttle",
    "firmwareVersion": "1.0.0",
    "hardwareRevision": "revB",
    "ipAddress": "127.0.0.1",
    "macAddress": "00:11:22:33:44:55",
    "uptimeSeconds": 3600,
    "storageCapacity": 100
  }
}
```

### 2. Telemetry / Live Status (Server -> App)

**Trigger:** Sent periodically (e.g., every 1 second) while connected.
**Payload:**
```json
{
  "type": "TELEMETRY",
  "data": {
    "deviceId": "MOCK-001",
    "timestamp": 1754040010,
    "currentMission": "NONE",
    "currentState": "IDLE",
    "speed": 0.0,
    "direction": "NONE",
    "rackPosition": "RACK-A",
    "liftPosition": "DOWN",
    "batteryPercent": 85.5,
    "isEmergencyStopActive": false,
    "commStatus": "ONLINE",
    "isOnline": true
  }
}
```

### 3. Operational Commands (App -> Server)

**Trigger:** Sent by the App when you press buttons in the Operator Console (e.g., Forward, Lift, Stop).
**Expectation (App sends):**
```json
{
  "type": "COMMAND",
  "data": {
    "deviceId": "MOCK-001",
    "messageId": "cmd-12345",
    "timestamp": 1754040500,
    "command": "MOVE_FORWARD",
    "parameters": {
      "speed": "1.0"
    }
  }
}
```

**Action (Server sends response):** The server should acknowledge the command.
```json
{
  "type": "RESPONSE",
  "data": {
    "deviceId": "MOCK-001",
    "timestamp": 1754040501,
    "correlationId": "cmd-12345",
    "success": true,
    "message": "Moving forward",
    "responseTimeMs": 10
  }
}
```

### 4. Diagnostics & Faults (Server -> App)

**Trigger:** Sent by the server to simulate a hardware failure or report diagnostics.
**Payload:**
```json
{
  "type": "FAULTS",
  "data": {
    "deviceId": "MOCK-001",
    "timestamp": 1754040600,
    "faults": [
      {
        "id": "f-01",
        "faultCode": "ERR-SENS-01",
        "faultName": "Sensor Blocked",
        "severity": "CRITICAL",
        "description": "Front proximity sensor blocked",
        "suggestedAction": "Clear obstruction",
        "timestamp": "2026-08-05T10:30:00Z"
      }
    ]
  }
}
```
