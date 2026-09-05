package com.example.connectivity_bluetooth

import android.bluetooth.BluetoothDevice

/**
 * A discovered BLE device, plus the raw [BluetoothDevice] needed to connect to it.
 *
 * @param name    Advertised name, or "Unknown" if not broadcasting a name.
 * @param address MAC address (e.g. "AA:BB:CC:DD:EE:FF"). Unique identifier for the device.
 * @param rssi    Signal strength in dBm. Closer to 0 = stronger signal. -40 is very close, -90 is weak.
 * @param device  The underlying system handle — pass this straight to `connectGatt()`, no re-lookup needed.
 */
data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val device: BluetoothDevice
)
