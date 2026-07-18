package com.mehei.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BookingRequest(
    val id: String,
    val customerId: String,
    val customerName: String,
    val numHands: Int,
    val complexity: Complexity,
    val eventType: EventType,
    val estimatedPrice: Int,
    val clientLatitude: Double,
    val clientLongitude: Double,
    val requestedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = requestedAt + 15000, // 15 seconds to accept
    val status: RequestStatus = RequestStatus.BROADCASTING
)

@Serializable
enum class RequestStatus {
    BROADCASTING,
    ACCEPTED,
    EXPIRED,
    DECLINED
}
