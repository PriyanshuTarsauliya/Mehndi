package com.mehei.app.payment

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentEventManager @Inject constructor() {
    private val _paymentResults = MutableSharedFlow<PaymentResult>(extraBufferCapacity = 1)
    val paymentResults: SharedFlow<PaymentResult> = _paymentResults.asSharedFlow()

    fun emitResult(result: PaymentResult) {
        _paymentResults.tryEmit(result)
    }
}
