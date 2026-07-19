package com.mehei.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.mehei.app.data.local.TokenManager
import com.mehei.app.data.remote.MeheiApiService
import okhttp3.MultipartBody
import kotlinx.coroutines.launch

data class ProfileState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val initial: String = "",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: MeheiApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        val name = tokenManager.getUserName() ?: "Guest"
        val phone = tokenManager.getUserPhone() ?: ""
        val initial = if (name.isNotEmpty()) name.take(1).uppercase() else "G"
        _state.value = ProfileState(
            name = name,
            phone = phone,
            email = "",
            initial = initial,
            profileImageUrl = tokenManager.getProfileImageUrl()
        )
    }

    fun uploadProfileImage(part: MultipartBody.Part) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true)
            try {
                val response = apiService.uploadProfileImage(part)
                if (response.isSuccessful && response.body() != null) {
                    val url = response.body()!!.profileImageUrl
                    tokenManager.saveProfileImageUrl(url)
                    _state.value = _state.value.copy(
                        profileImageUrl = url,
                        isUploading = false
                    )
                } else {
                    _state.value = _state.value.copy(isUploading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isUploading = false)
            }
        }
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updatePhone(phone: String) {
        _state.value = _state.value.copy(phone = phone)
    }

    fun saveChanges() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            // Mock network call
            kotlinx.coroutines.delay(1000)
            tokenManager.saveUserName(_state.value.name)
            _state.value = _state.value.copy(
                initial = if (_state.value.name.isNotEmpty()) _state.value.name.take(1).uppercase() else "G",
                isLoading = false
            )
        }
    }
}
