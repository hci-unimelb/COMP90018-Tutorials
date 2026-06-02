package com.example.storage.sharedpreferences

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.storage.sharedpreferences.databinding.ActivityMainBinding

/**
 * MAINACTIVITY: SHAREDPREFERENCES (Simple Storage)
 *
 * What is SharedPreferences?
 * SharedPreferences is a lightweight, built-in storage system on Android.
 * It is designed to save small amounts of simple data (like settings, user configurations, high scores)
 * as **Key-Value Pairs** (similar to a dictionary in Python or Map in Java).
 *
 * Example: Key "username" -> Value "JohnDoe"
 *
 * SharedPreferences values are written to an XML file in your app's private directory and persist
 * even if the user completely closes or reboots their phone.
 *
 * Crucial: Do NOT use SharedPreferences to store large datasets (like a list of items or images).
 * For large datasets, use a database (Room or SQLite) or Internal Files.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Keys used to save and retrieve values from SharedPreferences.
    // Keeping these as constants prevents typos and bugs!
    private val PREFERENCE_NAME = "data"
    private val OBJECT_NAME = "name"
    private val OBJECT_AGE = "age"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. SAVE DATA BUTTON Click Listener
        binding.saveData.setOnClickListener {
            // Open SharedPreferences in private mode (only this app can read these values)
            // Calling '.edit()' enters editing mode to allow writing values
            val editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit()

            // Save a string value from our "Name" input field
            editor.putString(OBJECT_NAME, binding.name.text.toString())

            // SAFE INTEGER CONVERSION:
            // We use Kotlin's safe `.toIntOrNull()` extension function instead of `.toInt()`.
            // If the user enters invalid characters or leaves the age field empty, `.toIntOrNull()`
            // safely returns null. We use the Elvis Operator (`?:`) to provide a default fallback value of `0`,
            // preventing the app from throwing parsing exceptions on empty inputs!
            val ageInput = binding.age.text.toString()
            val ageValue = ageInput.toIntOrNull() ?: 0
            editor.putInt(OBJECT_AGE, ageValue)

            // 'apply()' saves the data asynchronously in the background.
            // (Note: Historically, 'commit()' was used, but 'apply()' is preferred because it runs
            // in the background without blocking the UI thread!)
            editor.apply()
        }

        // 2. RESTORE DATA BUTTON Click Listener
        binding.restoreData.setOnClickListener {
            // Open SharedPreferences in read-only mode
            val preferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE)

            // Read the saved name. We provide a default fallback string ("") if no value was ever saved.
            val savedName = preferences.getString(OBJECT_NAME, "")
            binding.name.setText(savedName)

            // Read the saved age. We provide a default fallback integer (0) if no value was ever saved.
            val savedAge = preferences.getInt(OBJECT_AGE, 0)
            binding.age.setText(savedAge.toString())
        }
    }
}
