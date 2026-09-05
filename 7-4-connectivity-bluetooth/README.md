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
| Live-updating scan results, keyed by MAC | `LinkedHashMap` in `onScanResult` |
| Stopping the scan before connecting | `connectToDevice()` calls `stopBleScan()` first |
| Tap a result to connect | `item_ble_device.xml` row + `connectToDevice()` |
| GATT connection to a device | `BluetoothDevice.connectGatt()` |
| Closing the previous connection before opening a new one | `activeGatt?.close()` in `connectToDevice()` |
| Service & characteristic discovery | `BluetoothGatt.discoverServices()` + `BluetoothGattCallback` |
| Releasing the GATT connection on exit | `activeGatt?.close()` in `onDestroy()` |

> **Fixed from an earlier pass of this demo:** the UI used to say "tap a device to connect" but the list wasn't clickable and nothing ever called `connectGatt()` — and `activeGatt` was never assigned on connect, so the `onDestroy()` cleanup wasn't actually closing anything. Both are wired up correctly now; see `connectToDevice()`.

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
A plain Kotlin `data class` holding the fields surfaced for each discovered device: `name`, `address` (MAC), `rssi` (signal strength in dBm), and the raw `BluetoothDevice` handle — kept alongside the display fields so `connectToDevice()` never needs to re-look the device up by address.

### `MainActivity.kt`
All BLE logic lives here for tutorial clarity. Key components:

- **`BluetoothManager` / `BluetoothAdapter`** — entry point to the Bluetooth subsystem. `BluetoothManager` is obtained via `getSystemService`; the adapter is retrieved from it.
- **`BluetoothLeScanner`** — obtained from the adapter. Exposes `startScan` / `stopScan`.
- **`ScanCallback`** — anonymous object. `onScanResult` fires on the scanner thread for each advertisement packet received; results are stored in a `LinkedHashMap<address, BleDevice>` so a device seen again just updates its RSSI in place.
- **`connectToDevice()`** — stops the scan, closes any previous `activeGatt`, then calls `device.connectGatt(this, false, gattCallback)` and keeps the result in `activeGatt`.
- **`BluetoothGattCallback`** — anonymous object handling async GATT events. `onConnectionStateChange` fires when the link comes up or drops; `onServicesDiscovered` fires after `discoverServices()` completes.
- **`scanHandler`** — a `Handler` on the main looper used to post the auto-stop runnable after 10 seconds, protecting battery.

### `activity_main.xml` + `item_ble_device.xml`
A `ConstraintLayout` with a title/subtitle, a **Scan/Stop** `Button` next to an inline status `TextView`, and a `ScrollView` wrapping a `LinearLayout` container. Each scan result (and, later, the discovered-services text) is inflated from `item_ble_device.xml` — a small clickable row (name + monospace MAC/RSSI) using `?attr/selectableItemBackground` for the tap ripple. That row layout is deliberately its own file: it's the same shape a RecyclerView item would take, so swapping the manual container for a `RecyclerView` later is a drop-in change, not a rewrite.

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
