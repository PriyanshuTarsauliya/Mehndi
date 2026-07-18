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
import com.mehei.app.domain.model.PaymentMethod
import com.mehei.app.domain.model.RefundStatus
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
    // Payment Method
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.UPI,
    // Coupon
    val couponCode: String = "",
    val couponDiscount: Int = 0,
    val isCouponApplied: Boolean = false,
    val couponError: String? = null,
    // Tip
    val tipAmount: Int = 0,
)

sealed interface CheckoutEvent {
    data class AddressChanged(val address: String) : CheckoutEvent
    data class LandmarkChanged(val landmark: String) : CheckoutEvent
    data class DateChanged(val date: String) : CheckoutEvent
    data class TimeChanged(val time: String) : CheckoutEvent
    data class PaymentMethodChanged(val method: PaymentMethod) : CheckoutEvent
    data class CouponChanged(val code: String) : CheckoutEvent
    data object ApplyCoupon : CheckoutEvent
    data object RemoveCoupon : CheckoutEvent
    data class TipChanged(val amount: Int) : CheckoutEvent
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

    // Hardcoded valid coupons for demo
    private val validCoupons = mapOf(
        "MEHEI50" to 50,
        "FIRST100" to 100,
        "BRIDAL200" to 200,
        "HENNA20" to 20,
    )

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
            is CheckoutEvent.PaymentMethodChanged -> _state.update { it.copy(selectedPaymentMethod = event.method) }
            is CheckoutEvent.CouponChanged -> _state.update { it.copy(couponCode = event.code, couponError = null) }
            is CheckoutEvent.ApplyCoupon -> applyCoupon()
            is CheckoutEvent.RemoveCoupon -> removeCoupon()
            is CheckoutEvent.TipChanged -> _state.update { it.copy(tipAmount = event.amount) }
            is CheckoutEvent.ConfirmPayment -> initiatePayment()
        }
    }

    private fun applyCoupon() {
        val code = _state.value.couponCode.trim().uppercase()
        val discount = validCoupons[code]
        if (discount != null) {
            val actualDiscount = discount.coerceAtMost(_state.value.depositAmount)
            _state.update {
                it.copy(
                    couponCode = code,
                    couponDiscount = actualDiscount,
                    isCouponApplied = true,
                    couponError = null,
                )
            }
        } else {
            _state.update { it.copy(couponError = "Invalid coupon code") }
        }
    }

    private fun removeCoupon() {
        _state.update {
            it.copy(
                couponCode = "",
                couponDiscount = 0,
                isCouponApplied = false,
                couponError = null,
            )
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
            // Calculate final amount: deposit - coupon + tip, in paise
            val finalAmount = (current.depositAmount - current.couponDiscount + current.tipAmount).coerceAtLeast(1)
            val amountPaise = finalAmount * 100
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
            status = BookingStatus.ACCEPTED,
            customerNote = "${current.landmark}",
            paymentMethod = current.selectedPaymentMethod,
            depositPaymentId = paymentId,
            tipAmount = current.tipAmount,
            couponCode = if (current.isCouponApplied) current.couponCode else null,
            couponDiscount = current.couponDiscount,
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
