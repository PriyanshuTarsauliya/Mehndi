package com.mehei.app.domain.model

/**
 * MEHEI Refund & Cancellation Policy.
 *
 * Rules:
 * ┌──────────────────────────────────┬──────────────────────┐
 * │ Cancellation Window              │ Refund               │
 * ├──────────────────────────────────┼──────────────────────┤
 * │ > 48 hours before session       │ Full deposit refund  │
 * │ 24–48 hours before session      │ 50% of deposit       │
 * │ < 24 hours before session       │ Non-refundable       │
 * │ Artist cancels                  │ Full refund           │
 * │ No-show by artist               │ Full refund + ₹200   │
 * │ Session incomplete (artist)     │ Pro-rated refund      │
 * └──────────────────────────────────┴──────────────────────┘
 */
object RefundPolicy {

    data class RefundResult(
        val isEligible: Boolean,
        val refundPercent: Int,       // 0–100
        val refundAmount: Int,        // INR
        val reason: String,
        val compensationBonus: Int = 0 // Extra compensation (e.g., artist no-show)
    )

    /**
     * Calculate refund eligibility based on cancellation timing.
     *
     * @param depositAmount the deposit paid in INR
     * @param hoursUntilSession hours remaining until the booked session
     * @param cancelledByArtist whether the artist initiated the cancellation
     * @param artistNoShow whether the artist failed to show up
     */
    fun calculateRefund(
        depositAmount: Int,
        hoursUntilSession: Long,
        cancelledByArtist: Boolean = false,
        artistNoShow: Boolean = false,
    ): RefundResult {
        // Artist-initiated cancellations always get full refund
        if (cancelledByArtist) {
            return RefundResult(
                isEligible = true,
                refundPercent = 100,
                refundAmount = depositAmount,
                reason = "Artist cancelled the booking. Full refund issued.",
            )
        }

        // Artist no-show: full refund + ₹200 compensation
        if (artistNoShow) {
            return RefundResult(
                isEligible = true,
                refundPercent = 100,
                refundAmount = depositAmount,
                reason = "Artist did not show up. Full refund + ₹200 compensation.",
                compensationBonus = 200,
            )
        }

        // Customer-initiated cancellations are time-tiered
        return when {
            hoursUntilSession > 48 -> RefundResult(
                isEligible = true,
                refundPercent = 100,
                refundAmount = depositAmount,
                reason = "Cancelled more than 48 hours before session. Full deposit refund.",
            )
            hoursUntilSession in 24..48 -> RefundResult(
                isEligible = true,
                refundPercent = 50,
                refundAmount = depositAmount / 2,
                reason = "Cancelled 24–48 hours before session. 50% deposit refund.",
            )
            else -> RefundResult(
                isEligible = false,
                refundPercent = 0,
                refundAmount = 0,
                reason = "Cancelled less than 24 hours before session. Non-refundable.",
            )
        }
    }

    /**
     * Policy text for display in the UI.
     */
    val policyItems: List<PolicyItem> = listOf(
        PolicyItem(
            title = "Free Cancellation",
            description = "Cancel more than 48 hours before your session for a full deposit refund.",
            icon = "✅"
        ),
        PolicyItem(
            title = "Late Cancellation",
            description = "Cancel 24–48 hours before your session and receive 50% of your deposit back.",
            icon = "⚠️"
        ),
        PolicyItem(
            title = "Last-Minute Cancellation",
            description = "Cancellations within 24 hours of the session are non-refundable.",
            icon = "❌"
        ),
        PolicyItem(
            title = "Artist Cancellation",
            description = "If your artist cancels, you receive a full refund automatically.",
            icon = "🔄"
        ),
        PolicyItem(
            title = "No-Show Protection",
            description = "If your artist doesn't show up, you get a full refund plus ₹200 compensation.",
            icon = "🛡️"
        ),
        PolicyItem(
            title = "Quality Guarantee",
            description = "Not satisfied with the work? Contact support within 24 hours for a review. Eligible cases receive a pro-rated refund.",
            icon = "⭐"
        ),
        PolicyItem(
            title = "Refund Processing",
            description = "Approved refunds are processed within 5–7 business days to your original payment method.",
            icon = "💰"
        ),
    )
}

data class PolicyItem(
    val title: String,
    val description: String,
    val icon: String,
)
