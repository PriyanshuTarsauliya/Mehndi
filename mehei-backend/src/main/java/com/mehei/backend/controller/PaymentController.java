package com.mehei.backend.controller;

import com.mehei.backend.entity.PaymentTransaction;
import com.mehei.backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentTransaction>> getUserPayments(@PathVariable String userId) {
        return ResponseEntity.ok(paymentRepository.findByUserId(userId));
    }
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentTransaction>> getBookingPayments(@PathVariable String bookingId) {
        return ResponseEntity.ok(paymentRepository.findByBookingId(bookingId));
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentTransaction> processPayment(@RequestBody PaymentTransaction payment) {
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            payment.setTransactionId(UUID.randomUUID().toString());
        }
        payment.setType("PAYMENT");
        payment.setStatus("SUCCESS"); // Mocking successful payment
        payment.setTimestamp(LocalDateTime.now());
        
        PaymentTransaction saved = paymentRepository.save(payment);
        return ResponseEntity.ok(saved);
    }
    
    @PostMapping("/refund")
    public ResponseEntity<PaymentTransaction> processRefund(@RequestBody PaymentTransaction refund) {
        if (refund.getTransactionId() == null || refund.getTransactionId().isEmpty()) {
            refund.setTransactionId(UUID.randomUUID().toString());
        }
        refund.setType("REFUND");
        refund.setStatus("SUCCESS");
        refund.setTimestamp(LocalDateTime.now());
        
        PaymentTransaction saved = paymentRepository.save(refund);
        return ResponseEntity.ok(saved);
    }
}
