package com.example.connectivity_bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.connectivity_bluetooth.databinding.ActivityMainBinding
import com.example.connectivity_bluetooth.databinding.ItemBleDeviceBinding

/**
 * Tutorial 7-4: Bluetooth Low Energy (BLE) Scanner Demo
 *
 * This is written as a reference for the BLE best practices you'll want in
 * your own project, not just a one-off demo:
 *   1. Runtime permission request for BLUETOOTH_SCAN / BLUETOOTH_CONNECT (API 31+)
 *   2. Checking if Bluetooth is enabled and prompting the user to enable it
 *   3. Scanning with an auto-stop timeout, deduplicated by MAC address
 *   4. Stopping the scan before connecting (a device won't connect reliably
 *      while the radio is still busy scanning)
 *   5. Connecting to a chosen device with BluetoothGatt and discovering its
 *      GATT services/characteristics
 *   6. Always closing the previous GATT connection before opening a new one,
 *      and always closing it in onDestroy -- BluetoothGatt objects are a
 *      limited system resource (Android allows ~7 concurrent connections)
 *
 * BLE scanning and connecting only work on a real device -- the emulator has
 * no Bluetooth radio.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Bluetooth system service entry points
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null

    // The one GATT connection this demo keeps open at a time.
    private var activeGatt: BluetoothGatt? = null

    // Scan auto-stops after this many milliseconds (battery safety)
    private val scanPeriodMs = 10_000L
    private var isScanning = false
    private val scanHandler = Handler(Looper.getMainLooper())

    // Collected scan results, keyed by MAC address so re-advertising devices update in place
    private val discoveredDevices = linkedMapOf<String, BleDevice>()

    // -----------------------------------------------------------------------
    // Runtime permission launcher (API 31+)
    // -----------------------------------------------------------------------
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            startBleScan()
        } else {
            binding.statusText.text = "Bluetooth permissions denied. Cannot scan."
            binding.statusText.setTextColor(getColor(R.color.status_error))
        }
    }

    // Launcher to ask user to enable Bluetooth
    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (bluetoothAdapter?.isEnabled == true) {
            checkPermissionsAndScan()
        } else {
            binding.statusText.text = "Bluetooth is disabled. Enable it to scan."
            binding.statusText.setTextColor(getColor(R.color.status_error))
        }
    }

    // -----------------------------------------------------------------------
    // BLE Scan Callback
    // -----------------------------------------------------------------------

    /**
     * ScanCallback fires once per advertisement packet (repeatedly for the same
     * device as long as it keeps advertising). Runs on a background thread --
     * post UI updates to the Main Thread.
     */
    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: "Unknown"
            // Update in place if we've seen this MAC before, so RSSI stays live
            // instead of piling up duplicate rows for the same device.
            discoveredDevices[device.address] = BleDevice(name, device.address, result.rssi, device)
            runOnUiThread { refreshDeviceList() }
        }

        override fun onScanFailed(errorCode: Int) {
            runOnUiThread {
                binding.statusText.text = "Scan failed (error $errorCode). " +
                    "Ensure Bluetooth and Location are enabled."
                binding.statusText.setTextColor(getColor(R.color.status_error))
            }
        }
    }

    // -----------------------------------------------------------------------
    // GATT Callback (for connecting to a device)
    // -----------------------------------------------------------------------

    /**
     * BluetoothGattCallback fires on connection state changes and service discovery.
     * Callbacks run on a Binder thread -- use runOnUiThread for UI updates.
     */
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "GATT connected. Discovering services...")
                    runOnUiThread {
                        binding.statusText.text = "Connected. Discovering services..."
                        binding.statusText.setTextColor(getColor(R.color.status_connected))
                    }
                    gatt.discoverServices()  // must be called to enumerate services
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "GATT disconnected")
                    runOnUiThread {
                        binding.statusText.text = "Disconnected."
                        binding.statusText.setTextColor(getColor(R.color.status_idle))
                    }
                    gatt.close()
                    if (activeGatt === gatt) activeGatt = null
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val serviceList = gatt.services.joinToString("\n\n") { service ->
                    "Service: ${service.uuid}\n" +
                        service.characteristics.joinToString("\n") { char ->
                            "  Characteristic: ${char.uuid}"
                        }
                }
                runOnUiThread {
                    binding.statusText.text = "Services discovered:"
                    showRawText(serviceList)
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            binding.statusText.text = "This device does not support Bluetooth."
            binding.scanButton.isEnabled = false
            return
        }

        binding.scanButton.setOnClickListener {
            if (isScanning) stopBleScan() else checkPermissionsAndScan()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBleScan()
        // Always close GATT to free the connection slot (Android limits ~7 concurrent).
        activeGatt?.close()
        activeGatt = null
    }

    // -----------------------------------------------------------------------
    // Scan logic
    // -----------------------------------------------------------------------

    private fun checkPermissionsAndScan() {
        // On API 31+, BLUETOOTH_SCAN and BLUETOOTH_CONNECT are Dangerous permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val missing = listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ).filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }

            if (missing.isNotEmpty()) {
                requestPermissionsLauncher.launch(missing.toTypedArray())
                return
            }
        }

        // Check Bluetooth is enabled
        if (bluetoothAdapter?.isEnabled == false) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        startBleScan()
    }

    private fun startBleScan() {
        discoveredDevices.clear()
        refreshDeviceList()
        binding.statusText.text = "Scanning... (10s)"
        binding.statusText.setTextColor(getColor(R.color.status_scanning))
        binding.scanButton.text = "Stop Scan"
        isScanning = true

        bleScanner = bluetoothAdapter?.bluetoothLeScanner
        bleScanner?.startScan(scanCallback)

        // Auto-stop after scanPeriodMs to conserve battery
        scanHandler.postDelayed({ stopBleScan() }, scanPeriodMs)
    }

    private fun stopBleScan() {
        if (isScanning) {
            bleScanner?.stopScan(scanCallback)
            isScanning = false
            binding.scanButton.text = "Scan"
            binding.statusText.text = "Found ${discoveredDevices.size} device(s). Tap one to connect."
            binding.statusText.setTextColor(getColor(R.color.status_idle))
            scanHandler.removeCallbacksAndMessages(null)
        }
    }

    // -----------------------------------------------------------------------
    // Connect logic
    // -----------------------------------------------------------------------

    /**
     * Connects to a device the user tapped in the results list.
     *
     * Best practices applied here:
     *  - Stop scanning first -- connecting is more reliable with the radio
     *    not also busy scanning.
     *  - Close any previous GATT connection before opening a new one, so a
     *    student tapping a second device doesn't silently leak the first
     *    connection.
     *  - autoConnect = false -- correct for "user just tapped this nearby
     *    device", which connects immediately; autoConnect = true is for
     *    background reconnection to a device you expect to appear later.
     */
    private fun connectToDevice(device: BleDevice) {
        stopBleScan()
        activeGatt?.close()

        binding.statusText.text = "Connecting to ${device.name}..."
        binding.statusText.setTextColor(getColor(R.color.status_scanning))
        activeGatt = device.device.connectGatt(this, /* autoConnect = */ false, gattCallback)
    }

    // -----------------------------------------------------------------------
    // List rendering
    // -----------------------------------------------------------------------

    private fun refreshDeviceList() {
        val container = binding.deviceListContainer
        container.removeAllViews()

        if (discoveredDevices.isEmpty()) {
            val empty = ItemBleDeviceBinding.inflate(layoutInflater, container, false)
            empty.deviceName.text = if (isScanning) "Scanning..." else "No devices yet. Press Scan."
            empty.deviceName.isEnabled = false
            empty.deviceMeta.text = ""
            container.addView(empty.root)
            return
        }

        for (device in discoveredDevices.values) {
            val row = ItemBleDeviceBinding.inflate(layoutInflater, container, false)
            row.deviceName.text = device.name
            row.deviceMeta.text = "${device.address}   ${device.rssi} dBm"
            row.root.setOnClickListener { connectToDevice(device) }
            container.addView(row.root)
        }
    }

    /** Swaps the results list for a plain block of text (used to show discovered GATT services). */
    private fun showRawText(text: String) {
        val container = binding.deviceListContainer
        container.removeAllViews()
        val row = ItemBleDeviceBinding.inflate(layoutInflater, container, false)
        row.deviceName.text = "Services"
        row.deviceMeta.text = text
        container.addView(row.root)
    }

    companion object {
        private const val TAG = "BLEDemo"
    }
}
