package com.example.connectivity_websocket

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Listens for INSERTs on Supabase's `messages` table over Realtime's WebSocket
 * connection (supabase-kt's Realtime plugin -- see the README's note on how this
 * relates to 7-3's Postgrest-only demo and to the raw OkHttp echo-server version
 * this class used to wrap).
 *
 * A row inserted from ANYWHERE -- this app's Send button, the Supabase Table
 * Editor, the SQL editor, another device -- arrives here the same way, because
 * they all go through the same Postgres table and the same Realtime channel.
 *
 * Thread safety: postgresChangeFlow delivers on a background dispatcher; the
 * onEvent callback is dispatched as-is -- MainActivity wraps it in runOnUiThread.
 */
class WebSocketManager(private val onEvent: (WsEvent) -> Unit) {

    sealed class WsEvent {
        object Opened : WsEvent()
        data class MessageReceived(val text: String) : WsEvent()
        data class Error(val message: String) : WsEvent()
        object Closed : WsEvent()
    }

    private val supabase = SupabaseClientProvider.client
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var channel: RealtimeChannel? = null
    val isConnected: Boolean get() = channel != null

    /** Subscribes to INSERTs on [table]. Does nothing if already connected. */
    fun connect(table: String = "messages") {
        if (channel != null) return
        scope.launch {
            try {
                val ch = supabase.channel("messages-changes")

                // Must register the flow BEFORE calling subscribe().
                ch.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    this.table = table
                }.onEach { insert ->
                    val row = insert.decodeRecord<Message>()
                    onEvent(WsEvent.MessageReceived(row.content))
                }.launchIn(scope)

                ch.subscribe(blockUntilSubscribed = true)
                channel = ch
                Log.d(TAG, "Subscribed to $table changes")
                onEvent(WsEvent.Opened)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe: ${e.message}")
                onEvent(WsEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }

    /** Inserts a row into `messages`; the insert then arrives back via the subscribed flow. */
    fun send(text: String): Boolean {
        if (channel == null) return false
        scope.launch {
            try {
                supabase.from("messages").insert(Message(content = text))
            } catch (e: Exception) {
                Log.e(TAG, "Insert failed: ${e.message}")
                onEvent(WsEvent.Error(e.message ?: "Insert failed"))
            }
        }
        return true
    }

    fun disconnect() {
        val ch = channel ?: return
        channel = null
        scope.launch {
            ch.unsubscribe()
            onEvent(WsEvent.Closed)
        }
    }

    /** Call only when this manager will never be used again (e.g. onDestroy). */
    fun shutdown() {
        channel?.let { ch -> scope.launch { ch.unsubscribe() } }
        channel = null
    }

    companion object {
        private const val TAG = "WebSocketManager"
    }
}
