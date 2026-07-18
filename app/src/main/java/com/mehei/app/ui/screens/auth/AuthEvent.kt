package com.mehei.app.ui.screens.auth

sealed interface AuthEvent {
    data class PhoneNumberChanged(val number: String) : AuthEvent
    data class OtpCodeChanged(val code: String) : AuthEvent
    data class NameChanged(val name: String) : AuthEvent
    data class EmailChanged(val email: String) : AuthEvent
    data class RoleChanged(val role: String) : AuthEvent
    data object SendOtp : AuthEvent
    data object VerifyOtp : AuthEvent
    data object CompleteSetup : AuthEvent
}
