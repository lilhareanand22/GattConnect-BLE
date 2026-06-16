# GattConnect-BLE



GattConnect-BLE is a production-ready Android template demonstrating how to scan for, connect to, and interact with Bluetooth Low Energy (BLE) peripherals. It showcases a modern architecture for handling the complexities of the Android Bluetooth GATT stack with a focus on reliability and clean code.

## 🚀 Features

- **Real-time BLE Scanning**: Efficient discovery of nearby BLE devices with live RSSI (signal strength) updates.
- **Generic GATT Support**: Robust implementation for discovering services, reading/writing characteristics, and handling notifications.
- **Asynchronous State Management**: Clean handling of connection states and data streams using Kotlin Flows.
- **Event Logging**: A terminal-style monitor to track GATT events and data exchanges in real-time.
- **Material 3 UI**: A responsive, modern interface with dynamic color support and adaptive layouts.

## 🛠 Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Dependency Injection**: Hilt
- **Architecture**: MVVM + Clean Architecture
- **Concurrency**: Kotlin Coroutines & Flows
- **SDK**: targetSdk 37, minSdk 24

## 📐 Architecture

The project follows Clean Architecture principles, ensuring that Bluetooth hardware logic is decoupled from the UI.


## 🔄 BLE Integration Approaches

This project emphasizes the transition from older Android patterns to modern, reactive programming.

### Legacy Approach (The "Callback Hell")
Traditional BLE development in Android relies heavily on `BluetoothGattCallback`. This often leads to:
- Deeply nested callbacks that are hard to read and maintain.
- Difficulty managing threading (GATT callbacks often arrive on Binder threads).
- Fragile state management when handling sequential operations (like write-then-read).

### Modern Approach (The Reactive Way)
This implementation leverages modern Kotlin features to solve these issues:
- **Coroutines**: Simplifies asynchronous tasks, allowing for sequential-looking code for complex GATT handshakes.
- **Flows/StateFlow**: Converts GATT call[gradle](gradle)backs into observable streams, making it easy for the UI to react to state changes (Connecting, Connected, Disconnected).
- **Structured Concurrency**: Ensures Bluetooth operations are tied to appropriate lifecycles, preventing memory leaks and orphaned connections.

## ⚙️ Generic Workflow

### 1. Discovery
The app utilizes `BluetoothLeScanner` with optimized scan filters. RSSI values are streamed through a `SharedFlow` to provide real-time UI updates without unnecessary recompositions.

### 2. Connection & Service Discovery
Upon connection, the app triggers `discoverServices()`. This implementation demonstrates how to safely traverse the GATT tree to identify specific characteristics required for your device's protocol.

### 3. Data Interaction
The template provides clean patterns for:
- **Writing**: Sending configuration or command bytes to characteristics.
- **Notifications**: Subscribing to characteristic changes to receive data from the peripheral asynchronously.
- **Parsing**: A dedicated layer for converting raw byte arrays into meaningful domain models.

## 📦 Installation

1. Clone the repository.
2. Open in Android Studio (Ladybug or newer recommended).
3. Ensure Bluetooth and Location permissions are granted on the test device.
4. Please add own BLE device UUID as per device configuration
5. Build and run on an Android 8.0 (API 26) or higher device.