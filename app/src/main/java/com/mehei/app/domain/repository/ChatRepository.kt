package com.mehei.app.domain.repository

import com.mehei.app.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    /**
     * Observe messages for a given booking in real-time.
     * Returns a Flow that emits the full message list on every change.
     */
    fun getMessages(bookingId: String): Flow<List<ChatMessage>>

    /**
     * Send a new message.
     */
    suspend fun sendMessage(bookingId: String, text: String): Boolean
}
