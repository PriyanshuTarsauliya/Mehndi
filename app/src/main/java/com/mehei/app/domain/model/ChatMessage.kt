package com.mehei.app.domain.model

/**
 * A single chat message between a customer and artist
 * linked to a specific booking.
 */
data class ChatMessage(
    val id: String,
    val bookingId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long, // epoch millis
    val isFromCurrentUser: Boolean,
)
