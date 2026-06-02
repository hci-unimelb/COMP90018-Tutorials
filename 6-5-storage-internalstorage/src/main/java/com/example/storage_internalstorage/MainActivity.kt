package com.example.storage_internalstorage

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.storage_internalstorage.databinding.ActivityMainBinding
import java.io.FileNotFoundException

/**
 * MAINACTIVITY: INTERNAL STORAGE FILES
 *
 * What is Internal Storage?
 * Every Android app has a private, sandboxed folder on the device's flash storage.
 * Files saved here are:
 *  1. Completely private to this application (other apps cannot read them).
 *  2. Automatically deleted if the user uninstalls the app.
 *
 * We read and write raw text files using standard streams:
 *  - `openFileOutput`: Opens an output stream to write bytes to a file.
 *  - `openFileInput`: Opens an input stream to read bytes from a file.
 *
 * MODERN KOTLIN I/O STREAMING:
 * In Kotlin, we write clean, concise, and safe input/output (I/O) code using standard
 * library extension functions like `.bufferedWriter()`, `.bufferedReader()`, and the `.use { ... }` block!
 *   1. The `.use` block automatically closes the reader/writer resource when the block finishes
 *      (preventing native stream leaks and memory issues).
 *   2. Code size is reduced drastically, making it highly readable and robust.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Name of the private text file we will create
    private val FILE_NAME = "myFile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup button click events
        binding.saveButton.setOnClickListener { save(binding.editText.text.toString()) }
        binding.loadButton.setOnClickListener { binding.editText.setText(load()) }
        binding.clearButton.setOnClickListener { binding.editText.setText("") }
    }

    /**
     * Writes text input to our private internal file.
     */
    private fun save(input: String) {
        try {
            // 1. openFileOutput creates or opens "myFile" in MODE_PRIVATE (overwrites file contents)
            // 2. '.bufferedWriter()' is a Kotlin extension that wraps it in an efficient buffer
            // 3. '.use { ... }' executes our write and AUTOMATICALLY closes the writer when done!
            openFileOutput(FILE_NAME, MODE_PRIVATE).bufferedWriter().use { writer ->
                writer.write(input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Reads text from our private internal file and returns it as a String.
     */
    private fun load(): String {
        try {
            // 1. openFileInput opens "myFile" for reading
            // 2. '.bufferedReader()' is a Kotlin extension wrapping it in a buffer
            // 3. '.use' automatically closes the reader, and '.readText()' reads the entire file as a string!
            return openFileInput(FILE_NAME).bufferedReader().use { reader ->
                reader.readText()
            }
        } catch (e: FileNotFoundException) {
            // Triggered if the user clicks "Load" before saving anything
            Log.d(TAG, "File not found. Returning empty string.")
            return ""
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    companion object {
        private const val TAG = "InternalStorage"
    }
}
