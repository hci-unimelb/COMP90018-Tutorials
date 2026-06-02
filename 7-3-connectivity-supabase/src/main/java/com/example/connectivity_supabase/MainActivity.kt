package com.example.connectivity_supabase

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.connectivity_supabase.databinding.ActivityMainBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tutorial 7-3: Supabase Connectivity Demo
 *
 * Demonstrates the Supabase Kotlin SDK for Android:
 *   - SELECT  : load all messages from the database on startup
 *   - INSERT  : send a new message when the user taps "Send"
 *   - Refresh : reload the list on demand
 *
 * All Supabase calls are suspend functions and MUST run on a background
 * dispatcher (Dispatchers.IO). We use lifecycleScope.launch + withContext
 * to keep the Main Thread free (prevents NetworkOnMainThreadException).
 *
 * Prerequisites — see README.md:
 *   1. Create a Supabase project at supabase.com
 *   2. Create the `messages` table (SQL in README)
 *   3. Add SUPABASE_URL and SUPABASE_KEY to local.properties
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Access the shared Supabase client singleton
    private val supabase = SupabaseClientProvider.client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load existing messages when the Activity starts
        loadMessages()

        // INSERT: send a new message on button click
        binding.messageButton.setOnClickListener {
            val text = binding.messageContent.text.toString().trim()
            if (text.isNotBlank()) {
                sendMessage(text)
            }
        }

        // SELECT: manually refresh the list
        binding.refreshButton.setOnClickListener {
            loadMessages()
        }
    }

    /**
     * SELECT all rows from the `messages` table, ordered by creation time.
     * Decodes each row into a [Message] data class via kotlinx.serialization.
     */
    private fun loadMessages() {
        binding.statusText.text = "Loading..."
        binding.statusText.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // withContext moves the network call off the Main Thread
                val messages = withContext(Dispatchers.IO) {
                    supabase.from("messages")
                        .select()
                        .decodeList<Message>()
                }

                // Back on Main Thread — safe to update UI
                if (messages.isEmpty()) {
                    binding.messageDisplay.text = "(no messages yet)"
                } else {
                    binding.messageDisplay.text = messages
                        .reversed()   // newest first
                        .joinToString("\n\n") { "• ${it.content}" }
                }
                binding.statusText.visibility = View.GONE

            } catch (e: Exception) {
                binding.statusText.text = "Error loading: ${e.message}"
            }
        }
    }

    /**
     * INSERT a new row into the `messages` table.
     * After a successful insert, clears the input field and reloads the list.
     */
    private fun sendMessage(text: String) {
        binding.messageButton.isEnabled = false
        binding.statusText.text = "Sending..."
        binding.statusText.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    supabase.from("messages")
                        .insert(Message(content = text))
                }

                // Clear input and refresh list on success
                binding.messageContent.setText("")
                loadMessages()

            } catch (e: Exception) {
                binding.statusText.text = "Error sending: ${e.message}"
            } finally {
                binding.messageButton.isEnabled = true
            }
        }
    }
}
