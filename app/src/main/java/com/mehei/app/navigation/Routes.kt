package com.mehei.app.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for MEHEI.
 * Using Compose Navigation 2.8+ @Serializable pattern.
 */



@Serializable
data object FlashSlotsRoute

// --- Request Flow ---
@Serializable
data object RequestRoute

@Serializable
data class MatchingRoute(val requestId: String)

@Serializable
data class LiveTrackingRoute(val bookingId: String)

@Serializable
data class ArtistDetailRoute(val artistId: String)

@Serializable
data class BookingCalculatorRoute(val artistId: String)

@Serializable
data class BookingConfirmRoute(val bookingId: String)

@Serializable
data object LoginRoute

@Serializable
data object ForgotPasswordRoute

@Serializable
data object ProfileSetupRoute

@Serializable
data class CheckoutRoute(val artistId: String, val depositAmount: Int)

@Serializable
data object BookingsHistoryRoute

@Serializable
data object ProfileRoute

@Serializable
data object PersonalDetailsRoute

@Serializable
data object PaymentMethodsRoute

@Serializable
data object HelpSupportRoute

@Serializable
data object SettingsRoute

@Serializable
data object PrivacyPolicyRoute

@Serializable
data object TermsOfServiceRoute

@Serializable
data class ArtistNavigationRoute(val bookingId: String)
@Serializable
data object FavoritesRoute

// --- Artist Routes ---

@Serializable
data object ArtistDashboardRoute

@Serializable
data class ArtistBookingDetailRoute(val bookingId: String)

@Serializable
data object ArtistPortfolioRoute

// --- Chat Routes ---

@Serializable
data class ChatRoute(val bookingId: String, val otherUserName: String)

// --- Payment Routes ---

@Serializable
data object PaymentHistoryRoute

@Serializable
data object RefundPolicyRoute
