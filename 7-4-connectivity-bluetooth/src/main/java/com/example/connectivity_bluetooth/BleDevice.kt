package com.example.connectivity_bluetooth

/**
 * Simple data class representing a discovered BLE device.
 *
 * @param name    Advertised name, or "Unknown" if not broadcasting a name.
 * @param address MAC address (e.g. "AA:BB:CC:DD:EE:FF"). Unique identifier for the device.
 * @param rssi    Signal strength in dBm. Closer to 0 = stronger signal. -40 is very close, -90 is weak.
 */
data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int
)
