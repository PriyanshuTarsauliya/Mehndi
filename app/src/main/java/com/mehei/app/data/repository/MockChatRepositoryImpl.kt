package com.mehei.app.data.repository

import com.mehei.app.domain.model.ChatMessage
import com.mehei.app.domain.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of ChatRepository.
 * Stores messages in-memory. Replace with FirestoreChatRepositoryImpl
 * when Firebase is connected.
 */
@Singleton
class MockChatRepositoryImpl @Inject constructor() : ChatRepository {

    // bookingId -> list of messages
    private val messageStore = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "book-004" to listOf(
                ChatMessage(
                    id = "msg-001",
                    bookingId = "book-004",
                    senderId = "cust-456",
                    senderName = "Customer",
                    text = "Hi! I need bridal mehendi for my engagement ceremony. Can you do a full-arm design?",
                    timestamp = System.currentTimeMillis() - 3600_000,
                    isFromCurrentUser = false,
                ),
                ChatMessage(
                    id = "msg-002",
                    bookingId = "book-004",
                    senderId = "artist-current",
                    senderName = "You",
                    text = "Yes, absolutely! I specialize in portrait-style bridal designs. I'll bring 3 premium organic cones.",
                    timestamp = System.currentTimeMillis() - 3000_000,
                    isFromCurrentUser = true,
                ),
                ChatMessage(
                    id = "msg-003",
                    bookingId = "book-004",
                    senderId = "cust-456",
                    senderName = "Customer",
                    text = "That sounds perfect! Can you share some reference photos?",
                    timestamp = System.currentTimeMillis() - 1800_000,
                    isFromCurrentUser = false,
                ),
            )
        )
    )

    private var nextMsgId = 4

    override fun getMessages(bookingId: String): Flow<List<ChatMessage>> {
        return messageStore.map { store ->
            store[bookingId] ?: emptyList()
        }
    }

    override suspend fun sendMessage(bookingId: String, text: String): Boolean {
        delay(200) // Simulate network
        val newMessage = ChatMessage(
            id = "msg-${String.format("%03d", nextMsgId++)}",
            bookingId = bookingId,
            senderId = "artist-current",
            senderName = "You",
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
        )

        val currentStore = messageStore.value.toMutableMap()
        val existing = currentStore[bookingId]?.toMutableList() ?: mutableListOf()
        existing.add(newMessage)
        currentStore[bookingId] = existing
        messageStore.value = currentStore
        return true
    }
}
