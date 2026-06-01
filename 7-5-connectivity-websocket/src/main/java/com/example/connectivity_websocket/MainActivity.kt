package com.example.connectivity_websocket

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.connectivity_websocket.databinding.ActivityMainBinding

/**
 * Tutorial 7-5: WebSocket Demo
 *
 * Demonstrates a full-duplex WebSocket connection using OkHttp.
 * Connects to wss://echo.websocket.org -- a public echo server that
 * reflects every message back to the sender.
 *
 * Key concepts covered:
 *   - WebSocket vs HTTP: persistent, full-duplex, low overhead
 *   - WebSocketListener callbacks: onOpen, onMessage, onClosing, onFailure
 *   - Proper lifecycle management: connect in UI, disconnect in onDestroy
 *   - Thread safety: OkHttp callbacks run off Main Thread; use runOnUiThread
 *   - Clean close: close code 1000 = Normal Closure per RFC 6455
 *
 * UI flow:
 *   Connect -> status turns green -> type message -> Send -> see echo reply
 *   Disconnect -> status turns red
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // The echo server URL -- wss:// = WebSocket over TLS (like https for WS)
    private val SERVER_URL = "wss://echo.websocket.org"

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
                    appendLog("✓ Connected to $SERVER_URL")
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
                appendLog("Connecting to $SERVER_URL...")
                wsManager.connect(SERVER_URL)
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
