package com.mehei.app.domain.model

/**
 * An off-peak time slot published by an artist for discounted bookings.
 * Modeled after hotel/airline yield management — monetize idle time
 * without diluting the artist's premium wedding-season brand.
 */
data class FlashSlot(
    val id: String,
    val artistId: String,
    val artistName: String,
    val date: String,             // ISO-8601 date
    val startTime: String,        // HH:mm
    val endTime: String,          // HH:mm
    val discountPercent: Int,     // 10-50% off standard rate
    val isBooked: Boolean,
)

/**
 * A review left by a customer after a completed booking.
 * Trust signal specific to small events — not bridal portfolio.
 */
data class Review(
    val id: String,
    val bookingId: String,
    val artistId: String,
    val customerName: String,
    val rating: Float,            // 1.0 - 5.0
    val comment: String,
    val eventType: EventType,
    val date: String,             // ISO-8601 date
    val photoUrls: List<String> = emptyList(),
)
