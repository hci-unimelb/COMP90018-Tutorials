package com.example.storage_internalstorage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.storage_internalstorage.databinding.ActivityMainBinding
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Name of the private image file we will create
    private val IMAGE_FILE_NAME = "myImage.png"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup button click events
        binding.saveButton.setOnClickListener { save(binding.editText.text.toString()) }
        binding.loadButton.setOnClickListener { binding.editText.setText(load()) }
        binding.clearButton.setOnClickListener { binding.editText.setText("") }

        binding.saveImageButton.setOnClickListener { saveImage(renderTimestampBitmap()) }
        binding.loadImageButton.setOnClickListener { binding.imagePreview.setImageBitmap(loadImage()) }
        binding.clearImageButton.setOnClickListener { binding.imagePreview.setImageBitmap(null) }
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

    /**
     * DEMO: builds a small bitmap in memory — a solid background plus the current time —
     * so every tap produces visibly different bytes. Stands in for "a photo the user took"
     * or "a thumbnail the app downloaded," without needing a camera or network for the demo.
     */
    private fun renderTimestampBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(14, 122, 108)) // same teal as the rest of this week's slides

        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 42f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Saved at", bitmap.width / 2f, 130f, paint)
        canvas.drawText(time, bitmap.width / 2f, 180f, paint)
        return bitmap
    }

    /**
     * Writes a bitmap's raw PNG bytes to our private internal file.
     *
     * Same openFileOutput() as the text demo above — Internal Storage doesn't care what the
     * bytes mean. bitmap.compress() is what turns pixels into a PNG byte stream to write.
     */
    private fun saveImage(bitmap: Bitmap) {
        try {
            openFileOutput(IMAGE_FILE_NAME, MODE_PRIVATE).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            binding.imagePreview.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Reads those PNG bytes back off disk and decodes them into a displayable Bitmap.
     * Returns null if nothing's been saved yet — same "expected first run" shape as load().
     */
    private fun loadImage(): Bitmap? {
        return try {
            openFileInput(IMAGE_FILE_NAME).use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "No saved image yet. Returning null.")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val TAG = "InternalStorage"
    }
}
