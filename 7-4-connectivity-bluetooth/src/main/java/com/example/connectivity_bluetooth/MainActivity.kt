package com.example.connectivity_bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
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
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.connectivity_bluetooth.databinding.ActivityMainBinding

/**
 * Tutorial 7-4: Bluetooth Low Energy (BLE) Scanner Demo
 *
 * Demonstrates:
 *   1. Runtime permission request for BLUETOOTH_SCAN / BLUETOOTH_CONNECT (API 31+)
 *   2. Checking if Bluetooth is enabled and prompting the user to enable it
 *   3. BLE device scanning using BluetoothLeScanner + ScanCallback
 *   4. Connecting to a discovered BLE device with BluetoothGatt
 *   5. Discovering GATT services and listing their UUIDs
 *
 * BLE scanning only works on a real device -- the emulator has no BT radio.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Bluetooth system service entry points
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null

    // Tracks currently open GATT connection so we can close it in onDestroy
    private var activeGatt: BluetoothGatt? = null

    // Scan auto-stops after this many milliseconds (battery safety)
    private val SCAN_PERIOD_MS = 10_000L
    private var isScanning = false
    private val scanHandler = Handler(Looper.getMainLooper())

    // Collected scan results
    private val discoveredDevices = mutableListOf<BleDevice>()
    private val displayLines = mutableListOf<String>()

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
        }
    }

    // Launcher to ask user to enable Bluetooth
    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (bluetoothAdapter?.isEnabled == true) {
            checkPermissionsAndScan()
        } else {
            binding.statusText.text = "Bluetooth is disabled. Enable it to scan."
        }
    }

    // -----------------------------------------------------------------------
    // BLE Scan Callback
    // -----------------------------------------------------------------------

    /**
     * ScanCallback fires once per discovered device (or periodically for re-advertising devices).
     * Runs on a background thread -- post UI updates to the Main Thread.
     */
    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: "Unknown"
            val address = device.address
            val rssi = result.rssi

            // Avoid duplicates by MAC address
            if (discoveredDevices.none { it.address == address }) {
                discoveredDevices.add(BleDevice(name, address, rssi))
                runOnUiThread { refreshDeviceList() }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            runOnUiThread {
                binding.statusText.text = "Scan failed (error $errorCode). " +
                    "Ensure Bluetooth and Location are enabled."
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
                    runOnUiThread { binding.statusText.text = "Connected! Discovering services..." }
                    gatt.discoverServices()  // must be called to enumerate services
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "GATT disconnected")
                    runOnUiThread { binding.statusText.text = "Disconnected." }
                    gatt.close()
                    activeGatt = null
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val serviceList = gatt.services.joinToString("\n") { service ->
                    "Service: ${service.uuid}\n" +
                    service.characteristics.joinToString("\n") { char ->
                        "  Characteristic: ${char.uuid}"
                    }
                }
                runOnUiThread {
                    binding.statusText.text = "Services discovered:"
                    binding.deviceListText.text = serviceList
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

        binding.statusText.text = "Press Scan to discover nearby BLE devices."
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBleScan()
        // Always close GATT to free connection slots (Android limits ~7 concurrent)
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
        displayLines.clear()
        binding.deviceListText.text = ""
        binding.statusText.text = "Scanning... (10s)"
        binding.scanButton.text = "Stop Scan"
        isScanning = true

        bleScanner = bluetoothAdapter?.bluetoothLeScanner
        bleScanner?.startScan(scanCallback)

        // Auto-stop after SCAN_PERIOD_MS to conserve battery
        scanHandler.postDelayed({ stopBleScan() }, SCAN_PERIOD_MS)
    }

    private fun stopBleScan() {
        if (isScanning) {
            bleScanner?.stopScan(scanCallback)
            isScanning = false
            binding.scanButton.text = "Scan"
            binding.statusText.text = "Scan complete. Found ${discoveredDevices.size} device(s). Tap a device to connect."
            scanHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun refreshDeviceList() {
        val text = discoveredDevices.mapIndexed { i, d ->
            "[${i + 1}] ${d.name}\n     ${d.address}   RSSI: ${d.rssi} dBm"
        }.joinToString("\n\n")
        binding.deviceListText.text = text
        binding.statusText.text = "Scanning... found ${discoveredDevices.size} device(s)"
    }

    companion object {
        private const val TAG = "BLEDemo"
    }
}
