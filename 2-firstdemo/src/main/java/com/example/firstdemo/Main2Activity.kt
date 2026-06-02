package com.example.firstdemo

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdemo.databinding.ActivityMain2Binding

/**
 * [Main2Activity] - The Second Screen of the App
 *
 * This screen is launched by MainActivity. It extracts parameters from the launch request,
 * displays it, and handles returning a response package back when closed.
 */
class Main2Activity : AppCompatActivity() {

    // ViewBinding reference to access widgets in activity_main2.xml
    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup edge-to-edge UI drawing
        enableEdgeToEdge()
        
        // Inflate the XML layout into Kotlin objects
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Read the string data passed by the calling screen.
        // We look inside the intent extras bundle using the shared key constant: MainActivity.MESSAGE
        binding.previousMessage.text = intent.getStringExtra(MainActivity.MESSAGE)

        /**
         * Handling Back Navigation (Modern Android API):
         * We register an OnBackPressedCallback to intercept when the user presses the system back button
         * or uses the back swipe gesture. This lets us run code BEFORE the screen closes.
         */
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 1. Create a blank Intent to hold the return data package
                val returnIntent = Intent()
                
                // 2. Attach our return text under our unique key RECEIVED_MESSAGE
                returnIntent.putExtra(RECEIVED_MESSAGE, "Hello from the second activity.")
                
                // 3. Set the result status to OK (success) and attach our return intent data
                setResult(RESULT_OK, returnIntent)
                
                // 4. Close this Activity screen and return to the caller
                finish()
            }
        }
        
        // Register this callback with the Activity's onBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }

    companion object {
        // Shared constant key name for return data payload
        const val RECEIVED_MESSAGE = "Received message"
    }
}
