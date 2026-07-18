package com.mehei.app.ui.screens.booking

import androidx.compose.runtime.Immutable
import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.model.EventType
import com.mehei.app.domain.model.PriceEstimate

@Immutable
data class BookingCalcState(
    val artist: Artist? = null,
    val numHands: Int = 4,
    val selectedComplexity: Complexity = Complexity.TRADITIONAL,
    val estimatedHours: Float = 1.5f,
    val selectedEventType: EventType = EventType.PARTY,
    val flashSlotDiscountPercent: Int = 0,
    val includeOrganic: Boolean = true,
    val materialFee: Int = 200,    // ₹200 for organic henna
    val priceEstimate: PriceEstimate? = null,
    val customerNote: String = "",
)

sealed interface BookingCalcEvent {
    data class SetHands(val count: Int) : BookingCalcEvent
    data class SetComplexity(val complexity: Complexity) : BookingCalcEvent
    data class SetHours(val hours: Float) : BookingCalcEvent
    data class SetEventType(val type: EventType) : BookingCalcEvent
    data class ToggleOrganicHenna(val include: Boolean) : BookingCalcEvent
    data class SetNote(val note: String) : BookingCalcEvent
    data object ConfirmBooking : BookingCalcEvent
}

sealed interface BookingCalcEffect {
    data class NavigateToCheckout(val artistId: String, val depositAmount: Int) : BookingCalcEffect
}
