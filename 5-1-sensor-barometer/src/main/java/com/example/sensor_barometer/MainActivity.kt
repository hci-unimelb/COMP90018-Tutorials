package com.example.sensor_barometer

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sensor_barometer.databinding.ActivityMainBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * MAINACTIVITY: BAROMETER DISPLAY
 *
 * This activity displays atmospheric pressure reading in two forms:
 *  1. **Text View**: Displays the raw numeric hectopascal (hPa) value.
 *  2. **Image Gauge**: Rotates a dial pointer image based on the pressure value.
 *
 * It uses the EventBus library to receive pressure updates asynchronously from our `Barometer`
 * sensor listener class.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var sensorLabel: TextView
    private lateinit var pressure: TextView
    private lateinit var barometer: Barometer
    private lateinit var switchLayout: Button

    private var output = OutputVisibility.Text

    // Constants used to calculate the rotation angle of the physical needle gauge image
    private val ROT_ZERO = -125f // Base rotation representing 0 pressure
    private val ROT_VALUE = 0.893f // Rotation angle multiplier per 1 unit of pressure

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorLabel = binding.sensorLabel
        pressure = binding.barometerValue
        switchLayout = binding.button

        // Initialize the pointer needle at its base rotation (-125 degrees)
        binding.point.rotation = ROT_ZERO

        // Button to toggle visibility states (Text, Gauge, or Both)
        switchLayout.setOnClickListener {
            // Cycle through our enum values [Text -> Image -> Both -> Text...]
            output = OutputVisibility.values()[(output.ordinal + 1) % 3]

            when (output) {
                OutputVisibility.Image -> {
                    binding.panel.visibility = View.VISIBLE
                    binding.point.visibility = View.VISIBLE
                    binding.sensorLabel.visibility = View.INVISIBLE
                    binding.barometerValue.visibility = View.INVISIBLE
                }
                OutputVisibility.Text -> {
                    binding.panel.visibility = View.INVISIBLE
                    binding.point.visibility = View.INVISIBLE
                    binding.sensorLabel.visibility = View.VISIBLE
                    binding.barometerValue.visibility = View.VISIBLE
                }
                OutputVisibility.Both -> {
                    binding.panel.visibility = View.VISIBLE
                    binding.point.visibility = View.VISIBLE
                    binding.sensorLabel.visibility = View.VISIBLE
                    binding.barometerValue.visibility = View.VISIBLE
                }
            }
        }

        // Initialize our Barometer helper class.
        // Note: We do NOT start listening immediately inside onCreate() anymore!
        barometer = Barometer(this, sensorLabel)
    }

    /**
     * LIFECYCLE MANAGEMENT FOR ENERGY EFFICIENCY:
     *
     * Sensors (like barometers, gyroscopes, and GPS) consume significant battery power.
     * If an app continues to listen to sensors while in the background, it will drain the user's
     * battery rapidly and may get killed by the operating system.
     *
     * Best Practice:
     * - **onStart()**: Screen becomes visible $\rightarrow$ Register EventBus & start sensor updates.
     * - **onStop()**: Screen is hidden $\rightarrow$ Unregister EventBus & stop sensor updates immediately!
     */
    override fun onStart() {
        super.onStart()
        // Register this Activity as an EventBus subscriber
        EventBus.getDefault().register(this)
        
        // Start listening to the hardware barometer
        barometer.enableSensor()
    }

    override fun onStop() {
        // Stop listening to the sensor immediately to save battery!
        barometer.disableSensor()
        
        // Unregister EventBus to prevent memory leaks
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    fun setSensorLabel(s: String) {
        sensorLabel.text = s
    }

    /**
     * EVENTBUS CALLBACK:
     * Triggered automatically on the Main UI Thread when `Barometer` posts a new `BarometerMessage`.
     * We update the raw text display and rotate the needle gauge image accordingly.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessage(event: BarometerMessage) {
        pressure.text = event.pressure.toString()
        // Rotate the needle graphic: Base rotation + (Pressure * scale)
        binding.point.rotation = ROT_ZERO + (event.pressure * ROT_VALUE)
    }

    private enum class OutputVisibility {
        Text,
        Image,
        Both
    }
}
