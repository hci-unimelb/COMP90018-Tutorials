# 7-4 Connectivity: Bluetooth Low Energy (BLE)

This module demonstrates Bluetooth Low Energy (BLE) scanning and GATT connection on Android using the standard SDK APIs — no third-party libraries required.

---

## What it demonstrates

| Concept | Where |
|---|---|
| Runtime permission requests (API 31+) | `MainActivity` — `requestPermissionsLauncher` |
| Prompting user to enable Bluetooth | `MainActivity` — `enableBtLauncher` |
| BLE device scanning | `BluetoothLeScanner.startScan()` + `ScanCallback` |
| Auto-stop scan (battery safety) | `Handler.postDelayed()` with 10 s timeout |
| Deduplication of scan results | MAC address check in `onScanResult` |
| GATT connection to a device | `BluetoothDevice.connectGatt()` |
| Service & characteristic discovery | `BluetoothGatt.discoverServices()` + `BluetoothGattCallback` |

---

## Important: Real device required

BLE scanning requires actual Bluetooth hardware. The Android emulator has **no Bluetooth radio**, so scanning will always return zero results (or fail outright). You must run this app on a physical Android device.

---

## Permissions explained

### API 31+ (Android 12+)
Android 12 replaced the legacy Bluetooth permissions with fine-grained ones:

| Permission | Why needed |
|---|---|
| `BLUETOOTH_SCAN` | Start/stop BLE scans |
| `BLUETOOTH_CONNECT` | Connect to GATT servers, read device name |

Both are **Dangerous** permissions — the app requests them at runtime via `ActivityResultContracts.RequestMultiplePermissions`.

`BLUETOOTH_SCAN` is declared with `android:usesPermissionFlags="neverForLocation"` because this app does not derive location from scan results. This avoids the need for `ACCESS_FINE_LOCATION` on API 31+.

### API 30 and below
Older APIs used:
- `BLUETOOTH` — basic adapter access
- `BLUETOOTH_ADMIN` — scan and pair
- `ACCESS_FINE_LOCATION` — required for BLE scans (OS enforced)

These are declared with `android:maxSdkVersion="30"` so they are not requested on newer devices.

---

## How to run

1. Connect a physical Android device (API 33+) with USB debugging enabled.
2. Open the project in Android Studio.
3. Select the `7-4-connectivity-bluetooth` run configuration.
4. Run. Grant the Bluetooth permissions when prompted.
5. Press **Scan** — nearby BLE devices appear within seconds.
6. Press **Stop Scan** or wait 10 s for the scan to auto-stop.

---

## Architecture overview

### `BleDevice.kt`
A plain Kotlin `data class` holding the three fields surfaced for each discovered device: `name`, `address` (MAC), and `rssi` (signal strength in dBm). Keeping it separate from `MainActivity` makes it easy to extend (e.g., add a `scanRecord` field later).

### `MainActivity.kt`
All BLE logic lives here for tutorial clarity. Key components:

- **`BluetoothManager` / `BluetoothAdapter`** — entry point to the Bluetooth subsystem. `BluetoothManager` is obtained via `getSystemService`; the adapter is retrieved from it.
- **`BluetoothLeScanner`** — obtained from the adapter. Exposes `startScan` / `stopScan`.
- **`ScanCallback`** — anonymous object. `onScanResult` fires on the scanner thread for each advertisement packet received. Results are deduplicated by MAC address before updating the UI.
- **`BluetoothGattCallback`** — anonymous object handling async GATT events. `onConnectionStateChange` fires when the link comes up or drops; `onServicesDiscovered` fires after `discoverServices()` completes.
- **`scanHandler`** — a `Handler` on the main looper used to post the auto-stop runnable after 10 seconds, protecting battery.

### `activity_main.xml`
A single `ConstraintLayout` with:
- A **title** `TextView`
- A **Scan/Stop** `Button` (toggles label and scan state)
- A **status** `TextView` (shows current state in blue)
- A `ScrollView` wrapping a monospace `TextView` for the device/service list

---

## Classic Bluetooth vs BLE — when to use which

| | Classic Bluetooth (BR/EDR) | Bluetooth Low Energy (BLE) |
|---|---|---|
| **Power** | High | Very low |
| **Throughput** | Up to ~3 Mbps | Up to ~2 Mbps (BT 5) |
| **Range** | ~10–100 m | ~10–400 m (BT 5) |
| **Typical uses** | Audio streaming, file transfer, keyboards | Sensors, beacons, wearables, IoT |
| **Android API** | `BluetoothSocket`, `BluetoothServerSocket` | `BluetoothLeScanner`, `BluetoothGatt` |
| **Connection setup** | Slower (pairing may be required) | Faster (no pairing needed for scan) |

Use **BLE** when your peripheral is a sensor, health device, beacon, or any device that transmits small amounts of data infrequently. Use **Classic Bluetooth** when you need sustained high-bandwidth connections (e.g., audio headsets).
