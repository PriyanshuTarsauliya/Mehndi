package com.mehei.app.ui.screens.artist.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehei.app.domain.model.Booking
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
    val selectedTab: Int = 0 // 0: New Requests, 1: Upcoming, 2: Past
) {
    val pendingBookings: List<Booking>
        get() = bookings.filter { it.status == BookingStatus.PENDING }
        
    val upcomingBookings: List<Booking>
        get() = bookings.filter { it.status == BookingStatus.CONFIRMED }
        
    val pastBookings: List<Booking>
        get() = bookings.filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.CANCELLED }
}

sealed interface ArtistDashboardEvent {
    data class TabSelected(val index: Int) : ArtistDashboardEvent
    data object Refresh : ArtistDashboardEvent
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
