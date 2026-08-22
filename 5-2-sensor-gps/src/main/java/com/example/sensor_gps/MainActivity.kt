package com.example.sensor_gps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sensor_gps.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

/**
 * MAINACTIVITY: GPS SENSOR DEMO
 *
 * This screen's ONE job is fetching a coordinate: check/request the runtime permission,
 * then read it via Google Play Services' `FusedLocationProviderClient`. It deliberately
 * does NOT show a map itself — that's MapActivity's job, launched once a coordinate is
 * available (see openMap() below). Splitting these into two screens keeps each one doing
 * one thing, and makes it obvious on screen that fetching a location and displaying it
 * on a map are two separate steps, not one.
 *
 * What is the FusedLocationProviderClient?
 * Google's advanced location client that combines (fuses) signals from GPS, Wi-Fi networks,
 * cell towers, and device sensors to calculate highly accurate location coordinates
 * while optimizing battery consumption.
 *
 * WHAT ARE RUNTIME PERMISSIONS?
 * Location is considered a "Dangerous Permission" because it impacts user privacy.
 * In modern Android, declaring a permission in the AndroidManifest.xml is NOT enough.
 * We must explicitly request the user's permission at RUNTIME (a pop-up dialog) before accessing GPS!
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Google's API for managing location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Configuration settings (frequency, accuracy) for location requests
    private lateinit var locationRequest: LocationRequest

    // The callback code that runs whenever the device calculates a new location update
    private lateinit var locationCallback: LocationCallback

    // Unique request ID code used to identify our runtime permission prompt
    private val Request_Code_Location = 22

    private lateinit var latttv: TextView
    private lateinit var longtv: TextView
    private lateinit var sourceLabel: TextView
    private lateinit var button: Button
    private lateinit var mapButton: Button

    // The most recently resolved coordinate (real fix or demo fallback), handed off
    // to MapActivity when mapButton is tapped. Null until updateLocation() resolves once.
    private var lastLat: Double? = null
    private var lastLng: Double? = null

    // DEMO FALLBACK — Bouverie Street, Carlton (this tutorial's teaching venue).
    // Street-level accuracy only (~city-block precision, not a rooftop-exact geocode
    // for the specific house number). The emulator has no real GPS fix until one is
    // injected — via Extended Controls -> Location, or `adb emu geo fix 144.9620 -37.8032`
    // — so lastLocation legitimately returns null until then. This fallback keeps the
    // demo from stalling on a blank screen; swap in the exact coordinate (long-press
    // the address in Google Maps, copy the lat/lng shown) if you want it precise.
    private val DEMO_FALLBACK_LAT = -37.8032
    private val DEMO_FALLBACK_LNG = 144.9620

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Initialize the Google Fused Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 2. Configure the LocationRequest:
        // - Set interval to 5000 milliseconds (request a location update every 5 seconds)
        @Suppress("DEPRECATION")
        locationRequest = LocationRequest.create().apply { interval = 5000 }

        // 3. Define what to do when a new location coordinate is received
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.d("LocationTest", "Received location update")

                // Get the most recent location and update our user interface
                locationResult.lastLocation?.let { updateUI(it) }
            }
        }

        latttv = binding.latitudeInput
        longtv = binding.longitudeInput
        sourceLabel = binding.sourceLabel
        button = binding.button
        mapButton = binding.mapButton

        button.setOnClickListener {
            updateLocation()
        }

        mapButton.setOnClickListener {
            openMap()
        }
    }

    /**
     * Triggers active GPS updates and retrieves the last known location.
     */
    private fun updateLocation() {
        // 1. Check if the user has already granted Location permissions
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // 2. Permission is GRANTED! Request active, repeating location updates
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

            // 3. Also query the cache to immediately grab the last known location (instant load)
            fusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    if (location == null) {
                        // Expected on a fresh emulator with no injected/cached fix yet —
                        // fall back to the demo location instead of leaving the screen blank.
                        Log.d("LocationTest", "Last location cache is null — showing demo fallback location")
                        showLocation(DEMO_FALLBACK_LAT, DEMO_FALLBACK_LNG, isRealFix = false)
                    } else {
                        Log.d("LocationTest", "Successfully retrieved cached location")
                        updateUI(location)
                    }
                }
        } else {
            // 4. Permission is NOT granted! Show the system dialog prompting the user for permission
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Request_Code_Location
            )
        }
    }

    /**
     * Updates the screen's TextViews with a real GPS fix.
     */
    private fun updateUI(location: Location) {
        showLocation(location.latitude, location.longitude, isRealFix = true)
    }

    /**
     * Single place that updates the lat/long text, the "where did this come from" label,
     * and remembers the coordinate for openMap() — used by both the real-fix path
     * (updateUI) and the demo-fallback path (updateLocation's null branch), so the two
     * paths can never drift out of sync with each other.
     */
    private fun showLocation(lat: Double, lng: Double, isRealFix: Boolean) {
        latttv.text = lat.toString()
        longtv.text = lng.toString()
        sourceLabel.text = if (isRealFix) {
            "Source: real GPS fix"
        } else {
            "Source: demo fallback (Bouverie Street — not a real GPS fix)"
        }

        lastLat = lat
        lastLng = lng
        mapButton.isEnabled = true
    }

    /**
     * Hands the most recently resolved coordinate off to MapActivity. Only reachable
     * once mapButton is enabled, which only happens after showLocation() has run once.
     */
    private fun openMap() {
        val lat = lastLat ?: return
        val lng = lastLng ?: return
        startActivity(
            Intent(this, MapActivity::class.java)
                .putExtra(MapActivity.EXTRA_LAT, lat)
                .putExtra(MapActivity.EXTRA_LNG, lng)
        )
    }

    /**
     * LOCATION LIFECYCLE MANAGEMENT FOR ENERGY CONSERVATION:
     *
     * GPS hardware consumes high amounts of battery power. When the Activity is no longer visible
     * to the user, we override the `onStop()` method to explicitly tell the location client to
     * stop active GPS location updates. This prevents active GPS tracking when the app is backgrounded,
     * conserving device battery life!
     */
    override fun onStop() {
        // Stop active GPS tracking immediately when screen goes out of view
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("LocationTest", "Location updates stopped to conserve battery.")
        super.onStop()
    }

    /**
     * Callback method triggered automatically when the user clicks "Allow" or "Deny"
     * on the runtime permission pop-up dialog.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Request_Code_Location) {
            // Check if the first permission in our array was granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Success! Re-run location fetch
                updateLocation()
            }
        }
    }
}
