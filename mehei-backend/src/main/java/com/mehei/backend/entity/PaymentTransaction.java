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
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    @Id
    private String transactionId;
    
    @Column(name = "booking_id", nullable = false)
    private String bookingId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    @Column(name = "type", nullable = false) // e.g. "PAYMENT", "REFUND", "TIP"
    private String type;
    
    @Column(name = "status", nullable = false) // e.g. "SUCCESS", "PENDING", "FAILED"
    private String status;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Column(name = "payment_method")
    private String paymentMethod;
}
