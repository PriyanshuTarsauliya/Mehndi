package com.mehei.app.domain.usecase

import com.mehei.app.domain.model.PriceEstimate
import com.mehei.app.domain.model.RateCard

/**
 * Scope-based pricing calculator — the core MEHEI differentiator.
 *
 * Instead of pricing by occasion (wedding vs party), this calculates:
 *   total = (hands × pricePerHand) + (hours × hourlyRate) + materialFee
 *   final = total × (1 - flashDiscount%)
 *   deposit = final × 30%
 *
 * This makes a 90-minute Karva Chauth for 6 people its own product,
 * not a discount ask against a bridal rate.
 */
import javax.inject.Inject

class CalculatePriceUseCase @Inject constructor() {

    operator fun invoke(
        rateCard: RateCard,
        numHands: Int,
        estimatedHours: Float,
        flashSlotDiscountPercent: Int = 0,
        materialFee: Int = 0,
    ): PriceEstimate {
        require(numHands > 0) { "Must have at least 1 hand" }
        require(estimatedHours > 0f) { "Duration must be positive" }
        require(flashSlotDiscountPercent in 0..50) { "Discount must be 0-50%" }
        require(materialFee >= 0) { "Material fee cannot be negative" }

        val handsCost = numHands * rateCard.pricePerHand
        val hoursCost = (estimatedHours * rateCard.hourlyRate).toInt()
        val subtotal = handsCost + hoursCost + materialFee
        val discount = (subtotal * flashSlotDiscountPercent) / 100
        val total = subtotal - discount
        val deposit = (total * 30) / 100  // 30% non-refundable

        return PriceEstimate(
            handsCost = handsCost,
            hoursCost = hoursCost,
            materialFee = materialFee,
            discount = discount,
            total = total,
            deposit = deposit,
        )
    }
}
