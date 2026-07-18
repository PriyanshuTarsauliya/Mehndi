package com.mehei.app.ui.screens.artist.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehei.app.domain.model.Booking
import com.mehei.app.domain.model.BookingRequest
import com.mehei.app.domain.model.BookingStatus
import com.mehei.app.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistDashboardState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0, // 0: New Requests, 1: Upcoming, 2: Past
    val isOnline: Boolean = false,
    val incomingRequest: BookingRequest? = null
) {
    val pendingBookings: List<Booking>
        get() = bookings.filter { it.status == BookingStatus.REQUESTED || it.status == BookingStatus.MATCHING }
        
    val upcomingBookings: List<Booking>
        get() = bookings.filter { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.ON_THE_WAY || it.status == BookingStatus.ARRIVED }
        
    val pastBookings: List<Booking>
        get() = bookings.filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.CANCELLED || it.status == BookingStatus.NO_SHOW }
}

sealed interface ArtistDashboardEvent {
    data class TabSelected(val index: Int) : ArtistDashboardEvent
    data object Refresh : ArtistDashboardEvent
    data class ToggleOnlineStatus(val isOnline: Boolean) : ArtistDashboardEvent
    data class ReceiveIncomingRequest(val request: BookingRequest) : ArtistDashboardEvent
    data object AcceptRequest : ArtistDashboardEvent
    data object DeclineRequest : ArtistDashboardEvent
}

@HiltViewModel
class ArtistDashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ArtistDashboardState())
    val state: StateFlow<ArtistDashboardState> = _state.asStateFlow()

    init {
        loadBookings()
    }

    fun onEvent(event: ArtistDashboardEvent) {
        when (event) {
            is ArtistDashboardEvent.TabSelected -> _state.update { it.copy(selectedTab = event.index) }
            is ArtistDashboardEvent.Refresh -> loadBookings()
            is ArtistDashboardEvent.ToggleOnlineStatus -> {
                _state.update { it.copy(isOnline = event.isOnline) }
                if (event.isOnline) {
                    // Simulate receiving a request after 5 seconds of going online
                    simulateIncomingRequest()
                } else {
                    _state.update { it.copy(incomingRequest = null) }
                }
            }
            is ArtistDashboardEvent.ReceiveIncomingRequest -> {
                _state.update { it.copy(incomingRequest = event.request) }
            }
            is ArtistDashboardEvent.AcceptRequest -> {
                // In reality, this would hit the API to claim the request
                _state.update { it.copy(incomingRequest = null) }
                loadBookings() // Refresh to see the new booking
            }
            is ArtistDashboardEvent.DeclineRequest -> {
                _state.update { it.copy(incomingRequest = null) }
            }
        }
    }
    
    private fun simulateIncomingRequest() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            if (_state.value.isOnline) {
                val mockReq = BookingRequest(
                    id = "req-123",
                    customerId = "cust-1",
                    customerName = "Priya Sharma",
                    numHands = 2,
                    complexity = com.mehei.app.domain.model.Complexity.TRADITIONAL,
                    eventType = com.mehei.app.domain.model.EventType.PARTY,
                    estimatedPrice = 2500,
                    clientLatitude = 19.0760,
                    clientLongitude = 72.8777
                )
                onEvent(ArtistDashboardEvent.ReceiveIncomingRequest(mockReq))
            }
        }
    }

    private fun loadBookings() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // "artist-current" matches the mock pending booking added in MockBookingRepositoryImpl
            when (val result = bookingRepository.getArtistBookings("artist-current")) {
                is com.mehei.app.domain.model.error.Result.Success -> {
                    _state.update { it.copy(
                        bookings = result.data,
                        isLoading = false
                    ) }
                }
                is com.mehei.app.domain.model.error.Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    // TODO: handle error
                }
            }
        }
    }
}
