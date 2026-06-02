package com.example.connectivity_supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a row in the Supabase `messages` table.
 *
 * Must be annotated with @Serializable so the supabase-kt Postgrest client
 * can automatically encode/decode it to/from JSON.
 *
 * Column mapping:
 *   - id          -> bigint (auto-generated primary key, null on insert)
 *   - content     -> text (the message body)
 *   - created_at  -> timestamptz (set automatically by Supabase on insert)
 */
@Serializable
data class Message(
    val id: Long? = null,
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
