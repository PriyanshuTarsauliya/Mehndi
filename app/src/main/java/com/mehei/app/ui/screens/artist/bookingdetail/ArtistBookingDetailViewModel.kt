package com.mehei.app.ui.screens.artist.bookingdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehei.app.domain.model.Booking
import com.mehei.app.domain.model.BookingStatus
import com.mehei.app.domain.usecase.ObserveBookingDetailsUseCase
import com.mehei.app.domain.usecase.UpdateBookingStatusUseCase
import com.mehei.app.domain.model.error.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistBookingDetailState(
    val booking: Booking? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isUpdating: Boolean = false
)

sealed interface ArtistBookingDetailEffect {
    data object NavigateBack : ArtistBookingDetailEffect
    data class ShowMessage(val message: String) : ArtistBookingDetailEffect
}

@HiltViewModel
class ArtistBookingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeBookingDetails: ObserveBookingDetailsUseCase,
    private val updateBookingStatus: UpdateBookingStatusUseCase
) : ViewModel() {

    private val bookingId: String = checkNotNull(savedStateHandle["bookingId"])

    private val _isUpdating = MutableStateFlow(false)

    val state: StateFlow<ArtistBookingDetailState> = kotlinx.coroutines.flow.combine(
        observeBookingDetails(bookingId),
        _isUpdating
    ) { booking, isUpdating ->
        if (booking != null) {
            ArtistBookingDetailState(booking = booking, isLoading = false, isUpdating = isUpdating)
        } else {
            ArtistBookingDetailState(error = "Booking not found", isLoading = false, isUpdating = isUpdating)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ArtistBookingDetailState(isLoading = true)
    )

    private val _effects = MutableSharedFlow<ArtistBookingDetailEffect>()
    val effects: SharedFlow<ArtistBookingDetailEffect> = _effects.asSharedFlow()

    fun acceptBooking() {
        updateStatus(BookingStatus.ACCEPTED, "Booking Accepted!")
    }

    fun declineBooking() {
        updateStatus(BookingStatus.CANCELLED, "Booking Declined")
    }

    private fun updateStatus(status: BookingStatus, successMessage: String) {
        viewModelScope.launch {
            _isUpdating.value = true
            val result = updateBookingStatus(bookingId, status)
            when (result) {
                is Result.Success -> {
                    _effects.emit(ArtistBookingDetailEffect.ShowMessage(successMessage))
                    _effects.emit(ArtistBookingDetailEffect.NavigateBack)
                }
                is Result.Error -> {
                    _effects.emit(ArtistBookingDetailEffect.ShowMessage("Failed to update booking: ${result.error.message}"))
                }
            }
            _isUpdating.value = false
        }
    }
}
