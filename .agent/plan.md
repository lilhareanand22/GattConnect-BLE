# Project Plan

Create a production-ready BLE Scanner application named 'BleVButton' that scans for nearby Bluetooth Low Energy devices, connects to them, and displays incoming messages. The app should be a single-screen application following Material Design 3 guidelines with a vibrant, energetic color scheme. It must use Kotlin, Jetpack Compose, MVVM, Clean Architecture, and Hilt. Key features include BLE scanning, device listing with RSSI and connection status, single active connection management, and a message panel for received data. Handle permissions for Android 8+ and 12+.

## Project Brief

# BleVButton Project Brief

## Features
- **BLE Device Discovery**: Real-time scanning for nearby Bluetooth Low Energy devices with live signal strength (RSSI) updates.
- **Connection Management**: One-tap connection and disconnection to a single active BLE device with clear visual status feedback.
- **Real-time Message Monitoring**: A dedicated terminal-style panel to receive and display incoming data packets and system events from the connected peripheral.
- **Automated Permission Handling**: Integrated workflow for managing complex Bluetooth and Location permissions across Android 8 through Android 12+.

## High-Level Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Navigation**: Jetpack Navigation 3 (State-driven)
- **Adaptive Strategy**: Compose Material Adaptive library
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Concurrency**: Kotlin Coroutines & Flow for reactive BLE state management

## UI Design Image
![UI Design](file://C:/Users/Varsha/Documents/WorkSpcace/Google_Jetpack_Training/BleVButton/input_images/image_0.png)

## Implementation Steps

### Task_1_Infrastructure: Setup Hilt DI, project architecture, and Bluetooth/Location permission handling. Add necessary Hilt dependencies to build files.
- **Status:** COMPLETED
- **Updates:** Successfully set up Hilt DI, project architecture, and permission handling.
- **Acceptance Criteria:**
  - Hilt is correctly configured and the project builds.
  - Permission handling for Bluetooth (Scan, Connect, Advertise) and Location is implemented and works for Android 8+ and 12+.
  - Core data models for BleDevice and ConnectionState are defined.

### Task_2_BLE_Core: Implement the BLE Scanner and GATT Connection Manager. This includes scanning for devices with RSSI updates and managing a single active connection to receive data.
- **Status:** COMPLETED
- **Updates:** Implemented BleRepositoryImpl for scanning and connection management.
- **Acceptance Criteria:**
  - BLE Scanner discovers nearby devices and provides real-time RSSI updates.
  - Connection and disconnection logic for a single BLE device is functional.
  - Incoming data packets from the connected device are captured and exposed via Kotlin Flow.

### Task_3_UI_Compose: Develop the single-screen Compose UI using MVVM. Implement the device list, connection status indicators, and the terminal-style message panel.
- **Status:** COMPLETED
- **Updates:** Developed the single-screen Compose UI using MVVM.
- **Acceptance Criteria:**
  - UI matches the design provided in input_images/image_0.png.
  - Device list shows discovery status and signal strength.
  - Terminal panel correctly displays real-time incoming messages.
  - ViewModel manages the BLE state and UI interaction seamlessly using Hilt.

### Task_4_Verification: Apply a vibrant Material 3 color scheme, create an adaptive app icon, and perform final verification of the application.
- **Status:** COMPLETED
- **Updates:** Final verification completed successfully.
- Application is stable and builds successfully.
- UI matches the design in input_images/image_0.png.
- Vibrant Material 3 theme is applied correctly.
- Adaptive app icon is present and functional.
- Permission handling works as expected.
- All core features (Scanning, Connecting visuals, Terminal) are implemented and verified.
- Tested on phone and tablet emulators.
- **Acceptance Criteria:**
  - Vibrant, energetic Material 3 theme (Light/Dark) is applied.
  - Adaptive app icon matches the BleVButton function.
  - Application is stable (no crashes), builds successfully, and all existing tests pass.
  - Final app aligns with all project requirements.
- **Duration:** N/A

