package com.mehei.backend.controller;

import com.mehei.backend.entity.ChatMessage;
import com.mehei.backend.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<ChatMessage>> getMessagesForBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(chatMessageRepository.findByBookingIdOrderByTimestampAsc(bookingId));
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        message.setIsRead(false);
        
        ChatMessage saved = chatMessageRepository.save(message);
        return ResponseEntity.ok(saved);
    }
}
