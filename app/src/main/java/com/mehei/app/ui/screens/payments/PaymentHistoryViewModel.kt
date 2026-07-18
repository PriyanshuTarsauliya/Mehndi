package com.mehei.app.ui.screens.payments

import androidx.lifecycle.ViewModel
import com.mehei.app.domain.model.Payment
import com.mehei.app.domain.model.PaymentMethod
import com.mehei.app.domain.model.PaymentStatus
import com.mehei.app.domain.model.PaymentType
import com.mehei.app.domain.model.RefundStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(PaymentHistoryState())
    val state: StateFlow<PaymentHistoryState> = _state.asStateFlow()

    init {
        // Load demo payment data
        _state.value = PaymentHistoryState(
            payments = listOf(
                Payment(
                    id = "pay_1",
                    bookingId = "bk-a1b2c3d4",
                    customerId = "cust-current",
                    amount = 900,
                    type = PaymentType.DEPOSIT,
                    method = PaymentMethod.UPI,
                    status = PaymentStatus.SUCCESS,
                    razorpayPaymentId = "pay_NkX7cL9eDfG3Hj",
                    createdAt = "Jul 15, 2026 · 2:30 PM",
                ),
                Payment(
                    id = "pay_2",
                    bookingId = "bk-a1b2c3d4",
                    customerId = "cust-current",
                    amount = 100,
                    type = PaymentType.TIP,
                    method = PaymentMethod.UPI,
                    status = PaymentStatus.SUCCESS,
                    razorpayPaymentId = "pay_NkX7dM0fEgH4Ik",
                    createdAt = "Jul 15, 2026 · 2:30 PM",
                ),
                Payment(
                    id = "pay_3",
                    bookingId = "bk-e5f6g7h8",
                    customerId = "cust-current",
                    amount = 1500,
                    type = PaymentType.DEPOSIT,
                    method = PaymentMethod.CREDIT_CARD,
                    status = PaymentStatus.SUCCESS,
                    razorpayPaymentId = "pay_MjW6bK8cDfE2Gh",
                    createdAt = "Jul 10, 2026 · 11:00 AM",
                    refundAmount = 1500,
                    refundStatus = RefundStatus.PROCESSED,
                ),
                Payment(
                    id = "pay_4",
                    bookingId = "bk-i9j0k1l2",
                    customerId = "cust-current",
                    amount = 600,
                    type = PaymentType.DEPOSIT,
                    method = PaymentMethod.DEBIT_CARD,
                    status = PaymentStatus.FAILED,
                    createdAt = "Jul 8, 2026 · 4:45 PM",
                ),
            )
        )
    }
}
