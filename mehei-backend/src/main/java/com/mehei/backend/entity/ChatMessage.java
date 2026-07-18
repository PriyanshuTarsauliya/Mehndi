package com.mehei.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    private String messageId;
    
    @Column(name = "booking_id", nullable = false)
    private String bookingId;
    
    @Column(name = "sender_id", nullable = false)
    private String senderId;
    
    @Column(name = "receiver_id", nullable = false)
    private String receiverId;
    
    @Column(name = "content", nullable = false, length = 1000)
    private String content;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Column(name = "is_read")
    private Boolean isRead = false;
}
