package com.mehei.app.ui.screens.request

import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.model.EventType

data class RequestState(
    val isLoading: Boolean = false,
    val selectedEventType: EventType = EventType.PARTY,
    val selectedComplexity: Complexity = Complexity.TRADITIONAL,
    val selectedNumHands: Int = 2,
    val estimatedPrice: Int = 0,
    val estimatedEtaMinutes: Int = 10,
    val nearbyArtistsCount: Int = 3
)

sealed interface RequestEvent {
    data class OnEventTypeChanged(val eventType: EventType) : RequestEvent
    data class OnComplexityChanged(val complexity: Complexity) : RequestEvent
    data class OnNumHandsChanged(val numHands: Int) : RequestEvent
    object OnRequestArtistClick : RequestEvent
}

sealed interface RequestEffect {
    data class NavigateToMatching(val requestId: String) : RequestEffect
    data class ShowError(val message: String) : RequestEffect
}
