package com.mehei.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A booking order for a mehendi session.
 *
 * Domain invariants:
 * - [numHands] must be > 0
 * - [totalPrice] must be > 0
 * - [deposit] is always 30% of [totalPrice], non-refundable
 */
data class Booking(
    val id: String,
    val customerId: String,
    val artistId: String,
    val artistName: String,
    val bookingDate: String,       // ISO-8601 date string
    val startTime: String,         // HH:mm
    val endTime: String,           // HH:mm
    val numHands: Int,
    val complexity: Complexity,
    val eventType: EventType,
    val totalPrice: Int,           // INR
    val deposit: Int,              // INR (30% non-refundable)
    val materialFee: Int = 0,      // Artist-supplied organic henna
    val status: BookingStatus,
    val customerNote: String = "",
    // Uber-like Tracking Fields
    val clientLatitude: Double = 0.0,
    val clientLongitude: Double = 0.0,
    val artistLatitude: Double? = null,
    val artistLongitude: Double? = null,
    // Payment Tracking
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val depositPaymentId: String? = null,   // Razorpay payment ID for deposit
    val remainingPaymentId: String? = null,  // Razorpay payment ID for balance
    val tipAmount: Int = 0,                  // Optional tip in INR
    val couponCode: String? = null,
    val couponDiscount: Int = 0,             // INR discount from coupon
    val refundAmount: Int = 0,               // INR refunded
    val refundStatus: RefundStatus = RefundStatus.NONE,
)

@Serializable
enum class BookingStatus {
    REQUESTED,     // Broadcasted to nearby artists
    MATCHING,      // System finding an artist
    ACCEPTED,      // Artist accepted
    ON_THE_WAY,    // Artist is traveling to client
    ARRIVED,       // Artist has arrived at client location
    IN_PROGRESS,   // Session underway
    COMPLETED,     // Session done, pending review
    CANCELLED,     // Cancelled by customer or artist
    NO_SHOW,       // Customer did not appear
}

/**
 * Price breakdown from the scope-based calculator.
 * All values in INR.
 */
data class PriceEstimate(
    val handsCost: Int,
    val hoursCost: Int,
    val materialFee: Int,
    val discount: Int,
    val total: Int,
    val deposit: Int,              // 30% of total
)
