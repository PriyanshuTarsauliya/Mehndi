package com.mehei.app.ui.screens.checkout

import androidx.lifecycle.SavedStateHandle
import com.mehei.app.payment.PaymentEventManager
import com.mehei.app.payment.PaymentResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehei.app.domain.model.Booking
import com.mehei.app.domain.model.BookingStatus
import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.model.EventType
import com.mehei.app.domain.usecase.CreateBookingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

data class CheckoutState(
    val artistId: String = "",
    val depositAmount: Int = 0,
    val selectedDate: String = "Oct 24, 2026",
    val selectedTime: String = "10:00 AM",
    val address: String = "",
    val landmark: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
)

sealed interface CheckoutEvent {
    data class AddressChanged(val address: String) : CheckoutEvent
    data class LandmarkChanged(val landmark: String) : CheckoutEvent
    data class DateChanged(val date: String) : CheckoutEvent
    data class TimeChanged(val time: String) : CheckoutEvent
    data object ConfirmPayment : CheckoutEvent
}


sealed interface CheckoutEffect {
    data class NavigateToConfirmation(val bookingId: String) : CheckoutEffect
    data class ShowError(val message: String) : CheckoutEffect
    data class LaunchRazorpay(val amountPaise: Int) : CheckoutEffect
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createBookingUseCase: CreateBookingUseCase,
    private val paymentEventManager: PaymentEventManager
) : ViewModel() {

    private val artistId: String = checkNotNull(savedStateHandle["artistId"])
    private val depositAmount: Int = checkNotNull(savedStateHandle["depositAmount"])

    private val _state = MutableStateFlow(
        CheckoutState(artistId = artistId, depositAmount = depositAmount)
    )
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CheckoutEffect>()
    val effects: SharedFlow<CheckoutEffect> = _effects.asSharedFlow()
    
    private var pendingBookingId: String? = null

    init {
        viewModelScope.launch {
            paymentEventManager.paymentResults.collect { result ->
                when (result) {
                    is PaymentResult.Success -> {
                        // Payment successful, finalize the booking
                        finalizeBooking(result.paymentId)
                    }
                    is PaymentResult.Error -> {
                        _state.update { it.copy(isProcessing = false) }
                        _effects.emit(CheckoutEffect.ShowError("Payment failed: ${result.description}"))
                    }
                }
            }
        }
    }

    fun onEvent(event: CheckoutEvent) {
        when (event) {
            is CheckoutEvent.AddressChanged -> _state.update { it.copy(address = event.address) }
            is CheckoutEvent.LandmarkChanged -> _state.update { it.copy(landmark = event.landmark) }
            is CheckoutEvent.DateChanged -> _state.update { it.copy(selectedDate = event.date) }
            is CheckoutEvent.TimeChanged -> _state.update { it.copy(selectedTime = event.time) }
            is CheckoutEvent.ConfirmPayment -> initiatePayment()
        }
    }

    private fun initiatePayment() {
        val current = _state.value
        if (current.address.isBlank()) {
            viewModelScope.launch {
                _effects.emit(CheckoutEffect.ShowError("Please enter a delivery address."))
            }
            return
        }

        _state.update { it.copy(isProcessing = true, error = null) }
        viewModelScope.launch {
            // Amount in paise (multiply by 100)
            val amountPaise = current.depositAmount * 100
            _effects.emit(CheckoutEffect.LaunchRazorpay(amountPaise))
        }
    }
    
    private suspend fun finalizeBooking(paymentId: String) {
        val current = _state.value
        val bookingId = UUID.randomUUID().toString()
        val booking = Booking(
            id = bookingId,
            customerId = "cust-current", // Replaced with Firebase UID after auth
            artistId = current.artistId,
            artistName = "Artist", // Resolved from ArtistRepository in a real call
            bookingDate = current.selectedDate,
            startTime = current.selectedTime,
            endTime = "12:00", // Computed from estimated hours
            numHands = 4,      // Carried from calculator state (future enhancement)
            complexity = Complexity.TRADITIONAL,
            eventType = EventType.PARTY,
            totalPrice = (current.depositAmount * 100) / 30, // Back-compute from 30% deposit
            deposit = current.depositAmount,
            status = BookingStatus.CONFIRMED,
            customerNote = "Payment ID: $paymentId | ${current.landmark}",
        )
        try {
            when (val result = createBookingUseCase(booking)) {
                is com.mehei.app.domain.model.error.Result.Success -> {
                    _effects.emit(CheckoutEffect.NavigateToConfirmation(bookingId))
                }
                is com.mehei.app.domain.model.error.Result.Error -> {
                    _effects.emit(CheckoutEffect.ShowError(result.error.message))
                }
            }
        } catch (e: Exception) {
            _effects.emit(CheckoutEffect.ShowError(e.localizedMessage ?: "Unexpected error"))
        }
        _state.update { it.copy(isProcessing = false) }
    }
}
