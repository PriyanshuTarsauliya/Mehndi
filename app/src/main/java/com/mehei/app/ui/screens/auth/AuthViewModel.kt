package com.mehei.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mehei.app.data.local.TokenManager
import com.mehei.app.data.remote.MeheiApiService

data class AuthState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isOtpSent: Boolean = false,
    val isLoading: Boolean = false,
    val isOtpVerified: Boolean = false,
    val name: String = "",
    val email: String = "",
    val role: String = "CLIENT",
    val isProfileSetupComplete: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: MeheiApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.PhoneNumberChanged -> updatePhoneNumber(event.number)
            is AuthEvent.OtpCodeChanged -> updateOtpCode(event.code)
            is AuthEvent.NameChanged -> updateName(event.name)
            is AuthEvent.EmailChanged -> updateEmail(event.email)
            is AuthEvent.RoleChanged -> updateRole(event.role)
            AuthEvent.SendOtp -> sendOtp()
            AuthEvent.VerifyOtp -> verifyOtp()
            AuthEvent.CompleteSetup -> completeProfileSetup()
        }
    }

    private fun updatePhoneNumber(number: String) {
        _state.update { it.copy(phoneNumber = number) }
    }

    private fun updateOtpCode(code: String) {
        _state.update { it.copy(otpCode = code) }
    }

    private fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    private fun updateEmail(email: String) {
        _state.update { it.copy(email = email) }
    }

    private fun updateRole(role: String) {
        _state.update { it.copy(role = role) }
    }

    private fun sendOtp() {
        if (_state.value.phoneNumber.length < 10) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.sendOtp(com.mehei.app.data.remote.OtpRequest(_state.value.phoneNumber))
                if (response.isSuccessful) {
                    _state.update { it.copy(isOtpSent = true, isLoading = false) }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun verifyOtp() {
        if (_state.value.otpCode.length < 6) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.verifyOtp(
                    com.mehei.app.data.remote.OtpVerificationRequest(_state.value.phoneNumber, _state.value.otpCode)
                )
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUserId(authResponse.userId)
                    tokenManager.saveUserPhone(authResponse.phoneNumber)
                    tokenManager.saveUserName(authResponse.name ?: "")
                    tokenManager.saveUserRole(authResponse.role)

                    _state.update { 
                        it.copy(
                            isOtpVerified = true, 
                            isLoading = false,
                            isProfileSetupComplete = !authResponse.isNewUser
                        ) 
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun completeProfileSetup() {
        if (_state.value.name.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.setupProfile(
                    com.mehei.app.data.remote.ProfileSetupRequest(
                        name = _state.value.name,
                        email = _state.value.email.ifBlank { null },
                        role = _state.value.role
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // Update tokens with the new role
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUserRole(authResponse.role)
                    tokenManager.saveUserName(authResponse.name ?: "")
                    _state.update { it.copy(isProfileSetupComplete = true, isLoading = false) }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
