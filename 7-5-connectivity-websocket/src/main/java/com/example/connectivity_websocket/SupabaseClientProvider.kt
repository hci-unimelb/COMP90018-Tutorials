package com.example.connectivity_websocket

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Singleton Supabase client, shared with the 7-3-connectivity-supabase module's
 * project (same SUPABASE_URL / SUPABASE_KEY in local.properties, same `messages` table).
 *
 * Postgrest -> used by Send to INSERT a row.
 * Realtime  -> the actual WebSocket connection this demo listens on for row changes.
 */
object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Realtime)
    }
}
