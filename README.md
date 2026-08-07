# Warehouse Operations Suite (WMS)

<div align="center">

![Platform](https://img.shields.io/badge/Platform-Kotlin_Multiplatform_%7C_Compose-7F52FF.svg?style=for-the-badge)
![Target](https://img.shields.io/badge/Target-Android_Tablet_%7C_Desktop-3DDC84.svg?style=for-the-badge)
![Protocol](https://img.shields.io/badge/Protocol-WSP_1.0_(MQTT_%2B_Direct_WS)-0052CC.svg?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-SQLDelight_SQLite-003B57.svg?style=for-the-badge)
![Language](https://img.shields.io/badge/Localization-English_%7C_Hindi-(%E0%A4%B9%E0%A4%BF%E0%A4%82%E0%A4%A6%E0%A5%80)-FF6F00.svg?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)

**An industrial-grade, multi-transport Android tablet application for real-time monitoring, telemetry, control, and management of Autonomous Radio Shuttle Robots.**

[Features](#-key-features--modules) • [Architecture](#-architecture--technology-stack) • [Communication Transports](#-dual-communication-transports) • [Project Structure](#-project-structure) • [Quickstart & Building](#-quickstart--building) • [WSP Protocol](#-warehouse-shuttle-protocol-wsp)

</div>

---

## 📖 Overview

The **Warehouse Operations Suite (WMS)** is a high-performance Kotlin Multiplatform application tailored specifically for Android tablet industrial displays. Designed for modern logistics and automated storage facilities, it equips warehouse operators, maintenance engineers, and facility supervisors with centralized control over autonomous pallet shuttles.

The application combines a modern Material Design 3 user interface with robust, multi-transport communication capabilities. It seamlessly bridges high-level WMS inventory workflows with low-level shuttle robotics via the **Warehouse Shuttle Protocol (WSP)**.

---

## ⚡ Key Features & Modules

### 1. 📊 Dashboard & Telemetry Oversight
* Real-time warehouse metrics including total discovered shuttles, active shuttle connection details, and overall system status.
* Filtered module access based on Role-Based Access Control (RBAC).
* Quick action cards navigating directly to operational console screens.

### 2. 🤖 Shuttle Management & Discovery
* **Parallel IP Discovery**: Asynchronously probes target and fallback IP ranges (`127.0.0.1`, `10.0.2.2`, `localhost`, `0.0.0.0`, `192.168.68.78`) over WebSocket for instant shuttle discovery without UI freeze.
* **MQTT Zero-Conf Discovery**: Automatically detects shuttles broadcasting retained identity metadata.
* Single-click connection, status badge indicators (`CONNECTED`, `ACTIVE`, `DISABLED`), and device registration management.

### 3. 🕹️ Operator Console
* Real-time manual and automated shuttle motion controls (`STORE`, `RETRIEVE`, `MOVE_FORWARD`, `MOVE_REVERSE`, `STOP`, `LIFT_UP`, `LIFT_DOWN`).
* Instant Emergency Stop trigger with physical override capability.
* Live operational feedback displaying current task state, battery status, speed, and rack position.

### 4. 🩺 Diagnostics & Maintenance
* Multi-subsystem engineering diagnostics stream covering Motor, Battery, PLC, Communication, and Sensors.
* Automated maintenance test routines (`DRIVE_MOTOR_TEST`, `LIFT_SENSOR_TEST`, `CAN_TEST`, `RELAY_TEST`, etc.) with execution outcome reports (`PASS`, `FAIL`, `WARNING`).

### 5. 📈 Reports & Analytics
* Compact data presentation displaying multi-shuttle performance metrics and aggregated warehouse statistics.
* Individual shuttle report views alongside aggregated **All Shuttles** analytics.
* Instant report export capabilities (PDF, CSV, JSON).

### 6. ⚙️ Industrial Settings Management
* **General Settings**: Theme selection (Light & Dark modes), font sizing, and **Full Hindi Localization Switch**.
* **Communication Settings**: Mode toggle between `MQTT_BROKER` and `DIRECT` WebSocket, broker address/port tuning, keep-alive timers, and allowed shuttle IP lists.
* **Reports & Backup Settings**: Automatic backup interval, SQLite database export paths, and report naming token templates.
* **System & Security**: Security policy options, session timeouts, and debug logging controls.

### 7. 👥 User Management & Role Security
* Comprehensive Role-Based Access Control supporting **Admin**, **Operator**, **Technician**, and **Supervisor** roles.
* Create, edit, and unregister user credentials with granular feature permission flags.

---

## 🏗 Architecture & Technology Stack

The application follows strict **Clean Architecture** and **MVVM** design principles, keeping domain business rules completely independent of UI components and hardware transports.

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                              │
│         Compose Multiplatform UI (Screens, AppToolbar, Dialogs)        │
├────────────────────────────────────────────────────────────────────────┤
│                          ViewModel Layer                               │
│            StateFlow / UI Event & Effect Handlers (MVVM)               │
├────────────────────────────────────────────────────────────────────────┤
│                           Domain Layer                                 │
│          Use Cases, Domain Models, Feature Permission Rules            │
├────────────────────────────────────────────────────────────────────────┤
│                           Data Layer                                   │
│        Repositories (Discovery, Registered Shuttles, Settings, User)   │
├────────────────────────────────────────────────────────────────────────┤
│                       Communication Layer                              │
│    CommunicationServiceImpl (Unified Multi-Transport Dispatcher)       │
│        ├── MqttTransport (MQTT 3.1.1 / 5.0 Broker Client)              │
│        └── DirectTransport (Parallel WebSocket Client)                │
├────────────────────────────────────────────────────────────────────────┤
│                        Persistence Layer                               │
│        SQLDelight SQLite Database & Platform Security Drivers         │
└────────────────────────────────────────────────────────────────────────┘
```

### Core Technologies
* **Language**: Kotlin 1.9+ (Kotlin Multiplatform / KMP)
* **UI Framework**: Compose Multiplatform with Material Design 3
* **Dependency Injection**: Koin (`sharedModule`, `dashboardModule`, `shuttleModule`, `settingsModule`, `userModule`)
* **Database & Persistence**: SQLDelight (SQLite with platform-native drivers)
* **Network & WebSockets**: Ktor Client (WebSocket & HTTP engine)
* **Logging**: Napier multiplatform logging library
* **Localization**: Custom CompositionLocal engine (`LocalAppLanguage`) supporting live English & Hindi UI translations

---

## 📡 Dual Communication Transports

The system transparently supports two industrial communication modes via `CommunicationServiceImpl`:

### 1. MQTT Broker Mode (`MQTT_BROKER`)
* **Use Case**: Multi-shuttle centralized warehouse networks over an Eclipse Mosquitto or HiveMQ broker.
* **Topic Namespace**: `warehouse/shuttle/{deviceId}/...`
* **Features**: Retained `info` and `status` messages, QoS 1 command delivery, and Last Will and Testament (LWT) disconnect monitoring.

### 2. Direct WebSocket Mode (`DIRECT`)
* **Use Case**: Standalone point-to-point connections, offline edge testing, or direct tablet-to-shuttle maintenance.
* **Endpoint**: `ws://{shuttleIp}:8081/ws`
* **JSON Envelope**:
  ```json
  {
    "type": "INFO | COMMAND | RESPONSE | STATUS | TELEMETRY | DIAGNOSTICS | MAINTENANCE | FAULT | HEARTBEAT | LOG",
    "data": { /* WSP Payload Object */ }
  }
  ```
* **Parallel Discovery**: Probes multiple target IPs simultaneously using non-blocking coroutines for instant device discovery.

---

## 📂 Project Structure

```text
wms/
├── androidApp/                                  # Android entrypoint & Activity shell
│   └── src/main/
│       ├── java/com/example/myapplication/android/  # MainActivity & Android Application
│       └── res/                                 # Launcher icons & Android resources
├── shared/                                      # KMP Shared Source Code
│   └── src/commonMain/kotlin/com/example/myapplication/
│       ├── App.kt                               # Root Composable & CompositionLocal providers
│       ├── communication/                       # WSP Protocol implementation
│       │   ├── model/                           # WspPayload serializable data models
│       │   ├── service/                         # CommunicationServiceImpl multi-transport manager
│       │   └── transport/                       # MqttTransport & DirectTransport engines
│       ├── core/                                # Navigation, DI modules, Extensions
│       ├── database/                            # SQLDelight schema (.sq) and database helpers
│       ├── domain/                              # Feature models and business logic
│       ├── features/                            # Screen ViewModels & UI Composables
│       │   ├── auth/                            # Login screen
│       │   ├── dashboard/                       # Overview & metrics screen
│       │   ├── device/                          # Shuttle Management screen
│       │   ├── diagnostics/                     # Subsystem diagnostics screen
│       │   ├── maintenance/                     # Maintenance test console screen
│       │   ├── operator/                        # Motion control operator screen
│       │   ├── reports/                         # Reports & Analytics screens
│       │   ├── settings/                        # Settings screens & Repository
│       │   └── usermanagement/                  # User accounts & RBAC screen
│       └── presentation/                        # UI Components, Theme, AppToolbar, Localization
├── wsp/                                         # Warehouse Shuttle Protocol Specification
│   └── Protocol/                                # Schemas, payload examples, broker setup & README
├── mock_shuttle_server.py                       # Python asyncio mock WebSocket shuttle server
├── build.gradle.kts                             # Root Gradle build script
└── README.md                                    # Project documentation
```

---

## 🚀 Quickstart & Building

### Prerequisites
* **Java Development Kit**: JDK 17 or higher
* **Android SDK**: API Level 34 (Minimum SDK: 24)
* **Build Tool**: Gradle 8.x
* **Python** (for running mock shuttle server): Python 3.8+ with `websockets` library installed

### 1. Build Android APK
Compile and assemble the debug APK:

```bash
./gradlew :androidApp:assembleDebug
```

The output APK will be generated at `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

### 2. Install & Run on Tablet / Emulator
Install directly to an connected Android device or emulator:

```bash
./gradlew :androidApp:installDebug
```

### 3. Launch Mock Shuttle Server
To test shuttle discovery, live telemetry streaming, and operator commands without physical shuttle hardware, launch the included Python mock server:

```bash
# Install dependency if needed: pip install websockets
python3 mock_shuttle_server.py
```

Output:
```text
Starting mock shuttle server on ws://0.0.0.0:8081
[17:30:00] Client connected on /ws.
[17:30:00] Sent INFO payload.
```

### 4. Validate WSP Protocol Schemas
Verify that all YAML payload examples match the protocol specification schemas:

```bash
python3 wsp/Protocol/scripts/validate_schemas.py
```

---

## 🌐 Warehouse Shuttle Protocol (WSP)

The application communicates using the open **Warehouse Shuttle Protocol (WSP 1.0)** standard. Detailed protocol specifications, topic hierarchies, JSON/YAML schemas, and broker configuration files are stored in the [`wsp/Protocol/`](wsp/Protocol/README.md) directory.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
