package com.example.connectivity_websocket

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * Manages a single WebSocket connection to the echo server.
 *
 * Responsibilities:
 *   - Hold the OkHttpClient and WebSocket instance
 *   - Provide connect() / disconnect() / send() methods
 *   - Deliver events (open, message, failure, closed) via a callback lambda
 *
 * Why a separate class?
 *   Keeps Activity clean -- it only interacts with WebSocketManager, not raw OkHttp.
 *   Also makes it easier to swap out the server URL or library later.
 *
 * Thread safety:
 *   OkHttp's WebSocketListener callbacks run on its internal thread pool.
 *   The onEvent callback is always dispatched to the CALLER'S thread via the provided
 *   handler -- or MainActivity calls runOnUiThread() before updating views.
 */
class WebSocketManager(private val onEvent: (WsEvent) -> Unit) {

    /**
     * Sealed class representing all possible WebSocket events.
     * Using a sealed class means MainActivity can handle every case exhaustively with `when`.
     */
    sealed class WsEvent {
        object Opened                         : WsEvent()
        data class MessageReceived(val text: String) : WsEvent()
        data class Error(val message: String) : WsEvent()
        object Closed                         : WsEvent()
    }

    private val client = OkHttpClient.Builder()
        // readTimeout 0 = no timeout for the persistent WebSocket connection
        // (a non-zero timeout would close the socket after idle time)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    val isConnected: Boolean get() = webSocket != null

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket opened")
            onEvent(WsEvent.Opened)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Message received: $text")
            onEvent(WsEvent.MessageReceived(text))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            // Server is initiating close -- acknowledge it
            webSocket.close(1000, null)
            Log.d(TAG, "Closing: code=$code reason=$reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "Closed")
            this@WebSocketManager.webSocket = null
            onEvent(WsEvent.Closed)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "Failure: ${t.message}")
            this@WebSocketManager.webSocket = null
            onEvent(WsEvent.Error(t.message ?: "Unknown error"))
        }
    }

    /**
     * Opens a WebSocket connection to [url].
     * Does nothing if already connected.
     */
    fun connect(url: String) {
        if (webSocket != null) return
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, listener)
    }

    /**
     * Sends a text message over the open WebSocket.
     * Returns false if the connection is not open or the message cannot be enqueued.
     */
    fun send(text: String): Boolean {
        return webSocket?.send(text) ?: false
    }

    /**
     * Closes the connection with a Normal Closure code (1000).
     * Per the WebSocket spec, code 1000 = intentional, clean close.
     */
    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }

    /**
     * Shuts down OkHttp's internal thread pool.
     * Call this only when the client will never be used again (e.g. in onDestroy).
     * Forgetting this leaks threads.
     */
    fun shutdown() {
        disconnect()
        client.dispatcher.executorService.shutdown()
    }

    companion object {
        private const val TAG = "WebSocketManager"
    }
}
