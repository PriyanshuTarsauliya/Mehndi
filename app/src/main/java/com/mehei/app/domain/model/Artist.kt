package com.mehei.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Core domain model for a mehendi/henna artist.
 *
 * Domain rules:
 * - An APPRENTICE must have a non-null [parentArtistId]
 * - [rating] is in range 0.0..5.0
 * - [rateCards] must have at least one entry
 */
data class Artist(
    val id: String,
    val name: String,
    val rating: Float,
    val experienceYears: Int,
    val bio: String,
    val city: String,
    val profileImageUrl: String,
    val tier: ArtistTier,
    val parentArtistId: String? = null,
    val specialties: List<EventType>,
    val rateCards: List<RateCard>,
    val totalReviews: Int = 0,
    val hasFlashSlots: Boolean = false,
    val isFavorite: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distanceKm: Float? = null,   // Calculated from user's live location
)

@Serializable
enum class ArtistTier {
    MASTER,
    APPRENTICE,
    ASSOCIATE,
}

@Serializable
enum class EventType {
    KARVA_CHAUTH,
    BABY_SHOWER,
    TEEJ,
    ENGAGEMENT,
    FESTIVAL,
    CORPORATE,
    PARTY,
    HALDI,
    MEHNDI_NIGHT,
}

@Serializable
enum class Complexity {
    SIMPLE,        // Stripes, dots, simple mandalas — 15-20 min/hand
    TRADITIONAL,   // Classic dense patterns, Arabic — 30-45 min/hand
    PORTRAIT,      // Intricate figurines, bridal-level — 45-90 min/hand
}

data class RateCard(
    val complexity: Complexity,
    val pricePerHand: Int,   // In INR (paise for precision: ₹500 = 50000)
    val hourlyRate: Int,     // In INR
)
