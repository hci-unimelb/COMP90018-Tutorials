package com.example.connectivity_websocket

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.connectivity_websocket.databinding.ActivityMainBinding

/**
 * Tutorial 7-5: WebSocket Demo
 *
 * Demonstrates a full-duplex, real-time connection -- backed by Supabase
 * Realtime, which is itself a WebSocket under the hood (supabase-kt's
 * Realtime plugin over the same `messages` table used in 7-3).
 *
 * Key concepts covered:
 *   - WebSocket vs HTTP: persistent, full-duplex, low overhead
 *   - A higher-level SDK (supabase-kt) wrapping raw WebSocket frames as
 *     typed Postgres change events
 *   - Proper lifecycle management: connect in UI, disconnect in onDestroy
 *   - Thread safety: Realtime callbacks run off Main Thread; use runOnUiThread
 *
 * UI flow:
 *   Connect -> status turns green -> type message -> Send -> inserts a row,
 *   which comes straight back over the same channel as RECEIVED. Rows
 *   inserted from the Supabase dashboard/SQL editor while connected arrive
 *   the same way, with no matching SENT line.
 *   Disconnect -> status turns red
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val TABLE_NAME = "messages"

    private val messageLog = StringBuilder()

    /**
     * WebSocketManager handles all OkHttp details.
     * We pass a lambda that will be called on every WebSocket event.
     *
     * NOTE: OkHttp calls this lambda on a background thread.
     * We use runOnUiThread{} inside to update Views safely.
     */
    private val wsManager = WebSocketManager { event ->
        runOnUiThread {
            when (event) {
                is WebSocketManager.WsEvent.Opened -> {
                    appendLog("✓ Subscribed to \"$TABLE_NAME\" table changes")
                    binding.statusText.text = "Connected"
                    binding.statusText.setTextColor(getColor(android.R.color.holo_green_dark))
                    binding.connectButton.text = "Disconnect"
                    binding.sendButton.isEnabled = true
                }
                is WebSocketManager.WsEvent.MessageReceived -> {
                    appendLog("← RECEIVED: ${event.text}")
                }
                is WebSocketManager.WsEvent.Error -> {
                    appendLog("✗ Error: ${event.message}")
                    binding.statusText.text = "Error"
                    binding.statusText.setTextColor(getColor(android.R.color.holo_red_dark))
                    binding.connectButton.text = "Connect"
                    binding.sendButton.isEnabled = false
                }
                is WebSocketManager.WsEvent.Closed -> {
                    appendLog("✗ Disconnected")
                    binding.statusText.text = "Disconnected"
                    binding.statusText.setTextColor(getColor(android.R.color.holo_red_dark))
                    binding.connectButton.text = "Connect"
                    binding.sendButton.isEnabled = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendButton.isEnabled = false
        binding.statusText.text = "Disconnected"
        binding.statusText.setTextColor(getColor(android.R.color.holo_red_dark))

        binding.connectButton.setOnClickListener {
            if (wsManager.isConnected) {
                wsManager.disconnect()
            } else {
                appendLog("Connecting to Supabase Realtime...")
                wsManager.connect(TABLE_NAME)
            }
        }

        binding.sendButton.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()
            if (text.isNotBlank()) {
                val sent = wsManager.send(text)
                if (sent) {
                    appendLog("→ SENT: $text")
                    binding.messageInput.setText("")
                } else {
                    appendLog("✗ Send failed -- not connected")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // IMPORTANT: shutdown() releases OkHttp's thread pool.
        // Forgetting this leaks threads that outlive the Activity.
        wsManager.shutdown()
    }

    private fun appendLog(line: String) {
        messageLog.appendLine(line)
        binding.messageLog.text = messageLog.toString()
        // Auto-scroll to bottom
        binding.scrollView.post {
            binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }
}
