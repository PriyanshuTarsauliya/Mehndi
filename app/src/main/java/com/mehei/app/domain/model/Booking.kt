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
)

@Serializable
enum class BookingStatus {
    PENDING,       // Awaiting artist confirmation
    CONFIRMED,     // Artist accepted, deposit paid
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
