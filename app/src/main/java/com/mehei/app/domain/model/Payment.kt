package com.mehei.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a payment transaction for a MEHEI booking.
 *
 * Domain invariants:
 * - [amount] must be > 0
 * - [razorpayPaymentId] is populated after successful Razorpay transaction
 */
data class Payment(
    val id: String,
    val bookingId: String,
    val customerId: String,
    val amount: Int,                      // INR
    val type: PaymentType,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val razorpayPaymentId: String? = null,
    val razorpayOrderId: String? = null,
    val razorpaySignature: String? = null,
    val createdAt: String = "",           // ISO-8601
    val updatedAt: String = "",           // ISO-8601
    val refundId: String? = null,
    val refundAmount: Int = 0,            // INR — partial/full refund
    val refundReason: String? = null,
    val refundStatus: RefundStatus = RefundStatus.NONE,
)

@Serializable
enum class PaymentType {
    DEPOSIT,          // 30% upfront deposit
    REMAINING,        // 70% balance post-session
    TIP,              // Optional tip for the artist
    REFUND,           // Refund transaction (negative)
}

@Serializable
enum class PaymentMethod {
    UPI,
    CREDIT_CARD,
    DEBIT_CARD,
    NET_BANKING,
    WALLET,
    CASH,             // Cash on session (remaining balance)
}

@Serializable
enum class PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED,
}

@Serializable
enum class RefundStatus {
    NONE,             // No refund initiated
    REQUESTED,        // Customer requested refund
    UNDER_REVIEW,     // Admin reviewing
    APPROVED,         // Refund approved
    PROCESSED,        // Money returned
    REJECTED,         // Refund denied
}
