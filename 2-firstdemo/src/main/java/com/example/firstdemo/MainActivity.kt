package com.example.firstdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firstdemo.databinding.ActivityMainBinding

/**
 * [MainActivity] - The Main Screen of the App
 *
 * In Android, an "Activity" is a single screen with a user interface (UI) that the user interacts with.
 * AppCompatActivity is a base class that ensures older Android devices support modern Android features.
 */
class MainActivity : AppCompatActivity() {

    // An Enum (Enumeration) lists a set of constant choices. We use this to select which step to demonstrate.
    private enum class Step {
        One,   // Step One: Standard console logging
        Two,   // Step Two: Traditional button click handling
        Three  // Step Three: Modern ViewBinding click handling
    }

    // Selector for different styles of Intents (how we request other screens to open)
    private enum class IntentStyle {
        Implicit, // Implicit: Asking Android to find any screen that matches an action
        Explicit  // Explicit: Directly launching a specific screen by class name
    }

    // Toggle these properties to change the behavior of the tutorial demonstration!
    private val step = Step.Three
    private val intent = IntentStyle.Implicit

    // A Tag label used to easily locate and filter our log prints in Android Studio's Logcat console.
    private val TAG = "First Demo"

    /**
     * ViewBinding is a tool that automatically generates a class (ActivityMainBinding) which has direct
     * references to every UI view in our XML layout file (activity_main.xml) that has an android:id.
     * 'lateinit var' tells Kotlin: "We will initialize this variable soon (in onCreate), don't complain that it's empty now."
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * [onCreate] is the entry point of our Activity screen (similar to the 'main' function in other languages).
     * The Android OS runs this method when our screen is first loaded in memory.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Tells Android to draw the app content edge-to-edge (behind system status/nav bars)
        enableEdgeToEdge()
        
        // Inflates the XML layout file (loads its components into memory as Kotlin objects)
        binding = ActivityMainBinding.inflate(layoutInflater)
        
        // Sets the visible content of this screen to the root view of our inflated XML layout
        setContentView(binding.root)

        // Set the text of the TextView component using our modern ViewBinding object
        binding.receivedMessage.text = "ButterKnife has been replaced!"

        // STEP 1: Logcat Output Demo
        if (step == Step.One) {
            // Log.d writes a debug statement to the console. Helpful for developers to verify code runs!
            Log.d(TAG, "onCreate: Step One: This is the first LOG")
        }

        // STEP 2: Traditional Button Click Listener
        if (step == Step.Two) {
            // Traditional way: Find the button view inside the layout using its XML ID
            val button = findViewById<android.widget.Button>(R.id.button)
            
            // Set a click listener containing code that runs whenever the user taps this button
            button.setOnClickListener {
                binding.receivedMessage.text = "Button has been clicked!"
                Log.d(TAG, "onCreate: Step Two: Click Button!")
            }
        }

        // STEP 3: Modern ViewBinding Click Listener
        if (step == Step.Three) {
            // ViewBinding way: Directly access the button widget (binding.button) without searching for it
            binding.button.setOnClickListener {
                binding.receivedMessage.text = "Button has been clicked from ViewBinding!"
                Log.d(TAG, "onCreate: Step Three: Click View Binding Button!")
                
                // Call our helper function to trigger transition to the second screen
                triggerButtonPressWithIntent()
            }
        }

        // Adjusts the screen margins so system bars (like status and navigation bars) do not overlap our UI elements
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * An "Intent" is a messaging request object used to ask Android to do something
     * (e.g., launch a new screen, open the camera, browse a web URL).
     */
    private fun triggerButtonPressWithIntent() {
        Log.d(TAG, "outputLog: Click Button Successful!")

        // Style A: EXPLICIT INTENTS
        // We explicitly specify the exact destination screen class (Main2Activity) we want to load.
        if (intent == IntentStyle.Explicit) {
            Log.d(TAG, "outputLog: Step one: Explicit Intent")
            
            // Define an intent to transition from 'this' screen to 'Main2Activity'
            val explicit = Intent(this, Main2Activity::class.java)
            
            // putExtra allows us to attach extra parameters (like a payload package) to pass to the next activity
            explicit.putExtra(MESSAGE, "Hello from the first activity")
            
            // Launch the intent using our registered launcher
            startActivityIntent.launch(explicit)
        }

        // Style B: IMPLICIT INTENTS
        // We declare an "Action" string rather than naming a specific destination class.
        // Android searches for any activity that registered this Action filter in its Manifest file.
        if (intent == IntentStyle.Implicit) {
            val implicit = Intent()
            
            // Set the custom action name. Main2Activity is registered to respond to this action!
            implicit.action = "SecondActivity"
            
            // Attach the data payload string
            implicit.putExtra(MESSAGE, "Hello from the first activity")
            
            // Launch the intent
            startActivityIntent.launch(implicit)
        }
    }

    /**
     * Activity Result Launcher:
     * Registers a callback that launches a screen and waits for a result to return back when it closes.
     * It runs automatically when the second screen finishes and returns control to this screen.
     */
    private val startActivityIntent: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // If the second screen closed successfully (RESULT_OK)
        if (result.resultCode == RESULT_OK) {
            // Retrieve the data package sent back by the closing activity
            val returnedText = result.data?.getStringExtra(Main2Activity.RECEIVED_MESSAGE)
            
            // Update our TextView to show the text we received back
            binding.receivedMessage.text = returnedText
        }
    }

    // Companion object houses constant values shared across all instances of this class (similar to static in Java)
    companion object {
        const val MESSAGE = "Message" // The key label we use when packing intent payload extras
        const val MESSAGE_RECEIVED = 1
    }
}
