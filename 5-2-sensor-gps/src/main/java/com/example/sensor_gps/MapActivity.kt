package com.example.sensor_gps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sensor_gps.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * MAPACTIVITY: SHOW ONE COORDINATE ON GOOGLE MAPS
 *
 * A separate screen from MainActivity on purpose. MainActivity's job was fetching a
 * coordinate — checking the runtime permission, reading the fused location, or falling
 * back to a demo coordinate if neither is available. This Activity has exactly one job:
 * take the coordinate it was handed and show it on a real map. No permission logic here
 * at all — it never asks the device for its own location.
 *
 * Receives the coordinate via Intent extras (EXTRA_LAT / EXTRA_LNG) set by
 * MainActivity.openMap().
 */
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"
    }

    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Same pattern as any fragment-based screen: find the fragment declared in the
        // layout XML, then ask it to notify us via onMapReady() once the map is usable.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Fires once the map's tiles/services are ready to receive markers/camera moves.
     * Everything before this point (Intent extras, fragment lookup) can happen
     * immediately in onCreate() — only drawing on the map has to wait for this callback.
     */
    override fun onMapReady(map: GoogleMap) {
        val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
        val lng = intent.getDoubleExtra(EXTRA_LNG, 0.0)
        val here = LatLng(lat, lng)

        map.addMarker(MarkerOptions().position(here).title("Selected location"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 16f))
    }
}
