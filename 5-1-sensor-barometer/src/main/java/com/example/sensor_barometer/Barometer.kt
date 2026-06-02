package com.example.sensor_barometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.TextView
import org.greenrobot.eventbus.EventBus

/**
 * BAROMETER SENSOR CONTROLLER
 *
 * This class implements 'SensorEventListener' which allows it to receive data updates from the
 * device's hardware sensors.
 *
 * What is a Barometer?
 * A barometer measures atmospheric pressure. Mobile devices use it to detect altitude changes
 * (e.g., determining if a user is walking upstairs).
 *
 * How does the Android Sensor Framework work?
 *  1. Get the SENSOR_SERVICE from the Android system.
 *  2. Retrieve the specific hardware sensor (Sensor.TYPE_PRESSURE).
 *  3. Register this class as a "Listener" to start receiving stream values at a specified delay rate.
 */
class Barometer(
    private val mContext: Context, 
    private val sensorLabel: TextView
) : SensorEventListener {

    private var mSensorManager: SensorManager? = null
    private var sensor: Sensor? = null

    // We specify that we want to listen to the atmospheric pressure sensor
    private val primarySensor = Sensor.TYPE_PRESSURE

    /**
     * ENERGY CONSERVATION BEST PRACTICE:
     * Hardware sensors consume significant battery power. To conserve resources, we design the controller
     * with explicit `enableSensor()` and `disableSensor()` methods. This allows the host Activity to
     * easily register and unregister the sensor in alignment with its lifecycle states (onStart/onStop
     * or onResume/onPause), preventing background battery drain!
     */

    /**
     * Registers this class to start listening to the physical barometer hardware sensor.
     */
    fun enableSensor() {
        Log.v("Sensor...", "Enabling sensor...")
        
        // 1. Fetch the SensorManager which manages all hardware sensors on the device
        mSensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        Log.v("Sensor...", if (mSensorManager == null) "Sensors not supported" else "Sensors are supported")

        val manager = mSensorManager
        if (manager == null) {
            // Devices (or standard emulators) might not have physical sensors. Display an error message.
            sensorLabel.text = mContext.resources.getString(R.string.sensor_invalid)
            return
        }

        // 2. Locate the default physical pressure sensor on the hardware
        val foundSensor = manager.getDefaultSensor(primarySensor)
        sensor = foundSensor
        
        if (foundSensor == null) {
            // No pressure sensor exists on this device
            Log.v("Sensor..", "Pressure sensor is not supported")
            sensorLabel.text = mContext.resources.getString(R.string.pressure_sensor_invalid)
        } else {
            // Pressure sensor found! Register our class to receive its updates
            Log.v("Sensor..", "${foundSensor.name} is supported")
            
            // We request updates at 'SENSOR_DELAY_NORMAL' speed (suitable for general UI updates)
            manager.registerListener(this, foundSensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorLabel.text = mContext.resources.getString(R.string.pressure_label)
        }
    }

    /**
     * Unregisters our listener to stop receiving sensor updates.
     * ALWAYS call this when your Activity goes into the background to prevent heavy battery drain!
     */
    fun disableSensor() {
        mSensorManager?.let {
            it.unregisterListener(this)
            mSensorManager = null
            Log.v("Sensor...", "Sensor disabled and unregistered successfully.")
        }
    }

    /**
     * ONSENSORCHANGED:
     * This callback method runs automatically whenever the hardware detects a change in pressure values!
     *
     * @sensorEvent Contains the new sensor data values and accuracy status
     */
    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (sensorEvent.sensor == null) {
            Log.v("Sensor...", "OnSensorChanged.sensor = null")
            return
        }

        // Check if the update is coming from our pressure sensor
        if (sensorEvent.sensor.type == sensor?.type) {
            Log.v("Sensor...", "Posting pressure value: ${sensorEvent.values[0]}")
            
            // Retrieve the first pressure value (index 0 is the hectopascal / hPa value)
            val pressureValue = sensorEvent.values[0]
            
            // Post this value using EventBus so our Activity can receive it and update the gauge UI
            EventBus.getDefault().post(BarometerMessage(pressureValue))
        }
    }

    /**
     * Triggered if the accuracy of the sensor changes (e.g. going from low accuracy to high accuracy).
     * We don't need this for our basic demo, but we must implement the interface method.
     */
    override fun onAccuracyChanged(sensor: Sensor, i: Int) {
        // No action needed
    }
}
