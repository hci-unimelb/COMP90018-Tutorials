package com.example.connectivity_supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Singleton that holds the Supabase client instance.
 *
 * Supabase URL and Publishable key are injected via BuildConfig at compile time.
 * They are read from local.properties (never committed to Git).
 *
 * The Publishable key (sb_publishable_xxx) is the new key format introduced in 2025.
 * It is safe to embed in client-side code — it identifies your project but carries
 * no elevated server-side permissions on its own (unlike the secret Service Role key).
 *
 * Only install the modules you need:
 *   - Postgrest  -> database CRUD operations
 *   - Auth       -> user authentication (not used in this demo)
 *   - Storage    -> file uploads (not used in this demo)
 */
object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY   // sb_publishable_xxx
    ) {
        install(Postgrest)
    }
}
