package com.example.connectivity_websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A row in the Supabase `messages` table -- same shape as 7-3's Message.kt. */
@Serializable
data class Message(
    val id: Long? = null,
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
