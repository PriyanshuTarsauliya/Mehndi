package com.mehei.app.payment

sealed class PaymentResult {
    data class Success(val paymentId: String, val signature: String?, val orderId: String?) : PaymentResult()
    data class Error(val code: Int, val description: String) : PaymentResult()
}
